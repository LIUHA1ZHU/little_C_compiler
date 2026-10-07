package midEnd.ir.values.instructions;

public enum IrInstructionType {
    ReturnIntInstr,
    ReturnVoidInstr,
    BranchInstr,
    PhiInstr,
    ParallelCopy,
    MoveInstr,

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

    public String getCallValueType() {
        if (this == GEPInstr || this == AllocateInstr) return "i32 *";
        else return "i32";
    }
}
