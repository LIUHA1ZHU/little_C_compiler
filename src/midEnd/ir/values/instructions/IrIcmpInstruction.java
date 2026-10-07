package midEnd.ir.values.instructions;

import backend.mips.Register;
import backend.mips.assembly.MipsCompare;
import midEnd.ir.IrBuilder;
import midEnd.ir.IrValue;
import midEnd.ir.values.IrConstant;
import midEnd.ir.values.IrInstruction;
import utils.Config;

// %result = icmp cond i32 %value1, %value2
public class IrIcmpInstruction extends IrInstruction {
    public enum IcmpCondType {
        sgt, sge, slt, sle, eq, ne
    }
    private IcmpCondType icmpCondType;

    public IrIcmpInstruction(IcmpCondType icmpCondType, IrValue value1, IrValue value2) {
        super(IrBuilder.LocalPrefix + IrBuilder.getTempVarNum(), IrInstructionType.IcmpInstr, value1, value2, null);
        this.icmpCondType = icmpCondType;
    }

    public IrIcmpInstruction(IcmpCondType icmpCondType, IrValue value1, IrValue value2, boolean autoAdd) {
        super(IrBuilder.LocalPrefix + IrBuilder.getTempVarNum(), IrInstructionType.IcmpInstr, value1, value2, null, autoAdd);
        this.icmpCondType = icmpCondType;
    }

    public IcmpCondType getIcmpCondType() {
        return icmpCondType;
    }

    @Override
    public String toString() {
        return name + " = icmp " + icmpCondType + " i32 " + getFirstUseValue().getName() + ", " + getSecondUseValue().getName() + "\n";
    }

    public void toMips() {
        super.toMips();

        IrValue valueL = getFirstUseValue();
        IrValue valueR = getSecondUseValue();

        Register registerL = this.GetRegisterOrK0ForValue(valueL);
        Register registerR = this.GetRegisterOrK1ForValue(valueR);
        Register registerResult = this.GetRegisterOrK0ForValue(this);

        if (Config.optimize) {
            if (valueR instanceof IrConstant irConstantR) {
                this.LoadValueToRegister(valueL, registerL);
                this.GenerateMipsCompareInstr(registerL, irConstantR, registerResult);
            } else {
                // load
                this.LoadValueToRegister(valueL, registerL);
                this.LoadValueToRegister(valueR, registerR);

                // 生成计算指令
                this.GenerateMipsCompareInstr(registerL, registerR, registerResult);
            }
        } else {
            // 加载数据
            this.LoadValueToRegister(valueL, registerL);
            this.LoadValueToRegister(valueR, registerR);

            // 生成计算指令
            this.GenerateMipsCompareInstr(registerL, registerR, registerResult);
        }

        // 如果没有寄存器保留结果，则应该把结果存到栈上
        this.SaveRegisterResult(this, registerResult);
    }

    private void GenerateMipsCompareInstr(Register registerL, Register registerR,
                                          Register registerResult) {
        switch (this.icmpCondType) {
            case eq ->
                    new MipsCompare(MipsCompare.CompareType.SEQ, registerResult, registerL, registerR);
            case ne ->
                    new MipsCompare(MipsCompare.CompareType.SNE, registerResult, registerL, registerR);
            case sgt ->
                    new MipsCompare(MipsCompare.CompareType.SGT, registerResult, registerL, registerR);
            case sge ->
                    new MipsCompare(MipsCompare.CompareType.SGE, registerResult, registerL, registerR);
            case slt ->
                    new MipsCompare(MipsCompare.CompareType.SLT, registerResult, registerL, registerR);
            case sle ->
                    new MipsCompare(MipsCompare.CompareType.SLE, registerResult, registerL, registerR);
            default -> throw new RuntimeException("illegal compare op");
        }
    }

    private void GenerateMipsCompareInstr(
            Register registerL, IrConstant irConstant, Register registerResult) {
        int immediate = irConstant.getConstValue();
        switch (this.icmpCondType) {
            case eq -> new MipsCompare(MipsCompare.CompareType.SEQ,
                    registerResult, registerL, immediate);
            case ne -> new MipsCompare(MipsCompare.CompareType.SNE,
                    registerResult, registerL, immediate);
            case sgt -> new MipsCompare(MipsCompare.CompareType.SGT,
                    registerResult, registerL, immediate);
            case sge -> new MipsCompare(MipsCompare.CompareType.SGE,
                    registerResult, registerL, immediate);
            case slt -> new MipsCompare(MipsCompare.CompareType.SLTI,
                    registerResult, registerL, immediate);
            case sle -> new MipsCompare(MipsCompare.CompareType.SLE,
                    registerResult, registerL, immediate);
            default -> throw new RuntimeException("illegal compare op");
        }
    }
}
