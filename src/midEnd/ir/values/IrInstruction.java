package midEnd.ir.values;

import backend.mips.MipsBuilder;

import backend.mips.Register;
import backend.mips.assembly.MipsAnnotation;
import backend.mips.assembly.MipsLsu;
import backend.mips.assembly.pseudo.MarsLa;
import backend.mips.assembly.pseudo.MarsLi;
import backend.mips.assembly.pseudo.MarsMove;
import midEnd.ir.IrBuilder;
import midEnd.ir.IrValue;
import midEnd.ir.IrValueType;
import midEnd.ir.values.instructions.IrInstructionType;

public abstract class IrInstruction extends IrUser {
    private IrInstructionType instructionType;
    private IrBasicBlock parent;

    public IrInstruction(String name, IrInstructionType instructionType, IrValue value1,
                         IrValue value2, IrValue valueP) {
        this(name, instructionType, value1, value2, valueP, true);
    }

    public IrInstruction(String name, IrInstructionType instructionType, IrValue value1,
                         IrValue value2, IrValue valueP, boolean autoAdd) {
        super(name, IrValueType.Instr, value1, value2, valueP);
        this.instructionType = instructionType;
        // AST is traversed in postorder. So IrInstructions are created sequentially
        if (autoAdd) {
            this.parent = IrBuilder.getCurBasicBlock();
            IrBuilder.addInstr(this);
        }
    }

    public void setParent(IrBasicBlock parent) {
        this.parent = parent;
    }

    public IrInstructionType getInstructionType() {
        return instructionType;
    }

    public IrBasicBlock getParent() {
        return parent;
    }

    public void toMips() {
        new MipsAnnotation(this.toString());
    }

    protected void LoadValueToRegister(IrValue irValue, Register targetRegister) {
        if (irValue instanceof IrConstant irConstant) {
            new MarsLi(targetRegister, irConstant.getConstValue());
            return;
        }

        if (irValue instanceof IrGlobalValue irGlobalValue) {
            new MarsLa(targetRegister, irGlobalValue.getMipsLabel());
            return;
        }

        if (irValue instanceof midEnd.ir.values.instructions.IrAllocaInstruction) {
            Integer stackValueOffset = MipsBuilder.GetStackValueOffset(irValue);
            // if not allocated, allocate on stack
            if (stackValueOffset == null) {
                stackValueOffset = MipsBuilder.AllocateStackForValue(irValue);
            }
            new backend.mips.assembly.MipsAlu(backend.mips.assembly.MipsAlu.AluType.ADDI, targetRegister, Register.SP, stackValueOffset);
            return;
        }

        // already allocated
        Register valueRegister = MipsBuilder.GetValueToRegister(irValue);
        if (valueRegister != null) {
            new MarsMove(targetRegister, valueRegister);
            return;
        }

        Integer stackValueOffset = MipsBuilder.GetStackValueOffset(irValue);
        // if not allocated, allocate on stack
        if (stackValueOffset == null) {
            stackValueOffset = MipsBuilder.AllocateStackForValue(irValue);
        }
        new MipsLsu(MipsLsu.LsuType.LW, targetRegister, Register.SP, stackValueOffset);
    }

    protected Register GetRegisterOrK0ForValue(IrValue irValue) {
        Register register = MipsBuilder.GetValueToRegister(irValue);
        return register == null ? Register.K0 : register;
    }

    protected Register GetRegisterOrK1ForValue(IrValue irValue) {
        Register register = MipsBuilder.GetValueToRegister(irValue);
        return register == null ? Register.K1 : register;
    }

    /**
     * Save the result in a register, if not allocated, retain it on stack
     */
    protected void SaveRegisterResult(IrValue irValue, Register valueRegister) {
        Register register = MipsBuilder.GetValueToRegister(irValue);
        if (register == null) {
            int offset = MipsBuilder.AllocateStackForValue(irValue);
            new MipsLsu(MipsLsu.LsuType.SW, valueRegister, Register.SP, offset);
        } else {
            new MarsMove(register, valueRegister);
        }
    }
}
