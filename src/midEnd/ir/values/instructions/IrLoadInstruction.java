package midEnd.ir.values.instructions;

import backend.mips.MipsBuilder;
import backend.mips.Register;
import backend.mips.assembly.MipsLsu;
import midEnd.ir.IrBuilder;
import midEnd.ir.IrValue;
import midEnd.ir.values.IrInstruction;

// %result = load i32, i32* %ptr
public class IrLoadInstruction extends IrInstruction {
    /**
     * Just the name matters
     */
    public IrLoadInstruction(IrValue memPtr) {
        super(IrBuilder.LocalPrefix + IrBuilder.getTempVarNum(), IrInstructionType.LoadInstr, null, null, memPtr);
    }

    @Override
    public String toString() {
        return getName() + " = load i32, i32* " + getObjectiveUseValue().getName() + ", align 4\n";
    }

    public void toMips() {
        super.toMips();

        IrValue pointer = getObjectiveUseValue();
        Register pointerRegister = this.GetRegisterOrK0ForValue(pointer);
        Register targetRegister = this.GetRegisterOrK0ForValue(this);

        this.LoadValueToRegister(pointer, pointerRegister);

        new MipsLsu(MipsLsu.LsuType.LW, targetRegister, pointerRegister, 0);
        // save to stack
        if (MipsBuilder.GetValueToRegister(this) == null) {
            int offset = MipsBuilder.AllocateStackForValue(this);
            new MipsLsu(MipsLsu.LsuType.SW, targetRegister, Register.SP, offset);
        }
    }

    public void replaceUsedWith(IrValue value) {
        for (midEnd.ir.values.IrUser user : this.getUserList()) {
            if (user.getFirstUseValue() == this) {
                user.setUseValue1(value);
            } else if (user.getSecondUseValue() == this) {
                user.setUseValue2(value);
            } else if (user.getObjectiveUseValue() == this) {
                user.setUseValueP(value);
            }
            value.addUser(user);
        }
    }
}
