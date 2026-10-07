package midEnd.ir.values.instructions;

import backend.mips.MipsBuilder;
import backend.mips.Register;
import backend.mips.assembly.MipsAlu;
import backend.mips.assembly.MipsJump;
import backend.mips.assembly.MipsLsu;
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
        for (IrValue param : paramsList) {
            param.addUser(this);
        }
    }

    public ArrayList<IrValue> getParamsList() {
        return paramsList;
    }

    @Override
    public void replaceUse(IrValue oldVal, IrValue newVal) {
        super.replaceUse(oldVal, newVal);
        for (int i = 0; i < paramsList.size(); i++) {
            if (paramsList.get(i) == oldVal) {
                paramsList.set(i, newVal);
                oldVal.removeUser(this);
                newVal.addUser(this);
            }
        }
    }

    @Override
    public void removeAllValueUse() {
        super.removeAllValueUse();
        for (IrValue param : paramsList) {
            param.removeUser(this);
        }
        paramsList.clear();
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
                    sb.append(instr.getInstructionType().getCallValueType()).append(" ").append(instr.getName());
                } else { // constant or variable
                    sb.append("i32 ").append(value.getName());
                }
                if (i != paramsList.size() -1) sb.append(", ");
            }
        }
        else {
            sb.append(getName()).append(" = call i32 @").append(getFirstUseValue().getName());
            sb.append("(");
            for (int i = 0; i < paramsList.size(); i++) {
                IrValue value = paramsList.get(i);
                if (value.getValueType().equals(IrValueType.Instr)) {
                    IrInstruction instr = (IrInstruction) value;
                    sb.append(instr.getInstructionType().getCallValueType()).append(" ").append(instr.getName());
                } else { // constant or variable
                    sb.append("i32 ").append(value.getName());
                }
                if (i != paramsList.size() -1) sb.append(", ");
            }
        }
        sb.append(")\n");
        return sb.toString();
    }

    public void toMips() {
        super.toMips();

        int currentOffset = MipsBuilder.GetCurrentStackOffset();
        ArrayList<Register> allocatedRegisterList = MipsBuilder.GetAllocatedRegList();

        // Calculate stack space for parameters > 3
        int paramSpace = paramsList.size() * 4;
        
        // save current
        this.SaveCurrent(currentOffset, allocatedRegisterList);

        // fill params to register and stack
        this.FillParams(paramsList, currentOffset, allocatedRegisterList);
        
        // Adjust SP decrement to include paramSpace
        int baseOffset = currentOffset - 4 * allocatedRegisterList.size() - 8;
        int finalOffset = baseOffset - paramSpace;


        new MipsAlu(MipsAlu.AluType.ADDI, Register.SP, Register.SP, finalOffset);

        IrFunction targetFunction = (IrFunction) getFirstUseValue();
        new MipsJump(MipsJump.JumpType.JAL, targetFunction.getMipsLabel());

        // Pop stack arguments if any
        if (paramSpace > 0) {
            new MipsAlu(MipsAlu.AluType.ADDI, Register.SP, Register.SP, paramSpace);
        }

        // recover
        // RecoverCurrent expects the `formerOffset` which corresponds to the FrameSize (currentOffset initially)
        // because it uses it to calculate offsets relative to the OLD SP (which is restored first).
        // Wait, RecoverCurrent restores SP first using `LW SP, 4($SP)`.
        // At this point (after popping args), SP points to the "Saved Area" bottom.
        // Saved RA is at 0($SP) relative to Saved Area bottom? 
        // No, in SaveCurrent:
        // SP stored at `Base - 4`. RA stored at `Base - 8`.
        // If we move SP by `Base - ParamSpace`.
        // And then Add `ParamSpace`. SP is now at `OldSP + Base`.
        // Relative to this SP:
        // RA is at `OldSP + Base - 8` -> `-8($SP)`.
        // Saved SP is at `OldSP + Base - 4` -> `-4($SP)`.
        // But RecoverCurrent reads from 0 and 4.
        // This implies RecoverCurrent assumes SP points to `OldSP + Base - 8`?
        
        // Let's look at SaveCurrent again.
        // Writes SP at `currentOffset - regNum*4 - 4`.
        // Writes RA at `currentOffset - regNum*4 - 8`.
        // `Base = currentOffset - regNum*4 - 8`.
        // So RA is at `Base`. SP is at `Base + 4`.
        // So RA is at `0($Base)`. SP is at `4($Base)`.
        // So if SP points to `OldSP + Base`, then `LW RA, 0($SP)` works!
        
        // So yes, after popping ParamSpace, SP is at `OldSP + Base`.
        // RecoverCurrent logic is correct.
        
        // currentOffset variable passed to RecoverCurrent is used for loading registers.
        // It passes `currentOffset + 4 * allocatedRegisterList.size() + 8`.
        // Since `currentOffset` was NOT modified in my new code (I used local vars),
        // I should use the original `currentOffset` value here.
        // Wait, in original code `currentOffset` WAS modified.
        // `currentOffset = currentOffset - ...`
        // `currentOffset = currentOffset + ...`
        // So it restores it to original value.
        // So I can just pass `currentOffset` (initial value).
        
        this.RecoverCurrent(currentOffset, allocatedRegisterList);

        SaveRegisterResult(this, Register.V0);
    }

    private void SaveCurrent(int currentOffset, ArrayList<Register> allocatedRegisterList) {
        int registerNum = 0;
        for (Register register : allocatedRegisterList) {
            registerNum++;
            new MipsLsu(MipsLsu.LsuType.SW, register, Register.SP,
                    currentOffset - registerNum * 4);
        }

        new MipsLsu(MipsLsu.LsuType.SW, Register.SP, Register.SP,
                currentOffset - registerNum * 4 - 4);
        new MipsLsu(MipsLsu.LsuType.SW, Register.RA, Register.SP,
                currentOffset - registerNum * 4 - 8);
    }

    private void FillParams(ArrayList<IrValue> paramList, int currentOffset,
                            ArrayList<Register> allocatedRegisterList) {
        for (int i = 0; i < paramList.size(); i++) {
            IrValue param = paramList.get(i);
            // fill register
            if (i < 3) {
                Register paramRegister = Register.get(Register.A0.ordinal() + i + 1); // $a0 reserved for syscall
                Register valReg = MipsBuilder.GetValueToRegister(param);
                if (valReg != null && allocatedRegisterList.contains(valReg)) {
                    new MipsLsu(MipsLsu.LsuType.LW, paramRegister, Register.SP,
                            currentOffset - 4 * allocatedRegisterList.indexOf(valReg) - 4);
                } else {
                    this.LoadValueToRegister(param, paramRegister);
                }
            }
            // to stack
            else {
                Register tempRegister = Register.K0;
                Register valReg = MipsBuilder.GetValueToRegister(param);
                if (valReg != null && allocatedRegisterList.contains(valReg)) {
                    new MipsLsu(MipsLsu.LsuType.LW, tempRegister, Register.SP,
                            currentOffset - 4 * allocatedRegisterList.indexOf(valReg) - 4);
                } else {
                    this.LoadValueToRegister(param, tempRegister);
                }
                new MipsLsu(MipsLsu.LsuType.SW, tempRegister, Register.SP,
                        currentOffset - 4 * allocatedRegisterList.size() - 8 - i * 4 - 4);
            }
        }
    }

    private void RecoverCurrent(int formerOffset, ArrayList<Register> allocatedRegisterList) {
        new MipsLsu(MipsLsu.LsuType.LW, Register.RA, Register.SP, 0);
        new MipsLsu(MipsLsu.LsuType.LW, Register.SP, Register.SP, 4);

        // $sp recovered
        int registerNum = 0;
        for (Register register : allocatedRegisterList) {
            registerNum++;
            new MipsLsu(MipsLsu.LsuType.LW, register, Register.SP,
                    formerOffset - registerNum * 4);
        }
    }

}
