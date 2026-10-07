package midEnd.ir.values;

import midEnd.ir.IrValue;
import midEnd.ir.IrValueType;

public class IrVirtualValue extends IrValue {
    public IrVirtualValue(IrValueType valueType, String name) {
        super(name, valueType);
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public void toMips() {
        // Virtual value doesn't emit code directly, it's used as operand
    }
}
