package midEnd.ir.values.instructions;

public enum IrInstructionType {
    ReturnIntInstr,
    ReturnVoidInstr,
    BranchInstr,

    ArithmeticInstr,
    AllocateInstr,
    StoreInstr,
    LoadInstr,

    GlobalVariable,
}
