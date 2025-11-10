package midEnd.ir.values.instructions;

import midEnd.ir.IrBuilder;
import midEnd.ir.IrValue;
import midEnd.ir.IrValueType;
import midEnd.ir.values.IrFunction;
import midEnd.ir.values.IrInstruction;
import midEnd.ir.values.IrVariable;

import java.util.ArrayList;

public class IrCallInstruction extends IrInstruction {
    /**
     * gep | load | arithmetic
     */
    private ArrayList<IrValue> paramsList;

    public IrCallInstruction(IrFunction function, ArrayList<IrValue> paramsList) {
        super(function.getIrFunctionType().equals(IrFunction.IrFunctionType.intFunc) ? IrBuilder.LocalPrefix + IrBuilder.getTempVarNum() : "void",
                IrInstructionType.CallInstr, function, null, null);
        this.paramsList = paramsList;
    }

    @Override
    public String toString() {
        // TODO
        StringBuilder sb = new StringBuilder();
        if (getName().equals("void")) {
            sb.append("call void @").append(getFirstUseValue().getName()).append("(");
            for (int i = 0; i < paramsList.size(); i++) {
                IrValue value = paramsList.get(i);
                if (value.getValueType().equals(IrValueType.Instr)) {
                    IrInstruction instr = (IrInstruction) value;
                    sb.append(instr.getInstructionType().getValueType()).append(" ").append(instr.getName());
                } else {
                    sb.append("i32 ").append(value.getName());
                }
                if (i != paramsList.size() -1) sb.append(", ");
            }
        }
        else {
            sb.append(getName()).append(" = call i32 @").append(getFirstUseValue().getName());
//            sb.append("(");
//            for (int i = 0; i < paramsList.size(); i++) {
//                IrValue value = paramsList.get(i);
//                if (value.getValueType().equals(IrValueType.Instr)) {
//                    IrInstruction instr = (IrInstruction) value;
//                    if (instr.getInstructionType().getValueType().equals("i32 *")) sb.append("int * ");
//                    else sb.append("int ");
//                } else { // must be irConstant
//                    sb.append("int ");
//                }
//                if (i != paramsList.size() -1) sb.append(", ");
//            }
//            sb.append(")");
            sb.append("(");
            for (int i = 0; i < paramsList.size(); i++) {
                IrValue value = paramsList.get(i);
                if (value.getValueType().equals(IrValueType.Instr)) {
                    IrInstruction instr = (IrInstruction) value;
                    sb.append(instr.getInstructionType().getValueType()).append(" ").append(instr.getName());
                } else {
                    sb.append("i32 ").append(value.getName());
                }
                if (i != paramsList.size() -1) sb.append(", ");
            }
        }
        sb.append(")\n");
        return sb.toString();
    }
}
