package midEnd.ir.values;

import midEnd.ir.IrValue;
import midEnd.ir.IrValueType;

public class IrConstant extends IrValue {
    private final int constValue;

    public IrConstant(int constValue) {
        super(Integer.toString(constValue), IrValueType.ConstantValue);
        this.constValue = constValue;
    }

    public int getConstValue() {
        return constValue;
    }

    @Override
    public String toString() {
        return "i32 " + constValue;
    }

    @Override
    public void toMips() {
        throw new RuntimeException("constant shouldn't be translated");
    }
}
