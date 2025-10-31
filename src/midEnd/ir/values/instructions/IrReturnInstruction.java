package midEnd.ir.values.instructions;

import midEnd.ir.IrValue;
import midEnd.ir.values.IrInstruction;

public class IrReturnInstruction extends IrInstruction {
    public IrReturnInstruction(String name, IrInstructionType instructionType, IrValue useValue) {
        super(name, instructionType, null, null, useValue);
    }

    public IrReturnInstruction(String name, IrInstructionType instructionType) {
        super(name, instructionType, null, null, null);
    }

    @Override
    public String toString() {
        if (getObjectiveUseValue() == null) return "ret void\n";
        else return "ret i32 " + getObjectiveUseValue().getName() + "\n";
    }
}
