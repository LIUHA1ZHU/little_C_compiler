package midEnd.ir.values.instructions.phi;

import midEnd.ir.IrBuilder;
import midEnd.ir.IrValue;
import midEnd.ir.values.IrBasicBlock;
import midEnd.ir.values.IrInstruction;
import midEnd.ir.values.instructions.IrInstructionType;

import java.util.ArrayList;

public class ParallelCopyInstr extends IrInstruction {
    private final ArrayList<IrValue> srcList;
    private final ArrayList<IrValue> dstList;

    public ParallelCopyInstr(IrBasicBlock irBasicBlock) {
        super(IrBuilder.LocalPrefix + "parallel-copy", IrInstructionType.ParallelCopy, null, null, null, false);
        this.srcList = new ArrayList<>();
        this.dstList = new ArrayList<>();
        this.setParent(irBasicBlock);
    }

    public void addCopy(IrValue src, IrValue dst) {
        this.srcList.add(src);
        this.dstList.add(dst);
    }

    public ArrayList<IrValue> getSrcList() {
        return this.srcList;
    }

    public ArrayList<IrValue> getDstList() {
        return this.dstList;
    }

    @Override
    public String toString() {
        return "parallel-copy-instr";
    }

    @Override
    public void toMips() {
        throw new RuntimeException("pcopy not delete!");
    }
}
