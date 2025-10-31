package midEnd.ir.values.instructions;

import midEnd.ir.IrValue;
import midEnd.ir.values.IrBasicBlock;
import midEnd.ir.values.IrInstruction;

public class IrBranchInstruction extends IrInstruction {
    public IrBranchInstruction(String name, IrValue cond, IrValue ifTrue, IrValue ifFalse) {
        super(name, IrInstructionType.BranchInstr, ifTrue, ifFalse, cond);
    }

    public IrBranchInstruction(String name, IrValue destination) {
        super(name, IrInstructionType.BranchInstr, destination, null, null);
    }

    public void setDestination(IrBasicBlock basicBlock) {
        setUseValue1(basicBlock);
    }

    @Override
    public String toString() {
        return "";
    }
}
