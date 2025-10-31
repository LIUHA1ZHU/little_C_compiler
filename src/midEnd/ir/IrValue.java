package midEnd.ir;

public abstract class IrValue {
    protected String name;
    protected IrValueType valueType;

    public IrValue(String name, IrValueType valueType) {
        this.name = name;
        this.valueType = valueType;
    }

    public IrValueType getValueType() {
        return valueType;
    }

    public String getName() {
        return name;
    }

    public abstract String toString();
}
