package midEnd.ir.values.instructions;

import midEnd.ir.IrBuilder;
import midEnd.ir.IrValue;
import midEnd.ir.values.IrInstruction;

public class IrIcmpInstruction extends IrInstruction {
    public enum IcmpCondType {
        sgt, sge, slt, sle, eq, ne
    }
    private IcmpCondType icmpCondType;

    public IrIcmpInstruction(IcmpCondType icmpCondType, IrValue value1, IrValue value2) {
        super(IrBuilder.LocalPrefix + IrBuilder.getTempVarNum(), IrInstructionType.IcmpInstr, value1, value2, null);
        this.icmpCondType = icmpCondType;
    }

    @Override
    public String toString() {
        return name + " = icmp " + icmpCondType + " i32 " + getFirstUseValue().getName() + ", " + getSecondUseValue().getName() + "\n";
    }
}
