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
        if (this.useValue1 != null) {
            this.useValue1.removeUser(this);
        }
        this.useValue1 = useValue1;
        if (this.useValue1 != null) {
            this.useValue1.addUser(this);
        }
    }

    public void setUseValue2(IrValue useValue2) {
        if (this.useValue2 != null) {
            this.useValue2.removeUser(this);
        }
        this.useValue2 = useValue2;
        if (this.useValue2 != null) {
            this.useValue2.addUser(this);
        }
    }

    public void setUseValueP(IrValue useValueP) {
        if (this.useValueP != null) {
            this.useValueP.removeUser(this);
        }
        this.useValueP = useValueP;
        if (this.useValueP != null) {
            this.useValueP.addUser(this);
        }
    }

    public void replaceUse(IrValue oldVal, IrValue newVal) {
        if (useValue1 == oldVal) {
            useValue1 = newVal;
            oldVal.removeUser(this);
            newVal.addUser(this);
        }
        if (useValue2 == oldVal) {
            useValue2 = newVal;
            oldVal.removeUser(this);
            newVal.addUser(this);
        }
        if (useValueP == oldVal) {
            useValueP = newVal;
            oldVal.removeUser(this);
            newVal.addUser(this);
        }
    }

    public void removeAllValueUse() {
        if (useValue1 != null) {
            useValue1.removeUser(this);
            useValue1 = null;
        }
        if (useValue2 != null) {
            useValue2.removeUser(this);
            useValue2 = null;
        }
        if (useValueP != null) {
            useValueP.removeUser(this);
            useValueP = null;
        }
    }
}
