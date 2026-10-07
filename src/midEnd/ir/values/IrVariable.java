package midEnd.ir.values;

import midEnd.ir.IrBuilder;
import midEnd.ir.IrValue;
import midEnd.ir.IrValueType;

/**
 * Only used in function formal parameter.
 */
public class IrVariable extends IrValue {
    private int length;
    private boolean isArray;
    private boolean variableLength;

    public IrVariable(String name, boolean isGlobal) {
        super((isGlobal ? IrBuilder.GlobalPrefix : IrBuilder.LocalPrefix) + name, IrValueType.Variable);
        this.length = 1;
        this.isArray = false;
        this.variableLength = false;
    }

    public IrVariable(String name, boolean isGlobal, int length, boolean variableLength) {
        super((isGlobal ? IrBuilder.GlobalPrefix : IrBuilder.LocalPrefix) + name, IrValueType.Variable);
        this.length = length;
        this.isArray = true;
        this.variableLength = variableLength;
    }

    public boolean isArray() {
        return isArray;
    }

    public int getLength() {
        return length;
    }

    public boolean isVariableLength() {
        return variableLength;
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public void toMips() {
        throw new RuntimeException("parameters shouldn't be translated");
    }
}
