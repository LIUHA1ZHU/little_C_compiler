package midEnd.ir.values.instructions;

import midEnd.ir.IrBuilder;
import midEnd.ir.values.IrInstruction;

/**
 * alloca returns a ptr
 */
public class IrAllocaInstruction extends IrInstruction {
    private int length;
    private boolean isArray;

    public IrAllocaInstruction(String name, int length) {
        super(IrBuilder.LocalPrefix + name, IrInstructionType.AllocateInstr, null, null, null);
        this.length = length;
        this.isArray = true;
    }

    public IrAllocaInstruction(String name) {
        super(IrBuilder.LocalPrefix + name, IrInstructionType.AllocateInstr, null, null, null);
        this.length = 1;
        this.isArray = false;
    }

    public int getLength() {
        return length;
    }

    @Override
    public String toString() {
        if (!isArray) {
            return name + " = alloca i32, align 4\n";
        } else {
            return name + " = alloca [" + length + " x i32], align 4\n";
        }
    }
}
