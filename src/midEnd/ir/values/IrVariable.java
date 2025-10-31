package midEnd.ir.values;

import midEnd.ir.IrBuilder;
import midEnd.ir.IrValue;
import midEnd.ir.IrValueType;

/**
 * Just as a placeholder
 * Used as both variable for lval (both local & global) & function formal parameter.
 * Actually a LLVM IR pointer type
 */
public class IrVariable extends IrValue {
    public IrVariable(String name, boolean isGlobal) {
        super((isGlobal ? IrBuilder.GlobalPrefix : IrBuilder.LocalPrefix) + name, IrValueType.Variable);
    }

    @Override
    public String toString() {
        return name;
    }
}
