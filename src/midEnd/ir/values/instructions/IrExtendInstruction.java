package midEnd.ir.values.instructions;

import backend.mips.Register;
import midEnd.ir.IrBuilder;
import midEnd.ir.IrValue;
import midEnd.ir.values.IrInstruction;

// %result = zext i1 %value to i32
/**
 * only used in !unary , zero extend i1 to i32
 */
public class IrExtendInstruction extends IrInstruction {
    public IrExtendInstruction(IrValue value1) {
        super(IrBuilder.LocalPrefix + IrBuilder.getTempVarNum(), IrInstructionType.ExtendInstr, value1, null, null);
    }

    @Override
    public String toString() {
        return name + " = zext i1 " + getFirstUseValue().getName() + " to i32\n";
    }

    public void toMips() {
        super.toMips();

        Register register = this.GetRegisterOrK0ForValue(this);
        this.LoadValueToRegister(getFirstUseValue(), register);
        this.SaveRegisterResult(this, register);
    }
}
