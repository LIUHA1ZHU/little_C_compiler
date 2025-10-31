package midEnd.ir.values;

import midEnd.ir.IrBuilder;
import midEnd.ir.IrValue;
import midEnd.ir.IrValueType;
import midEnd.ir.values.instructions.IrInstructionType;

public abstract class IrInstruction extends IrUser {
    private IrInstructionType instructionType;

    public IrInstruction(String name, IrInstructionType instructionType, IrValue value1, IrValue value2, IrValue valueP) {
        super(name, IrValueType.Instr, value1, value2, valueP);
        this.instructionType = instructionType;
        // AST is traversed in postorder. So IrInstructions are created sequentially
        IrBuilder.addInstr(this);
    }

    public IrInstructionType getInstructionType() {
        return instructionType;
    }
}
