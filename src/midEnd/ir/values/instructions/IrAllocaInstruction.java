package midEnd.ir.values.instructions;

import midEnd.ir.IrBuilder;
import midEnd.ir.values.IrInstruction;

public class IrAllocaInstruction extends IrInstruction {
    public IrAllocaInstruction(String name) {
        super(IrBuilder.LocalPrefix + name, IrInstructionType.AllocateInstr, null, null, null);
    }

    @Override
    public String toString() {
        return name + " = alloca i32, align 4\n";
    }
}
