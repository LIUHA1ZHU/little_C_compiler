package midEnd.ir.values.instructions.IOInstructions;

import backend.mips.Register;
import backend.mips.assembly.MipsSyscall;
import backend.mips.assembly.pseudo.MarsLi;
import midEnd.ir.IrBuilder;
import midEnd.ir.IrValue;
import midEnd.ir.values.IrInstruction;
import midEnd.ir.values.instructions.IrInstructionType;

public class IrGetintInstruction extends IrInstruction {

    public IrGetintInstruction() {
        super(IrBuilder.LocalPrefix + IrBuilder.getTempVarNum(), IrInstructionType.GetIntInstr,null, null, null);
    }

    @Override
    public String toString() {
        return name + " = call i32 @getint()\n";
    }

    public void toMips() {
        super.toMips();

        new MarsLi(Register.V0, 5);
        new MipsSyscall();
        this.SaveRegisterResult(this, Register.V0);
    }
}
