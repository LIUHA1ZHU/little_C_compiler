package midEnd.ir.values.instructions;

import midEnd.ir.IrBuilder;
import midEnd.ir.IrValue;
import midEnd.ir.IrValueType;
import midEnd.ir.values.IrInstruction;

/**
 * firstUseValue: to be stored.
 * objectiveUseValue: a ptr
 */
public class IrStoreInstruction extends IrInstruction {
    public IrStoreInstruction(String name, IrValue value1, IrValue valueP) {
        super(name, IrInstructionType.StoreInstr, value1, null, valueP);
    }

    @Override
    public String toString() {
        return "store i32 " + getFirstUseValue().getName() + ", ptr " +  getObjectiveUseValue().getName() + ", align 4\n";
    }
}
