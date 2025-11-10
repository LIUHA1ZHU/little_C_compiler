package midEnd.ir.values.instructions;

import midEnd.ir.IrBuilder;
import midEnd.ir.IrValue;
import midEnd.ir.values.IrGlobalVariable;
import midEnd.ir.values.IrInstruction;
import midEnd.ir.values.IrVariable;

public class IrGEPInstruction extends IrInstruction {
    private IrValue index;

    public IrGEPInstruction(IrValue alloca, IrValue index) {
        super(IrBuilder.LocalPrefix + IrBuilder.getTempVarNum(), IrInstructionType.GEPInstr, alloca, null, null);
        this.index = index;
    }

    @Override
    public String toString() {
        if (getFirstUseValue() instanceof IrVariable && ((IrVariable) getFirstUseValue()).isVariableLength()) {
            return name + " = getelementptr inbounds i32, i32* " + getFirstUseValue().getName() + ", i32 " + index.getName() + "\n";
        } else {
            int length;
            if (getFirstUseValue() instanceof IrAllocaInstruction) {
                length = ((IrAllocaInstruction) getFirstUseValue()).getLength();
            } else if (getFirstUseValue() instanceof IrGlobalVariable) {
                length = ((IrGlobalVariable) getFirstUseValue()).getLength();
            } else if (getFirstUseValue() instanceof IrVariable) {
                length = ((IrVariable) getFirstUseValue()).getLength();
            } else throw new RuntimeException("gep invalid IrValue" + getFirstUseValue().getValueType());
            String valueName = getFirstUseValue().getName();
            return name + " = getelementptr inbounds [" + length + " x i32], [" + length +
                    " x i32]* " + valueName + ", i32 0, i32 " + index.getName() + "\n";
        }
    }
}
