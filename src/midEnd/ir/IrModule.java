package midEnd.ir;

import midEnd.ir.values.IrGlobalValue;

import java.util.ArrayList;

public class IrModule {
    private final ArrayList<IrGlobalValue> globalValues;

    public IrModule() {
        this.globalValues = new ArrayList<>();
    }

    public void addGlobalValue(IrGlobalValue globalValue) {
        globalValues.add(globalValue);
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (IrGlobalValue globalValue : globalValues) {
            sb.append(globalValue.toString()).append("\n");
        }
        return sb.toString();
    }

}
