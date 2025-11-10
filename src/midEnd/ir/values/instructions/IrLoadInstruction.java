package midEnd.ir.values.instructions;

import midEnd.ir.IrBuilder;
import midEnd.ir.IrValue;
import midEnd.ir.IrValueType;
import midEnd.ir.values.IrInstruction;

public class IrLoadInstruction extends IrInstruction {
    /**
     * Just the name matters
     */
    public IrLoadInstruction(IrValue memPtr) {
        super(IrBuilder.LocalPrefix + IrBuilder.getTempVarNum(), IrInstructionType.LoadInstr, null, null, memPtr);
    }

    @Override
    public String toString() {
        return getName() + " = load i32, i32* " + getObjectiveUseValue().getName() + ", align 4\n";
    }
}
