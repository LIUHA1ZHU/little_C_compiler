package midEnd.ir.values.instructions;

import backend.mips.MipsBuilder;
import backend.mips.Register;
import backend.mips.assembly.MipsAlu;
import backend.mips.assembly.MipsLsu;
import midEnd.ir.IrBuilder;
import midEnd.ir.values.IrInstruction;

/**
 * alloca returns a ptr
 */
public class IrAllocaInstruction extends IrInstruction {
    private int length;
    private boolean isArray;

    /**
     * length = 0 for unknown length in function parameter
     */
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

    public boolean isArray() {
        return isArray;
    }

    @Override
    public String toString() {
        if (!isArray) {
            return name + " = alloca i32, align 4\n";
        } else if (length != 0) {
            return name + " = alloca [" + length + " x i32], align 4\n";
        } else {
            return name + " = alloca i32*, align 8\n";
        }
    }

    public void toMips() {
        super.toMips();

        if (isArray) {
            MipsBuilder.AllocateStackSpace(4 * length);
        } else {
            MipsBuilder.AllocateStackSpace(4);
        }

        // save address on stack
        int pointerOffset = MipsBuilder.GetCurrentStackOffset();
        Register register = MipsBuilder.GetValueToRegister(this);
        if (register != null) {
            new MipsAlu(MipsAlu.AluType.ADDI, register, Register.SP, pointerOffset);
        } else {
            new MipsAlu(MipsAlu.AluType.ADDI, Register.K0, Register.SP, pointerOffset);
            pointerOffset = MipsBuilder.AllocateStackForValue(this);
            new MipsLsu(MipsLsu.LsuType.SW, Register.K0, Register.SP, pointerOffset);
        }
    }
}
