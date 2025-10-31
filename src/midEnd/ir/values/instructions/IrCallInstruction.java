package midEnd.ir.values.instructions;

import midEnd.ir.values.IrInstruction;

public class IrCallInstruction extends IrInstruction {
    //TODO
    public IrCallInstruction(String name, IrInstructionType instructionType) {
        super(name, instructionType, null, null, null);
    }

    @Override
    public String toString() {
        return "";
    }
}
