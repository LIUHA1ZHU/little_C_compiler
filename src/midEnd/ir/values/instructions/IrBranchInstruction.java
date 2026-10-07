package midEnd.ir.values.instructions;

import backend.mips.Register;
import backend.mips.assembly.MipsBranch;
import backend.mips.assembly.MipsJump;
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
        this(cond, true);
    }

    public IrBranchInstruction(IrValue cond, boolean autoAdd) {
        super("branch", IrInstructionType.BranchInstr, null, null, cond, autoAdd);
        if (autoAdd) {
            this.inBasicBlock = IrBuilder.getCurBasicBlock();
            IrBuilder.finishBasicBlockAndAddToFunc();
        }
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

    public void toMips() {
        super.toMips();

        if (getObjectiveUseValue() != null) {
            IrValue cond = getObjectiveUseValue();
            Register condRegister = this.GetRegisterOrK0ForValue(cond);
            this.LoadValueToRegister(cond, condRegister);
            // bne → true destination
            new MipsBranch(MipsBranch.BranchType.BNE, condRegister, Register.ZERO,
                    ((IrBasicBlock) getFirstUseValue()).getMipsLabel());
            // false destination
            new MipsJump(MipsJump.JumpType.J, ((IrBasicBlock) getSecondUseValue()).getMipsLabel());
        } else { // direct branch
            new MipsJump(MipsJump.JumpType.J, (getFirstUseValue() != null ?
                    ((IrBasicBlock) getFirstUseValue()).getMipsLabel() : ((IrBasicBlock) getSecondUseValue()).getMipsLabel()));
        }
    }
}
