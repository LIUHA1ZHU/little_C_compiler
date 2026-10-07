package midEnd.ir.values.instructions;

import backend.mips.Register;
import backend.mips.assembly.MipsAlu;
import midEnd.ir.IrBuilder;
import midEnd.ir.IrValue;
import midEnd.ir.values.IrConstant;
import midEnd.ir.values.IrGlobalVariable;
import midEnd.ir.values.IrInstruction;
import midEnd.ir.values.IrVariable;

// %result = getelementptr inbounds [length x i32], [length x i32]* %array, i32 0, i32 %index
// %result = getelementptr inbounds i32, i32* %ptr, i32 %index
public class IrGEPInstruction extends IrInstruction {

    public IrGEPInstruction(IrValue src, IrValue index) {
        super(IrBuilder.LocalPrefix + IrBuilder.getTempVarNum(), IrInstructionType.GEPInstr, src, index, null);
    }

    @Override
    public String toString() {
        if (getFirstUseValue() instanceof IrVariable && ((IrVariable) getFirstUseValue()).isVariableLength()) {
            return name + " = getelementptr inbounds i32, i32* " + getFirstUseValue().getName() + ", i32 " + getSecondUseValue().getName() + "\n";
        } else {
            int length = getLength();
            String valueName = getFirstUseValue().getName();
            return name + " = getelementptr inbounds [" + length + " x i32], [" + length +
                    " x i32]* " + valueName + ", i32 0, i32 " + getSecondUseValue().getName() + "\n";
        }
    }

    private int getLength() {
        int length;
        if (getFirstUseValue() instanceof IrAllocaInstruction) {
            length = ((IrAllocaInstruction) getFirstUseValue()).getLength();
        } else if (getFirstUseValue() instanceof IrGlobalVariable) {
            length = ((IrGlobalVariable) getFirstUseValue()).getLength();
        }  else throw new RuntimeException("gep invalid IrValue" + getFirstUseValue().getValueType());
        return length;
    }

    public void toMips() {
        super.toMips();

        IrValue pointerValue = getFirstUseValue();
        IrValue offsetValue = getSecondUseValue();

        Register pointerRegister = this.GetRegisterOrK0ForValue(pointerValue);
        // 一定要都是k0或k1，不然有覆盖问题
        Register offsetRegister = this.GetRegisterOrK1ForValue(offsetValue);
        Register targetRegister = this.GetRegisterOrK1ForValue(this);

        // 加载数组的首地址
        this.LoadValueToRegister(pointerValue, pointerRegister);
        if (offsetValue instanceof IrConstant irConstant) {
            // 直接赋值
            new MipsAlu(MipsAlu.AluType.ADDIU, targetRegister, pointerRegister,
                    irConstant.getConstValue() << 2);
        } else {
            // 加载offset的值
            this.LoadValueToRegister(offsetValue, offsetRegister);
            // 将offset左移两位
            new MipsAlu(MipsAlu.AluType.SLL, targetRegister, offsetRegister, 2);
            new MipsAlu(MipsAlu.AluType.ADDU, targetRegister, pointerRegister, targetRegister);
        }

        // 保存结果
        this.SaveRegisterResult(this, targetRegister);
    }
}
