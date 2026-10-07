package midEnd.ir;

import backend.mips.Register;
import backend.mips.assembly.MipsAnnotation;
import backend.mips.assembly.MipsJump;
import backend.mips.assembly.MipsLabel;
import backend.mips.assembly.MipsSyscall;
import backend.mips.assembly.pseudo.MarsLi;
import midEnd.ir.values.IrFunction;
import midEnd.ir.values.IrGlobalValue;

import java.util.ArrayList;
import java.util.Map;

public class IrModule {
    private final ArrayList<IrGlobalValue> globalValues;

    public IrModule() {
        this.globalValues = new ArrayList<>();
    }

    public void addGlobalValue(IrGlobalValue globalValue) {
        globalValues.add(globalValue);
    }

    public ArrayList<IrGlobalValue> getGlobalValues() {
        return globalValues;
    }

    public ArrayList<IrFunction> getFunctions() {
        ArrayList<IrFunction> functions = new ArrayList<>();
        for (IrGlobalValue globalValue : globalValues) {
            if (globalValue instanceof IrFunction) {
                functions.add((IrFunction) globalValue);
            }
        }
        return functions;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (IrGlobalValue globalValue : globalValues) {
            sb.append(globalValue.toString()).append("\n");
        }
        return sb.toString();
    }

    public void toMips() {
        for (IrGlobalValue globalValue : this.globalValues) {
            if (!(globalValue instanceof IrFunction)) globalValue.toMips();
        }

        // jump to main
        new MipsAnnotation("jump to main");
        new MipsJump(MipsJump.JumpType.JAL, "main");
        new MarsLi(Register.V0, 10);
        new MipsSyscall();

        for (IrGlobalValue globalValue : this.globalValues) {
            if (globalValue instanceof IrFunction) globalValue.toMips();
        }
    }
}
