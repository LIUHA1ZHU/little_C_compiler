package optimizers.modifiers;

import midEnd.ir.values.IrBasicBlock;
import midEnd.ir.values.IrConstant;
import midEnd.ir.values.IrInstruction;
import midEnd.ir.values.IrUser;
import midEnd.ir.IrValue;
import midEnd.ir.values.instructions.IrAllocaInstruction;
import midEnd.ir.values.instructions.IrLoadInstruction;
import midEnd.ir.values.instructions.IrStoreInstruction;
import midEnd.ir.values.instructions.phi.PhiInstr;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Stack;

public class InsertPhi {
    private final IrAllocaInstruction allocateInstr;
    private final IrBasicBlock entryBlock;
    private final HashSet<IrInstruction> defineInstrs;
    private final HashSet<IrInstruction> useInstrs;
    private final ArrayList<IrBasicBlock> defineBlocks;
    private final ArrayList<IrBasicBlock> useBlocks;
    private Stack<IrValue> valueStack;

    public InsertPhi(IrAllocaInstruction allocateInstr, IrBasicBlock entryBlock) {
        this.allocateInstr = allocateInstr;
        this.entryBlock = entryBlock;
        this.defineInstrs = new HashSet<>();
        this.useInstrs = new HashSet<>();
        this.defineBlocks = new ArrayList<>();
        this.useBlocks = new ArrayList<>();
        this.valueStack = new Stack<>();
    }

    public void addPhi() {
        // System.out.println("Processing Alloca: " + this.allocateInstr.getName());
        buildDefineUseRelationship();
        insertPhiToBlock();
        convertLoadStore(this.entryBlock);
    }

    private void buildDefineUseRelationship() {
        for (IrUser user : this.allocateInstr.getUserList()) {
            if (user instanceof IrInstruction userInstr) {
                if (userInstr instanceof IrLoadInstruction) {
                    this.addUseInstr(userInstr);
                } else if (userInstr instanceof IrStoreInstruction storeInstr) {
                    if (storeInstr.getObjectiveUseValue() == this.allocateInstr) {
                        this.addDefineInstr(userInstr);
                    }
                }
            }
        }
    }

    private void addDefineInstr(IrInstruction instr) {
        this.defineInstrs.add(instr);
        if (!this.defineBlocks.contains(instr.getParent())) {
            this.defineBlocks.add(instr.getParent());
        }
    }

    private void addUseInstr(IrInstruction instr) {
        this.useInstrs.add(instr);
        if (!this.useBlocks.contains(instr.getParent())) {
            this.useBlocks.add(instr.getParent());
        }
    }

    private void insertPhiToBlock() {
        HashSet<IrBasicBlock> addedPhiBlocks = new HashSet<>();
        Stack<IrBasicBlock> defineBlockStack = new Stack<>();

        for (IrBasicBlock defineBlock : this.defineBlocks) {
            defineBlockStack.push(defineBlock);
        }

        while (!defineBlockStack.isEmpty()) {
            IrBasicBlock defineBlock = defineBlockStack.pop();
            for (IrBasicBlock frontierBlock : defineBlock.getDominanceFrontier()) {
                if (!addedPhiBlocks.contains(frontierBlock)) {
                    this.insertPhiInstr(frontierBlock);
                    addedPhiBlocks.add(frontierBlock);
                    if (!this.defineBlocks.contains(frontierBlock)) {
                        defineBlockStack.push(frontierBlock);
                    }
                }
            }
        }
    }

    private void insertPhiInstr(IrBasicBlock irBasicBlock) {
        PhiInstr phiInstr = new PhiInstr(irBasicBlock);
        irBasicBlock.addInstrToHead(phiInstr);
        this.useInstrs.add(phiInstr);
        this.defineInstrs.add(phiInstr);
    }

    private void convertLoadStore(IrBasicBlock renameBlock) {
        final Stack<IrValue> stackCopy = (Stack<IrValue>) this.valueStack.clone();

        this.removeBlockLoadStore(renameBlock);
        this.convertPhiValue(renameBlock);

        for (IrBasicBlock dominateBlock : renameBlock.getDirectDominateBlocks()) {
            this.convertLoadStore(dominateBlock);
        }

        this.valueStack = stackCopy;
    }

    private void removeBlockLoadStore(IrBasicBlock visitBlock) {
        Iterator<IrInstruction> iterator = visitBlock.getInstructions().iterator();
        while (iterator.hasNext()) {
            IrInstruction instr = iterator.next();

            if (instr instanceof IrStoreInstruction storeInstr && this.defineInstrs.contains(instr)) {
                this.valueStack.push(storeInstr.getFirstUseValue());
                iterator.remove();
            } else if (!(instr instanceof PhiInstr) && this.useInstrs.contains(instr)) {
                // System.out.println("Replacing load " + instr.getName() + " in " + visitBlock.getName() + " with " + this.peekValueStack().getName());
                instr.modifyAllUsersToNewValue(this.peekValueStack());
                iterator.remove();
            } else if (instr instanceof PhiInstr && this.defineInstrs.contains(instr)) {
                this.valueStack.push(instr);
            } else if (instr == this.allocateInstr) {
                iterator.remove();
            }
        }
    }

    private void convertPhiValue(IrBasicBlock visitBlock) {
        for (IrBasicBlock nextBlock : visitBlock.getSuccessors()) {
            for (IrInstruction instr : nextBlock.getInstructions()) {
                if (instr instanceof PhiInstr phiInstr && this.useInstrs.contains(phiInstr)) {
                    phiInstr.convertBlockToValue(this.peekValueStack(), visitBlock);
                } else {
                    if (!(instr instanceof PhiInstr)) break;
                }
            }
        }
    }

    private IrValue peekValueStack() {
        return this.valueStack.isEmpty() ? new IrConstant(0) : this.valueStack.peek();
    }
}
