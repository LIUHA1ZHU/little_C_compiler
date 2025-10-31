package midEnd.ir.values;

import midEnd.ir.IrValueType;

import java.util.ArrayList;

public class IrFunction extends IrGlobalValue {
    public enum IrFunctionType {
        voidFunc,
        intFunc,
    }
    private final IrFunctionType irFunctionType;
    private final ArrayList<IrBasicBlock> basicBlocks;
    private final ArrayList<IrVariable> parameters;

    public IrFunction(String name, IrFunctionType functionType, ArrayList<IrVariable> parameters) {
        super(name, IrValueType.Function);
        this.irFunctionType = functionType;
        this.basicBlocks = new ArrayList<>();
        this.parameters = parameters == null ? new ArrayList<>() : parameters;
    }

    public void addBasicBlock(IrBasicBlock irBasicBlock) {
        basicBlocks.add(irBasicBlock);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (name.equals("main")) {
             sb.append("define dso_local i32 @main() {\n");
        }
        //TODO other func
        for (IrBasicBlock basicBlock : basicBlocks) {
            sb.append(basicBlock.toString());
        }
        return sb.append("}\n").toString();
    }
}
