package midEnd.ir.values.instructions.IOInstructions;

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
}
