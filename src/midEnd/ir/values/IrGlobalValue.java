package midEnd.ir.values;

import midEnd.ir.IrValue;
import midEnd.ir.IrValueType;

public abstract class IrGlobalValue extends IrValue {
    public IrGlobalValue(String name, IrValueType valueType) {
        super(name, valueType);
    }
}
