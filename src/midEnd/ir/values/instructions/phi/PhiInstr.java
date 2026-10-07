package midEnd.ir.values.instructions.phi;

import midEnd.ir.IrBuilder;
import midEnd.ir.IrValue;
import midEnd.ir.values.IrBasicBlock;
import midEnd.ir.values.IrInstruction;
import midEnd.ir.values.instructions.IrInstructionType;

import java.util.ArrayList;
import java.util.StringJoiner;

public class PhiInstr extends IrInstruction {
    private final ArrayList<IrBasicBlock> beforeBlockList;
    private final ArrayList<IrValue> useValueList;

    public PhiInstr(IrBasicBlock irBasicBlock) {
        super(IrBuilder.LocalPrefix + IrBuilder.getTempVarNum(), IrInstructionType.PhiInstr, null, null, null, false);
        this.setParent(irBasicBlock);
        
        this.beforeBlockList = new ArrayList<>(irBasicBlock.getPredecessors());
        this.useValueList = new ArrayList<>();
        
        for (int i = 0; i < this.beforeBlockList.size(); i++) {
            this.useValueList.add(null);
        }
    }

    public ArrayList<IrBasicBlock> getBeforeBlockList() {
        return this.beforeBlockList;
    }

    public ArrayList<IrValue> getUseValueList() {
        return this.useValueList;
    }

    public void convertBlockToValue(IrValue irValue, IrBasicBlock beforeBlock) {
        int index = this.beforeBlockList.indexOf(beforeBlock);
        if (index != -1) {
            this.useValueList.set(index, irValue);
            if (irValue != null) {
                irValue.addUser(this);
            }
        }
    }

    @Override
    public void replaceUse(IrValue oldVal, IrValue newVal) {
        for (int i = 0; i < useValueList.size(); i++) {
            if (useValueList.get(i) == oldVal) {
                useValueList.set(i, newVal);
                oldVal.removeUser(this);
                newVal.addUser(this);
            }
        }
    }

    @Override
    public void removeAllValueUse() {
        super.removeAllValueUse();
        for (IrValue val : useValueList) {
            if (val != null) {
                val.removeUser(this);
            }
        }
        useValueList.clear();
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(this.name);
        builder.append(" = phi i32 ");

        StringJoiner joiner = new StringJoiner(", ");
        for (int i = 0; i < this.beforeBlockList.size(); i++) {
            final StringBuilder blockBuilder = new StringBuilder();
            blockBuilder.append("[ ");
            IrValue val = this.useValueList.get(i);
            blockBuilder.append(val == null ? "undef" : val.getName());
            blockBuilder.append(", %");
            blockBuilder.append(this.beforeBlockList.get(i).getMipsName());
            blockBuilder.append(" ]");
            joiner.add(blockBuilder);
        }
        builder.append(joiner);
        builder.append("\n");
        return builder.toString();
    }

    @Override
    public void toMips() {
        throw new RuntimeException("phi not delete!");
    }
}
