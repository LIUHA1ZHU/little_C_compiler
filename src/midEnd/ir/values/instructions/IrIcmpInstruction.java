package midEnd.ir.values.instructions;

import midEnd.ir.IrValue;
import midEnd.ir.values.IrInstruction;

public class IrIcmpInstruction extends IrInstruction {
    public IrIcmpInstruction(String name, IrInstructionType instructionType, IrValue cond, IrValue value1, IrValue value2) {
        super(name, instructionType, value1, value2, cond);
    }

    @Override
    public String toString() {
        return "";
    }
}
