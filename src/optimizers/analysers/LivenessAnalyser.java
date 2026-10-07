package optimizers.analysers;

import midEnd.ir.IrModule;
import midEnd.ir.IrValue;
import midEnd.ir.values.IrBasicBlock;
import midEnd.ir.values.IrConstant;
import midEnd.ir.values.IrFunction;
import midEnd.ir.values.IrGlobalValue;
import midEnd.ir.values.IrInstruction;
import midEnd.ir.values.instructions.IOInstructions.IrPutintInstruction;
import midEnd.ir.values.instructions.IOInstructions.IrPutstrInstruction;
import midEnd.ir.values.instructions.IrBranchInstruction;
import midEnd.ir.values.instructions.IrCallInstruction;
import midEnd.ir.values.instructions.IrReturnInstruction;
import midEnd.ir.values.instructions.IrStoreInstruction;
import midEnd.ir.values.instructions.phi.MoveInstr;
import midEnd.ir.values.instructions.phi.PhiInstr;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class LivenessAnalyser {
    private static HashMap<IrBasicBlock, HashMap<IrInstruction, Set<IrValue>>> liveOutMap;
    private static HashMap<IrBasicBlock, Set<IrValue>> blockLiveIn;
    private static HashMap<IrBasicBlock, Set<IrValue>> blockLiveOut;

    public static void run(IrModule irModule) {
        liveOutMap = new HashMap<>();
        blockLiveIn = new HashMap<>();
        blockLiveOut = new HashMap<>();

        irModule.getGlobalValues().stream()
                .filter(IrFunction.class::isInstance)
                .map(IrFunction.class::cast)
                .forEach(LivenessAnalyser::analyseFunction);
    }

    private static void analyseFunction(IrFunction function) {
        // Initialize block sets
        for (IrBasicBlock block : function.getBasicBlocks()) {
            blockLiveIn.put(block, new HashSet<>());
            blockLiveOut.put(block, new HashSet<>());
        }

        boolean changed = true;
        while (changed) {
            changed = false;
            // Iterate blocks in reverse order for faster convergence
            ArrayList<IrBasicBlock> blocks = function.getBasicBlocks();
            for (int i = blocks.size() - 1; i >= 0; i--) {
                IrBasicBlock block = blocks.get(i);
                
                // LiveOut[B] = Union(LiveIn[S]) for S in Successors(B)
                Set<IrValue> newLiveOut = new HashSet<>();
                for (IrBasicBlock succ : block.getSuccessors()) {
                    if (blockLiveIn.get(succ) != null) {
                        newLiveOut.addAll(blockLiveIn.get(succ));
                    }
                }
                
                // Update LiveOut for block
                blockLiveOut.put(block, newLiveOut);

                // Calculate LiveIn[B]
                Set<IrValue> currentLive = new HashSet<>(newLiveOut);
                ArrayList<IrInstruction> instructions = block.getInstructions();
                for (int j = instructions.size() - 1; j >= 0; j--) {
                    IrInstruction instr = instructions.get(j);
                    
                    // Kill (Remove Def)
                    removeDef(instr, currentLive);
                    
                    // Gen (Add Use)
                    addUsedValuesToLiveSet(instr, currentLive);
                }

                // Check for change
                Set<IrValue> oldLiveIn = blockLiveIn.get(block);
                if (!currentLive.equals(oldLiveIn)) {
                    blockLiveIn.put(block, currentLive);
                    changed = true;
                }
            }
        }

        // Final pass to populate instruction-level map
        for (IrBasicBlock block : function.getBasicBlocks()) {
            analyseBlockDetailed(block);
        }
    }

    private static void analyseBlockDetailed(IrBasicBlock basicBlock) {
        HashMap<IrInstruction, Set<IrValue>> instructionLiveOut = new HashMap<>();
        ArrayList<IrInstruction> instructions = basicBlock.getInstructions();
        
        // Start with block's LiveOut
        Set<IrValue> currentLive = new HashSet<>(blockLiveOut.get(basicBlock));

        for (int i = instructions.size() - 1; i >= 0; i--) {
            IrInstruction instruction = instructions.get(i);
            
            // Record LiveOut for this instruction
            instructionLiveOut.put(instruction, new HashSet<>(currentLive));

            // Update for previous instruction (LiveIn of current is LiveOut of prev)
            
            // 1. Remove Def
            removeDef(instruction, currentLive);
            
            // 2. Add Use
            addUsedValuesToLiveSet(instruction, currentLive);
        }
        
        liveOutMap.put(basicBlock, instructionLiveOut);
    }

    public static Set<IrValue> getLiveOut(IrInstruction instruction) {
        IrBasicBlock block = instruction.getParent();
        if (liveOutMap.containsKey(block) && liveOutMap.get(block).containsKey(instruction)) {
            return new HashSet<>(liveOutMap.get(block).get(instruction));
        }
        return new HashSet<>();
    }

    public static Set<IrValue> getLiveIn(IrInstruction instruction) {
        Set<IrValue> liveIn = new HashSet<>(getLiveOut(instruction));
        removeDef(instruction, liveIn);
        addUsedValuesToLiveSet(instruction, liveIn);
        return liveIn;
    }

    public static boolean isNonDefInstr(IrInstruction instruction) {
        return instruction instanceof IrBranchInstruction ||
                instruction instanceof IrPutintInstruction ||
                instruction instanceof IrPutstrInstruction ||
                instruction instanceof IrStoreInstruction ||
                instruction instanceof IrReturnInstruction ||
                (instruction instanceof IrCallInstruction && "void".equals(instruction.getName()));
    }

    private static void addUsedValuesToLiveSet(IrInstruction instruction, Set<IrValue> liveSet) {
        if (instruction instanceof IrCallInstruction call) {
            for (IrValue param : call.getParamsList()) {
                addValueIfValid(param, liveSet);
            }
        } else if (instruction instanceof PhiInstr phi) {
            for (IrValue value : phi.getUseValueList()) {
                addValueIfValid(value, liveSet);
            }
        } else if (instruction instanceof MoveInstr move) {
            addValueIfValid(move.getSrcValue(), liveSet);
        } else {
            addValueIfValid(instruction.getFirstUseValue(), liveSet);
            addValueIfValid(instruction.getSecondUseValue(), liveSet);
            addValueIfValid(instruction.getObjectiveUseValue(), liveSet);
        }
    }
    
    private static void removeDef(IrInstruction instruction, Set<IrValue> liveSet) {
        if (instruction instanceof MoveInstr move) {
            liveSet.remove(move.getDstValue());
        } else if (!isNonDefInstr(instruction)) {
            liveSet.remove(instruction);
        }
    }

    private static void addValueIfValid(IrValue value, Set<IrValue> liveSet) {
        if (value != null && !(value instanceof IrConstant) && !(value instanceof IrGlobalValue)) {
            liveSet.add(value);
        }
    }
}
