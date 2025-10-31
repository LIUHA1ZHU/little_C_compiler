package midEnd.ir.values;

import midEnd.ir.IrBuilder;
import midEnd.ir.IrValue;
import midEnd.ir.IrValueType;

public class IrGlobalVariable extends IrGlobalValue{
    private IrValue initVal;
    public IrGlobalVariable(String name, IrValue initVal) {
        super(IrBuilder.GlobalPrefix + name, IrValueType.GlobalVariable);
        this.initVal = initVal;
        IrBuilder.addGlobalVariable(this);
    }

    @Override
    public String toString() {
        return name + " = dso_local global i32 " + initVal.getName() + ", align 4\n";
    }
}
