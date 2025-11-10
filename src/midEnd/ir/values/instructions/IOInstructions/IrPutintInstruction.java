package midEnd.ir.values.instructions.IOInstructions;

import midEnd.ir.IrValue;
import midEnd.ir.values.IrInstruction;
import midEnd.ir.values.instructions.IrInstructionType;

public class IrPutintInstruction extends IrInstruction {
    public IrPutintInstruction(IrValue valueToPut) {
        super(valueToPut.getName(), IrInstructionType.PutIntInstr, valueToPut, null, null);
    }

    @Override
    public String toString() {
        return "call void @putint(i32 " + getFirstUseValue().getName() + ")\n";
    }
}
