package midEnd.ir.values.instructions.phi;

import backend.mips.Register;
import midEnd.ir.IrValue;
import midEnd.ir.values.IrBasicBlock;
import midEnd.ir.values.IrInstruction;
import midEnd.ir.values.instructions.IrInstructionType;

public class MoveInstr extends IrInstruction {

    public MoveInstr(IrValue dstValue, IrValue srcValue, IrBasicBlock parent) {
        super("move", IrInstructionType.MoveInstr, srcValue, dstValue, null, false);
        this.setParent(parent);
    }

    public IrValue getSrcValue() {
        return getFirstUseValue();
    }

    public void setSrcValue(IrValue srcValue) {
        setUseValue1(srcValue);
    }

    public IrValue getDstValue() {
        return getSecondUseValue();
    }

    public void setDstValue(IrValue dstValue) {
        setUseValue2(dstValue);
    }

    @Override
    public String toString() {
        return "move " + getDstValue().getName() + ", " + getSrcValue().getName();
    }

    @Override
    public void toMips() {
        super.toMips();
        IrValue src = getSrcValue();
        IrValue dst = getDstValue();
        
        Register dstReg = GetRegisterOrK0ForValue(dst);
        LoadValueToRegister(src, dstReg);
        SaveRegisterResult(dst, dstReg);
    }
}
