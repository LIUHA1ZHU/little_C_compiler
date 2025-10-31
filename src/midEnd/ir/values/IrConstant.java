package midEnd.ir.values;

import midEnd.ir.IrValue;
import midEnd.ir.IrValueType;

public class IrConstant extends IrValue {
    private int constValue;

    public IrConstant(int constValue) {
        super(Integer.toString(constValue), IrValueType.ConstantValue);
        this.constValue = constValue;
    }

    @Override
    public String toString() {
        return "i32 " + constValue;
    }
}
