package optimizers.modifiers;

import optimizers.analysers.CFGAnalyser;
import midEnd.ir.IrModule;
import midEnd.ir.values.IrBasicBlock;
import midEnd.ir.values.IrFunction;
import midEnd.ir.values.IrInstruction;
import midEnd.ir.values.instructions.IrBranchInstruction;

import java.util.ArrayList;

public class BasicBlockMerger {
    public static void run(IrModule irModule) {
        CFGAnalyser analyser = new CFGAnalyser();
        analyser.analyse(irModule);

        boolean changed;
        do {
            changed = false;
            for (var globalValue : irModule.getGlobalValues()) {
                if (!(globalValue instanceof IrFunction function)) {
                    continue;
                }

                ArrayList<IrBasicBlock> blocks = function.getBasicBlocks();

                if (tryMergeBlocks(blocks)) {
                    changed = true;
                    break;
                }

                // if the only instr in a block is unconditional branch, it's removable
                if (tryRemovePassThroughBlock(blocks)) {
                    changed = true;
                    break;
                }
            }
        } while (changed);

        analyser.analyse(irModule);
    }

    private static boolean tryMergeBlocks(ArrayList<IrBasicBlock> blocks) {
        for (int i = 0; i < blocks.size(); i++) {
            IrBasicBlock b = blocks.get(i);

            if (!canMergeWithSuccessor(b)) {
                continue;
            }

            IrBasicBlock successor = b.getSuccessors().get(0);

            if (!isOnlyPredecessor(successor, b)) {
                continue;
            }
            // self-ring
            if (successor.getSuccessors().contains(successor)) {
                continue;
            }

            removeUnconditionalBranch(b);

            mergeBlocks(b, successor, blocks);
            return true;
        }
        return false;
    }

    private static boolean canMergeWithSuccessor(IrBasicBlock block) {
        return block.getSuccessors().size() == 1;
    }

    private static boolean isOnlyPredecessor(IrBasicBlock block, IrBasicBlock expectedPredecessor) {
        return block.getPredecessors().size() == 1
                && block.getPredecessors().get(0) == expectedPredecessor;
    }

    private static void removeUnconditionalBranch(IrBasicBlock block) {
        ArrayList<IrInstruction> instrs = block.getInstructions();
        if (instrs.isEmpty()) {
            return;
        }

        IrInstruction lastInstr = instrs.get(instrs.size() - 1);

        if (lastInstr instanceof IrBranchInstruction
                && lastInstr.getObjectiveUseValue() == null) {
            instrs.remove(instrs.size() - 1);
        }
    }

    private static void mergeBlocks(IrBasicBlock b, IrBasicBlock successor, ArrayList<IrBasicBlock> blocks) {
        for (IrInstruction instr : successor.getInstructions()) {
            instr.setParent(b);
        }
        b.getInstructions().addAll(successor.getInstructions());

        // update successor
        b.getSuccessors().clear();
        // would generate self-ring
        b.getSuccessors().addAll(successor.getSuccessors());


        // update predecessor
        updatePredecessors(successor, b);

        blocks.remove(successor);
    }

    private static void updatePredecessors(IrBasicBlock successor, IrBasicBlock newBlock) {
        for (IrBasicBlock ss : successor.getSuccessors()) {
            ArrayList<IrBasicBlock> preds = ss.getPredecessors();
            for (int p = 0; p < preds.size(); p++) {
                if (preds.get(p) == successor) {
                    preds.set(p, newBlock);
                }
            }
        }
    }

    private static boolean tryRemovePassThroughBlock(ArrayList<IrBasicBlock> blocks) {
        for (int i = 0; i < blocks.size(); i++) {
            IrBasicBlock b = blocks.get(i);

            if (b.getInstructions().size() != 1) {
                continue;
            }

            IrInstruction only = b.getInstructions().get(0);
            if (!(only instanceof IrBranchInstruction) || only.getObjectiveUseValue() != null) {
                continue;
            }

            IrBasicBlock target = null;
            if (only.getFirstUseValue() != null) {
                target = (IrBasicBlock) only.getFirstUseValue();
            } else if (only.getSecondUseValue() != null) {
                target = (IrBasicBlock) only.getSecondUseValue();
            }
            if (target == null || target == b) { // avoid self-ring
                continue;
            }

            // redirect target
            for (IrBasicBlock p : blocks) {
                for (IrInstruction ins : p.getInstructions()) {
                    if (ins instanceof IrBranchInstruction br) {
                        if (br.getFirstUseValue() == b) {
                            br.setTrueDestination(target);
                        }
                        if (br.getSecondUseValue() == b) {
                            br.setFalseDestination(target);
                        }
                    }
                }
            }

            for (IrBasicBlock pred : b.getPredecessors()) {
                ArrayList<IrBasicBlock> succs = pred.getSuccessors();
                for (int s = 0; s < succs.size(); s++) {
                    if (succs.get(s) == b) {
                        succs.set(s, target);
                    }
                }
                ArrayList<IrBasicBlock> tpreds = target.getPredecessors();
                boolean exists = false;
                for (IrBasicBlock tp : tpreds) {
                    if (tp == pred) {
                        exists = true;
                        break;
                    }
                }
                if (!exists) {
                    tpreds.add(pred);
                }
            }

            target.getPredecessors().removeIf(x -> x == b);

            blocks.remove(b);
            return true;
        }
        return false;
    }
}
