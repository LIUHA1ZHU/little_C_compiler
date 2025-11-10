package midEnd.ir.values;

import midEnd.ir.IrBuilder;
import midEnd.ir.IrValue;
import midEnd.ir.IrValueType;

import java.util.ArrayList;

public class IrGlobalVariable extends IrGlobalValue{
    private int length;
    private ArrayList<IrValue> initVals;
    private boolean isArray;

    /**
     * InitVals OF global values MUST BE CONSTANT
     */
    public IrGlobalVariable(String name, int length, ArrayList<IrValue> initVals, boolean isArray) {
        super(IrBuilder.GlobalPrefix + name, IrValueType.GlobalVariable);
        this.length = length;
        this.initVals = initVals;
        this.isArray = isArray;
        if (initVals.size() > length) throw new RuntimeException("WARNING: too much initVals in global variable");
        IrBuilder.addGlobalVariable(this);
    }

    private boolean allZero() {
        boolean allZero = true;
        for (IrValue val : initVals) {
            if (!(val instanceof IrConstant)) {
                allZero = false;
                break;
            }
            if (((IrConstant) val).getConstValue() != 0) {
                allZero = false;
                break;
            }
        }
        return allZero;
    }

    private boolean hasNonConstant() {
        boolean hasNonConstant = false;
        for (IrValue val : initVals) {
            if (!(val instanceof IrConstant)) {
                hasNonConstant = true;
                break;
            }
        }
        return hasNonConstant;
    }

    public int getLength() {
        return length;
    }

    public boolean isArray() {
        return isArray;
    }

    @Override
    public String toString() {
        if (!isArray) {
            if (!initVals.isEmpty() && initVals.get(0) instanceof IrConstant) {
                return name + " = dso_local global i32 " + ((IrConstant) initVals.get(0)).getConstValue() + ", align 4\n";
            } else {
                return name + " = dso_local global i32 0, align 4\n";
            }
        } else { // global array
            StringBuilder sb = new StringBuilder();
            sb.append(name).append(" = dso_local global ");
            sb.append("[").append(length).append(" x i32] ");
            if (allZero() || hasNonConstant()) {
                sb.append("zeroinitializer\n");
            } else {
                sb.append("[");
                for (int i = 0; i < initVals.size(); i++) {
                    IrValue val = initVals.get(i);
                    sb.append("i32 ").append(((IrConstant) val).getConstValue());
                    if (i != initVals.size() - 1) sb.append(",");
                }
                sb.append("]\n");
            }
            return sb.toString();
        }
    }
}
