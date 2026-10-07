package optimizers.modifiers;

import midEnd.ir.IrModule;
import midEnd.ir.IrValue;
import midEnd.ir.values.IrBasicBlock;
import midEnd.ir.values.IrFunction;
import midEnd.ir.values.IrInstruction;
import midEnd.ir.values.instructions.IrArithmeticInstruction;
import midEnd.ir.values.instructions.IrBranchInstruction;
import midEnd.ir.values.instructions.IrIcmpInstruction;
import midEnd.ir.values.instructions.IrInstructionType;
import midEnd.ir.values.instructions.phi.PhiInstr;
import optimizers.analysers.CFGAnalyser;
import optimizers.analysers.DominanceAnalyser;

import java.util.*;

public class LoopInvariantCodeMotion {

    private static class Loop {
        IrBasicBlock header;
        Set<IrBasicBlock> blocks = new HashSet<>();
        List<IrBasicBlock> backEdges = new ArrayList<>(); // Latch blocks

        public Loop(IrBasicBlock header) {
            this.header = header;
            this.blocks.add(header);
        }
    }

    public static void run(IrModule module) {
        CFGAnalyser.run(module);
        DominanceAnalyser.run(module);

        for (IrFunction function : module.getFunctions()) {
            runOnFunction(function);
        }
    }

    private static void runOnFunction(IrFunction function) {
        List<Loop> loops = findLoops(function);
        
        // Sort loops by size (number of blocks) ascending, so we process inner loops first
        loops.sort(Comparator.comparingInt(l -> l.blocks.size()));

        for (Loop loop : loops) {
            processLoop(loop);
        }
    }

    private static List<Loop> findLoops(IrFunction function) {
        Map<IrBasicBlock, Loop> loopMap = new HashMap<>();
        List<Loop> loops = new ArrayList<>();

        // 1. Identify Back Edges
        for (IrBasicBlock bb : function.getBasicBlocks()) {
            for (IrBasicBlock succ : bb.getSuccessors()) {
                // If succ dominates bb, it's a back edge
                if (bb.getDominators().contains(succ)) {
                    Loop loop = loopMap.computeIfAbsent(succ, Loop::new);
                    loop.backEdges.add(bb);
                    if (!loops.contains(loop)) {
                        loops.add(loop);
                    }
                }
            }
        }

        // 2. Populate Loop Body for each loop
        for (Loop loop : loops) {
            Queue<IrBasicBlock> queue = new LinkedList<>();
            for (IrBasicBlock backEdge : loop.backEdges) {
                if (!loop.blocks.contains(backEdge)) {
                    loop.blocks.add(backEdge);
                    queue.add(backEdge);
                }
            }

            while (!queue.isEmpty()) {
                IrBasicBlock curr = queue.poll();
                for (IrBasicBlock pred : curr.getPredecessors()) {
                    if (!loop.blocks.contains(pred)) {
                        loop.blocks.add(pred);
                        queue.add(pred);
                    }
                }
            }
        }

        return loops;
    }

    private static void processLoop(Loop loop) {
        // 1. Find or Create Pre-Header
        IrBasicBlock preHeader = getOrCreatePreHeader(loop);
        if (preHeader == null) return; // Should not happen if CFG is valid

        // 2. Identify Loop Invariants
        boolean changed = true;
        List<IrInstruction> invariants = new ArrayList<>();
        Set<IrInstruction> invariantSet = new HashSet<>();
        
        while (changed) {
            changed = false;
            for (IrBasicBlock bb : loop.blocks) {
                for (IrInstruction instr : bb.getInstructions()) {
                    if (invariantSet.contains(instr)) continue;
                    if (canBeHoisted(instr) && isInvariant(instr, loop, invariantSet)) {
                        invariantSet.add(instr);
                        invariants.add(instr);
                        changed = true;
                    }
                }
            }
        }

        for (IrInstruction instr : invariants) {
            // Remove from old block
            instr.getParent().getInstructions().remove(instr);
            
            // Add to PreHeader (before terminator)
            preHeader.addInstrBeforeJump(instr);
            instr.setParent(preHeader);
        }
    }

    private static boolean canBeHoisted(IrInstruction instr) {
        // Only hoist safe instructions
        // Arithmetic, Icmp, GEP (if implemented), Conversion
        if (instr instanceof IrArithmeticInstruction) {
            IrArithmeticInstruction arith = (IrArithmeticInstruction) instr;
            if (arith.getArithmeticType() == IrArithmeticInstruction.IrArithmeticType.sdiv ||
                arith.getArithmeticType() == IrArithmeticInstruction.IrArithmeticType.srem) {
                return false; 
            }
            return true;
        }
        if (instr instanceof IrIcmpInstruction) return true;
        // if (instr instanceof IrZextInstruction) return true;
        return false;
    }

    private static boolean isInvariant(IrInstruction instr, Loop loop, Set<IrInstruction> currentInvariants) {
        // Collect operands
        ArrayList<IrValue> operands = new ArrayList<>();
        if (instr.getFirstUseValue() != null) operands.add(instr.getFirstUseValue());
        if (instr.getSecondUseValue() != null) operands.add(instr.getSecondUseValue());
        if (instr.getObjectiveUseValue() != null) operands.add(instr.getObjectiveUseValue());

        for (IrValue op : operands) {
            if (!isOperandInvariant(op, loop, currentInvariants)) {
                return false;
            }
        }
        return true;
    }

    private static boolean isOperandInvariant(IrValue op, Loop loop, Set<IrInstruction> currentInvariants) {
        // 1. Constant
        if (op instanceof midEnd.ir.values.IrConstant || 
            op instanceof midEnd.ir.values.IrGlobalValue) {
            return true;
        }
        
        // 2. Defined outside loop
        if (op instanceof IrInstruction) {
            IrInstruction def = (IrInstruction) op;
            if (!loop.blocks.contains(def.getParent())) {
                return true;
            }
            // 3. Defined inside loop but is invariant
            if (currentInvariants.contains(def)) {
                return true;
            }
        }
   
        if (op.getValueType() == midEnd.ir.IrValueType.Variable) {
             return true;
        }

        return false;
    }

    private static IrBasicBlock getOrCreatePreHeader(Loop loop) {
        IrBasicBlock header = loop.header;
        List<IrBasicBlock> preds = header.getPredecessors();
        List<IrBasicBlock> outsidePreds = new ArrayList<>();
        
        for (IrBasicBlock pred : preds) {
            if (!loop.blocks.contains(pred)) {
                outsidePreds.add(pred);
            }
        }

        if (outsidePreds.isEmpty()) return null; // Unreachable loop or entry is header (unlikely for loop)
        if (outsidePreds.size() == 1) {
            IrBasicBlock p = outsidePreds.get(0);
            if (p.getSuccessors().size() == 1 && p.getSuccessors().get(0) == header) {
                return p;
            }
        }

        // Create PreHeader
        IrBasicBlock preHeader = new IrBasicBlock(header.getName() + "_preheader");
        header.getParent().addBasicBlock(preHeader); // Add to function (needs correct position?)
        // Ideally add before header.
        int headerIdx = header.getParent().getBasicBlocks().indexOf(header);
        if (headerIdx != -1) {
            header.getParent().getBasicBlocks().remove(preHeader);
            header.getParent().getBasicBlocks().add(headerIdx, preHeader);
        }

        preHeader.setParent(header.getParent());

        // Update CFG: OutsidePreds -> PreHeader -> Header
        
        // 1. Redirect OutsidePreds to PreHeader
        for (IrBasicBlock pred : outsidePreds) {
            // Replace 'header' with 'preHeader' in pred's terminator
            IrInstruction term = pred.getInstructions().get(pred.getInstructions().size() - 1);
            if (term instanceof IrBranchInstruction) {
                IrBranchInstruction br = (IrBranchInstruction) term;
                if (br.getFirstUseValue() == header) {
                    br.setTrueDestination(preHeader);
                }
                if (br.getSecondUseValue() == header) {
                    br.setFalseDestination(preHeader);
                }
            }
            
            // Update CFG links
            pred.getSuccessors().remove(header);
            pred.getSuccessors().add(preHeader);
            header.getPredecessors().remove(pred);
            preHeader.getPredecessors().add(pred);
        }

        // 2. Connect PreHeader to Header
        preHeader.getSuccessors().add(header);
        header.getPredecessors().add(preHeader);
        
        IrBranchInstruction jump = new IrBranchInstruction(null, false);
        jump.setTrueDestination(header);
        jump.setParent(preHeader);
        preHeader.addInstr(jump);

        // 3. Move Phi inputs
        
        List<IrInstruction> headerInstrs = new ArrayList<>(header.getInstructions()); // Copy to avoid concurrent mod
        for (IrInstruction instr : headerInstrs) {
            if (instr instanceof PhiInstr) {
                PhiInstr headerPhi = (PhiInstr) instr;
                
                // Create new Phi in PreHeader
                PhiInstr prePhi = new PhiInstr(preHeader);
                boolean used = false;

                // Collect inputs from outside preds
                for (IrBasicBlock pred : outsidePreds) {
                    // Find value in original phi for this pred
                    int idx = headerPhi.getBeforeBlockList().indexOf(pred);
                    if (idx != -1) {
                        IrValue val = headerPhi.getUseValueList().get(idx);
                        // Set value in new phi
                        prePhi.convertBlockToValue(val, pred);
                        used = true;
                    }
                }
                
                if (used) {
                    // Add prePhi to PreHeader
                    preHeader.addInstrToHead(prePhi);
                    
                    // Modify headerPhi: remove outsidePreds entries, add PreHeader entry
                    List<IrBasicBlock> blocks = headerPhi.getBeforeBlockList();
                    List<IrValue> values = headerPhi.getUseValueList();
                   
                    for (IrBasicBlock pred : outsidePreds) {
                        int idx = blocks.indexOf(pred);
                        while (idx != -1) {
                            IrValue oldVal = values.get(idx);
                            if (oldVal != null) {
                                oldVal.removeUser(headerPhi);
                            }
                            blocks.remove(idx);
                            values.remove(idx);
                            idx = blocks.indexOf(pred);
                        }
                    }
                    
                    // Add PreHeader entry
                    blocks.add(preHeader);
                    values.add(prePhi);
                    prePhi.addUser(headerPhi);
                } 
            } else {
                break; // Phis are at the beginning
            }
        }

        return preHeader;
    }
}
