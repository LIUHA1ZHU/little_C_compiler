package optimizers.modifiers;

import backend.mips.Register;
import midEnd.ir.IrModule;
import midEnd.ir.IrValue;
import midEnd.ir.values.IrBasicBlock;
import midEnd.ir.values.IrFunction;
import midEnd.ir.values.IrInstruction;
import midEnd.ir.values.instructions.phi.MoveInstr;
import midEnd.ir.values.instructions.phi.ParallelCopyInstr;
import midEnd.ir.values.instructions.phi.PhiInstr;
import midEnd.ir.values.instructions.IrInstructionType;
import midEnd.ir.values.IrConstant;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import midEnd.ir.values.IrGlobalValue;

public class RemovePhi {

    public static void run(IrModule irModule) {
        convertPhiToParallelCopy(irModule);
        lowerParallelCopies(irModule);
    }

    private static void convertPhiToParallelCopy(IrModule irModule) {
        for (IrGlobalValue globalValue : irModule.getGlobalValues()) {
            if (globalValue instanceof IrFunction function) {
                processFunctionPhi(function);
            }
        }
    }

    private static void processFunctionPhi(IrFunction function) {
        ArrayList<IrBasicBlock> blockList = new ArrayList<>(function.getBasicBlocks());
        for (IrBasicBlock block : blockList) {
            processBlockPhi(block);
        }
    }

    private static void processBlockPhi(IrBasicBlock block) {
        if (block.getInstructions().isEmpty() || !(block.getInstructions().get(0) instanceof PhiInstr)) {
            return;
        }

        ArrayList<ParallelCopyInstr> copyList = new ArrayList<>();
        ArrayList<IrBasicBlock> predecessors = new ArrayList<>(block.getPredecessors());
        
        for (IrBasicBlock beforeBlock : predecessors) {
            ParallelCopyInstr copyInstr = (beforeBlock.getSuccessors().size() == 1) ?
                insertCopyAtPredecessor(beforeBlock) :
                insertCopyAtNewMiddleBlock(beforeBlock, block);
            copyList.add(copyInstr);
        }

        Iterator<IrInstruction> iterator = block.getInstructions().iterator();
        while (iterator.hasNext()) {
            IrInstruction instr = iterator.next();
            if (instr instanceof PhiInstr phiInstr) {
                for (int i = 0; i < predecessors.size(); i++) {
                    IrBasicBlock pred = predecessors.get(i);
                    int index = phiInstr.getBeforeBlockList().indexOf(pred);
                    IrValue useValue = null;
                    if (index != -1 && index < phiInstr.getUseValueList().size()) {
                        useValue = phiInstr.getUseValueList().get(index);
                    }
                    
                    if (useValue != null) {
                        copyList.get(i).addCopy(useValue, phiInstr);
                    }
                }
                iterator.remove();
            } else {
                if (instr.getInstructionType() != IrInstructionType.PhiInstr) {
                    break; 
                }
            }
        }
    }

    private static ParallelCopyInstr insertCopyAtPredecessor(IrBasicBlock beforeBlock) {
        ParallelCopyInstr copyInstr = new ParallelCopyInstr(beforeBlock);
        beforeBlock.addInstrBeforeJump(copyInstr);
        return copyInstr;
    }

    private static ParallelCopyInstr insertCopyAtNewMiddleBlock(IrBasicBlock beforeBlock, IrBasicBlock nextBlock) {
        IrBasicBlock middleBlock = IrBasicBlock.addMiddleBlock(beforeBlock, nextBlock);
        ParallelCopyInstr copyInstr = new ParallelCopyInstr(middleBlock);
        middleBlock.addInstrBeforeJump(copyInstr);
        return findCopyInMiddleBlock(middleBlock);
    }
    
    private static ParallelCopyInstr findCopyInMiddleBlock(IrBasicBlock middleBlock) {
         for (IrInstruction instr : middleBlock.getInstructions()) {
             if (instr instanceof ParallelCopyInstr) {
                 return (ParallelCopyInstr) instr;
             }
         }
         return null;
    }

    private static void lowerParallelCopies(IrModule irModule) {
        for (IrGlobalValue globalValue : irModule.getGlobalValues()) {
            if (globalValue instanceof IrFunction function) {
                processFunctionCopies(function);
            }
        }
    }

    private static void processFunctionCopies(IrFunction function) {
        for (IrBasicBlock block : function.getBasicBlocks()) {
            processBlockCopies(block);
        }
    }

    private static void processBlockCopies(IrBasicBlock block) {
        ParallelCopyInstr copyInstr = null;
        for (IrInstruction instr : block.getInstructions()) {
            if (instr instanceof ParallelCopyInstr) {
                copyInstr = (ParallelCopyInstr) instr;
                break;
            }
        }
        
        if (copyInstr != null) {
            block.getInstructions().remove(copyInstr);
            expandParallelCopy(copyInstr, block);
        }
    }

    private static void expandParallelCopy(ParallelCopyInstr copyInstr, IrBasicBlock block) {
        ArrayList<MoveInstr> moveList = generateInitialMoves(copyInstr, block);
        
        ArrayList<MoveInstr> circleFixList = resolveCycles(copyInstr, block, moveList);
        ArrayList<MoveInstr> registerFixList = resolveInterferences(moveList, block);
        
        circleFixList.addAll(registerFixList);
        moveList.addAll(0, circleFixList);
        
        for (MoveInstr move : moveList) {
            block.addInstrBeforeJump(move);
        }
    }

    private static ArrayList<MoveInstr> generateInitialMoves(ParallelCopyInstr copyInstr, IrBasicBlock block) {
        ArrayList<IrValue> srcList = copyInstr.getSrcList();
        ArrayList<IrValue> dstList = copyInstr.getDstList();
        Map<IrValue, Register> registerMap = block.getParent().getValueRegisterMap();

        ArrayList<MoveInstr> moveList = new ArrayList<>();
        for (int i = 0; i < dstList.size(); i++) {
            IrValue src = srcList.get(i);
            IrValue dst = dstList.get(i);
            if (!src.equals(dst)) {
                Register srcReg = registerMap.get(src);
                Register dstReg = registerMap.get(dst);
                if (srcReg != null && dstReg != null && srcReg.equals(dstReg)) {
                    continue;
                }
                moveList.add(new MoveInstr(dst, src, block));
            }
        }
        return moveList;
    }

    private static ArrayList<MoveInstr> resolveCycles(ParallelCopyInstr copyInstr, IrBasicBlock block, ArrayList<MoveInstr> moveList) {
        ArrayList<IrValue> dstList = copyInstr.getDstList();
        ArrayList<MoveInstr> fixList = new ArrayList<>();
        HashSet<IrValue> valueRecord = new HashSet<>();
        
        for (int i = 0; i < moveList.size(); i++) {
            IrValue dstValue = dstList.get(i);
            //TODO If moveList has skipped elements (self-moves), this index mismatch is a potential bug in the original algorithm.

            if (!(dstValue instanceof IrConstant) && !valueRecord.contains(dstValue)) {
                if (hasCycle(copyInstr, i)) {
                    IrValue middleValue = new midEnd.ir.values.IrVirtualValue(dstValue.getValueType(), dstValue.getName() + "_tmp");
                    
                    for (MoveInstr moveInstr : moveList) {
                        if (moveInstr.getSrcValue().equals(dstValue)) {
                            moveInstr.setSrcValue(middleValue);
                        }
                    }
                    moveList.add(0, new MoveInstr(middleValue, dstValue, block));
                }
                valueRecord.add(dstValue);
            }
        }
        return fixList;
    }

    private static boolean hasCycle(ParallelCopyInstr copyInstr, int index) {
        ArrayList<IrValue> srcList = copyInstr.getSrcList();
        ArrayList<IrValue> dstList = copyInstr.getDstList();
        IrValue dstValue = dstList.get(index);
        for (int i = index + 1; i < srcList.size(); i++) {
            if (srcList.get(i).equals(dstValue)) {
                return true;
            }
        }
        return false;
    }

    private static ArrayList<MoveInstr> resolveInterferences(ArrayList<MoveInstr> moveList, IrBasicBlock block) {
        ArrayList<MoveInstr> fixList = new ArrayList<>();
        HashSet<IrValue> valueRecord = new HashSet<>();
        
        for (int i = moveList.size() - 1; i >= 0; i--) {
            IrValue srcValue = moveList.get(i).getSrcValue();
            if (!(srcValue instanceof IrConstant) && !valueRecord.contains(srcValue)) {
                if (hasInterference(moveList, i, block)) {
                    IrValue middleValue = new midEnd.ir.values.IrVirtualValue(srcValue.getValueType(), srcValue.getName() + "_tmp");
                    
                    for (MoveInstr moveInstr : moveList) {
                        if (moveInstr.getSrcValue() == srcValue) {
                            moveInstr.setSrcValue(middleValue);
                        }
                    }
                    MoveInstr moveInstr = new MoveInstr(middleValue, srcValue, block);
                    fixList.add(moveInstr);
                }
                valueRecord.add(srcValue);
            }
        }
        return fixList;
    }

    private static boolean hasInterference(ArrayList<MoveInstr> moveList, int index, IrBasicBlock block) {
        Map<IrValue, Register> registerMap = block.getParent().getValueRegisterMap();
        IrValue srcValue = moveList.get(index).getSrcValue();
        Register srcRegister = registerMap.get(srcValue);

        if (srcRegister != null) {
            for (int i = 0; i < index; i++) {
                IrValue dstValue = moveList.get(i).getDstValue();
                Register dstRegister = registerMap.get(dstValue);
                if (dstRegister != null && dstRegister.equals(srcRegister)) {
                    return true;
                }
            }
        }
        return false;
    }
}
