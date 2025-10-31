package midEnd.ir.values.instructions;

import midEnd.ir.IrBuilder;
import midEnd.ir.IrValue;
import midEnd.ir.values.IrInstruction;

public class IrArithmeticInstruction extends IrInstruction {
    public enum IrArithmeticType {
        add("add nsw i32 "),
        sub("sub i32 "),
        mul("mul nsw i32 "),
        sdiv("sdiv i32 "),
        srem("srem i32 "),
        and("and i32 "),
        or("or i32 ");

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

    @Override
    public String toString() {
        return name + " = " + arithmeticType.getString() + getFirstUseValue().getName() + ", " + getSecondUseValue().getName() + "\n";
    }
}
