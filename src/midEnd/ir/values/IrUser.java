package midEnd.ir.values;

import midEnd.ir.IrValue;
import midEnd.ir.IrValueType;

import java.util.ArrayList;

public abstract class IrUser extends IrValue {
    protected IrValue useValue1;
    protected IrValue useValue2;
    protected IrValue useValueP;

    public IrUser(String name, IrValueType valueType, IrValue value1, IrValue value2, IrValue valueP) {
        super(name, valueType);
        this.useValue1 = value1;
        this.useValue2 = value2;
        this.useValueP = valueP;
        if (value1 != null) value1.addUser(this);
        if (value2 != null) value2.addUser(this);
        if (valueP != null) valueP.addUser(this);
    }

    public IrValue getFirstUseValue() {
        return useValue1;
    }

    public IrValue getSecondUseValue() {
        return useValue2;
    }

    public IrValue getObjectiveUseValue() {
        return useValueP;
    }

    public void setUseValue1(IrValue useValue1) {
        this.useValue1 = useValue1;
    }

    public void setUseValue2(IrValue useValue2) {
        this.useValue2 = useValue2;
    }

    public void setUseValueP(IrValue useValueP) {
        this.useValueP = useValueP;
    }
}
