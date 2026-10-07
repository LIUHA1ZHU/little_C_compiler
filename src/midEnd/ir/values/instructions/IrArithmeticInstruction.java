package midEnd.ir.values.instructions;


import backend.mips.Register;
import backend.mips.assembly.MipsAlu;
import backend.mips.assembly.MipsMdu;
import midEnd.ir.IrBuilder;
import midEnd.ir.IrValue;
import midEnd.ir.values.IrInstruction;

// %result = op i32 %value1, %value2
public class IrArithmeticInstruction extends IrInstruction {
    public enum IrArithmeticType {
        add("add nsw i32 "),
        sub("sub i32 "),
        mul("mul nsw i32 "),
        sdiv("sdiv i32 "),
        srem("srem i32 "),
        shl("shl i32 "),
        lshr("lshr i32 "),
        ashr("ashr i32 "),
        mulh("mulh i32 "); // Special internal instruction for high 32 bits of multiplication

        private String string;

        IrArithmeticType(String string) {
            this.string = string;
        }

        public String getString() {
            return string;
        }
    }

    private IrArithmeticType arithmeticType;

    public IrArithmeticInstruction(IrArithmeticType arithmeticType, IrValue value1, IrValue value2) {
        super(IrBuilder.LocalPrefix + IrBuilder.getTempVarNum(), IrInstructionType.ArithmeticInstr, value1, value2, null);
        this.arithmeticType = arithmeticType;
    }

    public IrArithmeticInstruction(IrArithmeticType arithmeticType, IrValue value1, IrValue value2, boolean autoAdd) {
        super(IrBuilder.LocalPrefix + IrBuilder.getTempVarNum(), IrInstructionType.ArithmeticInstr, value1, value2, null, autoAdd);
        this.arithmeticType = arithmeticType;
    }

    public IrArithmeticType getArithmeticType() {
        return arithmeticType;
    }

    @Override
    public String toString() {
        return name + " = " + arithmeticType.getString() + getFirstUseValue().getName() + ", " + getSecondUseValue().getName() + "\n";
    }

    public void toMips() {
        super.toMips();

        IrValue valueL = this.GetValueL();
        IrValue valueR = this.GetValueR();

        Register registerL = this.GetRegisterOrK0ForValue(valueL);
        Register registerR = this.GetRegisterOrK1ForValue(valueR);
        // 为计算结果分配寄存器
        Register registerResult = this.GetRegisterOrK0ForValue(this);

//        if (Setting.FINE_TUNING) {
//            // 如果是乘除指令
//            if (this.IsMduInstr()) {
//                switch (this.aluOp) {
//                    case MUL ->
//                            this.MulOptimize(valueL, valueR, registerL, registerR, registerResult);
//                    case SDIV ->
//                            this.DivOptimize(valueL, valueR, registerL, registerR, registerResult);
//                    case SREM ->
//                            this.RemOptimize(valueL, valueR, registerL, registerR, registerResult);
//                    default -> {
//                    }
//                }
//            }
//            // 如果不是
//            else {
//                if (valueR instanceof IrConstant irConstant) {
//                    this.LoadValueToRegister(valueL, registerL);
//                    this.GenerateAluMipsInstr(registerL, irConstant, registerResult);
//                } else {
//                    LoadValueToRegister(valueL, registerL);
//                    LoadValueToRegister(valueR, registerR);
//                    // 生成计算指令
//                    this.GenerateAluMipsInstr(registerL, registerR, registerResult);
//                }
//            }
//        }
//        else {
            LoadValueToRegister(valueL, registerL);
            LoadValueToRegister(valueR, registerR);
            // 生成计算指令
            this.GenerateAluMipsInstr(registerL, registerR, registerResult);
//        }

        // 如果没有寄存器保留结果，则应该把结果存到栈上
        this.SaveRegisterResult(this, registerResult);
    }

    public IrValue GetValueL() {
        return this.getFirstUseValue();
    }

    public IrValue GetValueR() {
        return this.getSecondUseValue();
    }

    private void GenerateAluMipsInstr(Register registerL, Register registerR,
                                      Register registerResult) {
        switch (arithmeticType) {
            case add -> new MipsAlu(MipsAlu.AluType.ADDU, registerResult, registerL, registerR);
            case sub -> new MipsAlu(MipsAlu.AluType.SUBU, registerResult, registerL, registerR);
            case mul -> {
                new MipsMdu(MipsMdu.MduType.MULT, registerL, registerR);
                new MipsMdu(MipsMdu.MduType.MFLO, registerResult);
            }
            case sdiv -> {
                new MipsMdu(MipsMdu.MduType.DIV, registerL, registerR);
                new MipsMdu(MipsMdu.MduType.MFLO, registerResult);
            }
            case srem -> {
                new MipsMdu(MipsMdu.MduType.DIV, registerL, registerR);
                new MipsMdu(MipsMdu.MduType.MFHI, registerResult);
            }
            case shl -> new MipsAlu(MipsAlu.AluType.SLLV, registerResult, registerL, registerR);
            case lshr -> new MipsAlu(MipsAlu.AluType.SRLV, registerResult, registerL, registerR);
            case ashr -> new MipsAlu(MipsAlu.AluType.SRAV, registerResult, registerL, registerR);
            case mulh -> {
                new MipsMdu(MipsMdu.MduType.MULT, registerL, registerR);
                new MipsMdu(MipsMdu.MduType.MFHI, registerResult);
            }
            default -> throw new RuntimeException("illegal alu type");
        }
    }
}
