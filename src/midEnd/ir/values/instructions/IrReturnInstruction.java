package midEnd.ir.values.instructions;

import backend.mips.MipsBuilder;
import backend.mips.Register;
import backend.mips.assembly.MipsJump;
import backend.mips.assembly.pseudo.MarsMove;
import midEnd.ir.IrValue;
import midEnd.ir.values.IrInstruction;

public class IrReturnInstruction extends IrInstruction {
    public IrReturnInstruction(String name, IrInstructionType instructionType, IrValue useValue) {
        super(name, instructionType, null, null, useValue);
    }

    public IrReturnInstruction(String name, IrInstructionType instructionType) {
        super(name, instructionType, null, null, null);
    }

    @Override
    public String toString() {
        if (getObjectiveUseValue() == null) return "ret void\n";
        else return "ret i32 " + getObjectiveUseValue().getName() + "\n";
    }

    public void toMips() {
        super.toMips();

        IrValue returnValue = getObjectiveUseValue();
        if (returnValue != null) {
            Register returnRegister = MipsBuilder.GetValueToRegister(returnValue);
            if (returnRegister != null) {
                new MarsMove(Register.V0, returnRegister);
            }
            else {
                this.LoadValueToRegister(returnValue, Register.V0);
            }
        }
        new MipsJump(MipsJump.JumpType.JR, Register.RA);
    }
}
