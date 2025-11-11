package midEnd.ir.values;

import midEnd.ir.IrBuilder;
import midEnd.ir.IrValueType;

import java.util.ArrayList;

public class IrFunction extends IrGlobalValue {
    public enum IrFunctionType {
        voidFunc,
        intFunc,
    }
    private final IrFunctionType irFunctionType;
    private final ArrayList<IrBasicBlock> basicBlocks;
    private ArrayList<IrVariable> parameters;

    public IrFunction(String name, IrFunctionType functionType) {
        super(name, IrValueType.Function);
        this.irFunctionType = functionType;
        this.basicBlocks = new ArrayList<>();
        this.parameters = null;
    }

    public IrFunctionType getIrFunctionType() {
        return irFunctionType;
    }

    public void setParameters(ArrayList<IrVariable> parameters) {
        this.parameters = parameters;
    }

    public ArrayList<IrBasicBlock> getBasicBlocks() {
        return basicBlocks;
    }

    public ArrayList<IrVariable> getParameters() {
        return parameters;
    }

    public void addBasicBlock(IrBasicBlock irBasicBlock) {
        basicBlocks.add(irBasicBlock);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (name.equals("main")) {
             sb.append("define dso_local i32 @main() {\n");
        } else {
            if (irFunctionType.equals(IrFunctionType.voidFunc)) {
                sb.append("define dso_local void @").append(name);
            } else {
                sb.append("define dso_local i32 @").append(name);
            }

            sb.append("(");
            for (int i = 0; i < parameters.size(); i++) {
                IrVariable var = parameters.get(i);
                sb.append(var.isArray() ? "i32* " : "i32 ").append(var.getName());
                if (i != parameters.size() -1) sb.append(", ");
            }
            sb.append(")");

            sb.append(" {\n");

        }
        for (IrBasicBlock basicBlock : basicBlocks) {
            sb.append(basicBlock.toString());
        }
        return sb.append("}\n").toString();
    }
}
