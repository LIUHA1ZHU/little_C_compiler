package midEnd.ir.values.instructions.IOInstructions;

import backend.mips.Register;
import backend.mips.assembly.MipsSyscall;
import backend.mips.assembly.pseudo.MarsLa;
import backend.mips.assembly.pseudo.MarsLi;
import midEnd.ir.IrBuilder;
import midEnd.ir.IrValue;
import midEnd.ir.values.IrInstruction;
import midEnd.ir.values.IrStringConstant;
import midEnd.ir.values.instructions.IrInstructionType;

public class IrPutstrInstruction extends IrInstruction {

    public IrPutstrInstruction(IrStringConstant stringConstant) {
        super(stringConstant.getName(), IrInstructionType.PutStrInstr, stringConstant, null, null);
    }

    @Override
    public String toString() {
        int len = ((IrStringConstant) getFirstUseValue()).getLength();
        return "call void @putstr(i8* getelementptr inbounds ([" + len + " x i8], [" + len + " x i8]* "
                + getFirstUseValue().getName() + ", i64 0, i64 0))\n";
    }

    public void toMips() {
        super.toMips();

        new MarsLa(Register.A0, getFirstUseValue().getMipsName());
        new MarsLi(Register.V0, 4);
        new MipsSyscall();
    }
}
