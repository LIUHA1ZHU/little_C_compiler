package midEnd.ir.values;

import midEnd.ir.IrValue;
import midEnd.ir.IrValueType;
import midEnd.ir.values.instructions.IrInstructionType;

import java.util.ArrayList;

public class IrBasicBlock extends IrValue {
    private final ArrayList<IrInstruction> instructions;

    public IrBasicBlock(String name) {
        super(name, IrValueType.BasicBlock);
        this.instructions = new ArrayList<>();
    }

    public void addInstr(IrInstruction instruction) {
        instructions.add(instruction);
    }

    public boolean notEndWithTerminator() {
        if (instructions.isEmpty()) return true;
        IrInstructionType lastInstrType = instructions.get(instructions.size() - 1).getInstructionType();
        return !(lastInstrType.equals(IrInstructionType.ReturnVoidInstr) || lastInstrType.equals(IrInstructionType.ReturnIntInstr)
                || lastInstrType.equals(IrInstructionType.BranchInstr));
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(name).append(":\n");
        for (IrInstruction instruction : instructions) {
            sb.append("\t").append(instruction);
        }
        return sb.append("\n").toString();
    }
}
