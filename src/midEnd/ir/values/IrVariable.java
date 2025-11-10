package midEnd.ir.values;

import midEnd.ir.IrBuilder;
import midEnd.ir.IrValue;
import midEnd.ir.IrValueType;

/**
 * function formal parameter.
 * Actually a LLVM IR pointer type
 */
public class IrVariable extends IrValue {
    private int length;
    private boolean isArray;
    private boolean variableLength;

    private boolean isFuncFormal = true;

    public IrVariable(String name, boolean isGlobal, boolean isFuncFormal) {
        super((isGlobal ? IrBuilder.GlobalPrefix : IrBuilder.LocalPrefix) + name, IrValueType.Variable);
        this.length = 1;
        this.isArray = false;
        this.variableLength = false;
        this.isFuncFormal = isFuncFormal;
    }

    public IrVariable(String name, boolean isGlobal, int length, boolean variableLength) {
        super((isGlobal ? IrBuilder.GlobalPrefix : IrBuilder.LocalPrefix) + name, IrValueType.Variable);
        this.length = length;
        this.isArray = true;
        this.variableLength = variableLength;
        this.isFuncFormal = variableLength;
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

    public boolean isFuncFormal() {
        return isFuncFormal;
    }

    @Override
    public String toString() {
        return name;
    }
}
