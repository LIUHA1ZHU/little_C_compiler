package backend.mips;

import backend.mips.assembly.MipsAssembly;
import backend.mips.assembly.data.MipsData;
import midEnd.ir.IrValue;
import midEnd.ir.values.IrFunction;
import midEnd.ir.values.IrVariable;
import utils.FileIO;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class MipsBuilder {
    private static MipsModule currentModule = null;

    private static HashMap<IrValue, Register> valueRegisterMap = null;

    private static int stackOffset = 0;
    private static HashMap<IrValue, Integer> stackOffsetValueMap = null;

    public static void SetBackEndModule(MipsModule mipsModule) {
        currentModule = mipsModule;
    }

    public static MipsModule GetCurrentModule() {
        return currentModule;
    }

    public static void AddAssembly(MipsAssembly mipsAssembly) {
        if (mipsAssembly instanceof MipsData) {
            currentModule.AddToData(mipsAssembly);
        } else {
            currentModule.AddToText(mipsAssembly);
        }
    }

    public static void SetCurrentFunction(IrFunction irFunction) {
        valueRegisterMap = irFunction.getValueRegisterMap();
        stackOffset = 0;
        stackOffsetValueMap = new HashMap<>();
    }

    public static Register GetValueToRegister(IrValue irValue) {
        return valueRegisterMap.get(irValue);
    }

    public static void AllocateRegForParam(IrVariable irVariable, Register register) {
        valueRegisterMap.put(irVariable, register);
    }

    public static ArrayList<Register> GetAllocatedRegList() {
        ArrayList<Register> list = new ArrayList<>(new HashSet<>(valueRegisterMap.values()));
        // Ensure deterministic order for stack layout consistency
        list.sort(java.util.Comparator.comparingInt(Enum::ordinal));
        return list;
    }

    public static int GetCurrentStackOffset() {
        return stackOffset;
    }

    public static Integer GetStackValueOffset(IrValue irValue) {
        return stackOffsetValueMap.get(irValue);
    }

    public static Integer AllocateStackForValue(IrValue irValue) {
        Integer address = stackOffsetValueMap.get(irValue);
        if (address == null) {
            stackOffset -= 4;
            stackOffsetValueMap.put(irValue, stackOffset);
            address = stackOffset;
        }

        return address;
    }

    public static void SetStackValueOffset(IrValue irValue, int offset) {
        stackOffsetValueMap.put(irValue, offset);
    }

    public static void AllocateStackSpace(int offset) {
        stackOffset -= offset;
    }

    public static void outputMips() {
        FileIO.write(FileIO.IOType.MIPS, currentModule.toString());
    }
}
