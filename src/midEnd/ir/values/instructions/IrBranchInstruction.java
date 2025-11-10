package midEnd.ir.values.instructions;

import midEnd.ir.IrBuilder;
import midEnd.ir.IrValue;
import midEnd.ir.values.IrBasicBlock;
import midEnd.ir.values.IrInstruction;

/**
 * conditional branch when cond != null
 * unconditional branch destination can be both trueDestination or falseDestination
 */
public class IrBranchInstruction extends IrInstruction {
    private IrBasicBlock inBasicBlock;

    public IrBranchInstruction(IrValue cond) {
        super("branch", IrInstructionType.BranchInstr, null, null, cond);
        this.inBasicBlock = IrBuilder.getCurBasicBlock();
        IrBuilder.finishBasicBlockAndAddToFunc();
    }

    public void setTrueDestination(IrBasicBlock basicBlock) {
        setUseValue1(basicBlock);
    }

    public void setFalseDestination(IrBasicBlock basicBlock) {
        setUseValue2(basicBlock);
    }

    @Override
    public String toString() {
        if (getObjectiveUseValue() == null) { // direct branch
            return "br label %" + (getFirstUseValue() != null ? getFirstUseValue().getName() : getSecondUseValue().getName()) + "\n";
        } else {
            return "br i1 " + getObjectiveUseValue().getName() + ", label %" + getFirstUseValue().getName() + ", label %"
                    + getSecondUseValue().getName() + "\n";
        }
    }
}
