package midEnd.ir.values;

import backend.mips.assembly.data.MipsAsciiz;
import midEnd.ir.IrBuilder;
import midEnd.ir.IrValueType;

public class IrStringConstant extends IrGlobalValue{
    private String string;
    public IrStringConstant(String string) {
        super(IrBuilder.GlobalPrefix + ".str." + IrBuilder.getStringConstantNum(), IrValueType.StringConstant);
        this.string = string;
        IrBuilder.addStringConstant(this);
    }

    public int getLength() { // "\\n" is a single char but represented by two
        int len = string.length();
        int count = 0;
        int index = 0;
        while ((index = string.indexOf("\\n", index)) != -1) {
            count++;
            index += 2;
        }
        return len - count + 1; // + 1 for \0
    }

    @Override
    public String toString() {
        return name + " = private unnamed_addr constant [" + getLength() + " x i8] c\""
                + string.replace("\\n", "\\0A") + "\\00" + "\", align 1\n";
    }

    @Override
    public void toMips() {
        String name = getMipsLabel();

        String content = string.replace("\\0A", "\n");
        new MipsAsciiz(name, content);
    }
}
