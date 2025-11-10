package midEnd.ir.values.instructions;

import java.util.ArrayList;
import java.util.Arrays;

public enum IrInstructionType {
    ReturnIntInstr,
    ReturnVoidInstr,
    BranchInstr,

    ArithmeticInstr,
    AllocateInstr,
    IcmpInstr,
    ExtendInstr,
    StoreInstr,
    LoadInstr,
    CallInstr,

    GetIntInstr,
    PutIntInstr,
    PutStrInstr,

    GEPInstr,

    GlobalVariable;

    public String getValueType() {
        if (this == GEPInstr || this == AllocateInstr) return "i32 *";
        else return "i32";
    }
}
