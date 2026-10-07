package midEnd.ir.values.instructions;

import backend.mips.Register;
import backend.mips.assembly.MipsLsu;
import midEnd.ir.IrValue;
import midEnd.ir.values.IrInstruction;

// store i32 %value, i32* %ptr
/**
 * firstUseValue: to be stored.
 * objectiveUseValue: a ptr
 */
public class IrStoreInstruction extends IrInstruction {
    private boolean storeAddress;

    public IrStoreInstruction(String name, IrValue value1, IrValue valueP) {
        super(name, IrInstructionType.StoreInstr, value1, null, valueP);
        this.storeAddress = false;
    }

    public IrStoreInstruction(String name, IrValue value1, IrValue valueP, boolean storeAddress) {
        super(name, IrInstructionType.StoreInstr, value1, null, valueP);
        this.storeAddress = storeAddress;
    }

    @Override
    public String toString() {
        if (!storeAddress) {
            return "store i32 " + getFirstUseValue().getName() + ", i32* " +  getObjectiveUseValue().getName() + ", align 4\n";
        } else {
            return "store i32* " + getFirstUseValue().getName() + ", i32** " +  getObjectiveUseValue().getName() + ", align 8\n";
        }
    }

    public void toMips() {
        super.toMips();

        IrValue valueValue = getFirstUseValue();
        IrValue addressValue = getObjectiveUseValue();

        Register valueRegister = this.GetRegisterOrK0ForValue(valueValue);
        Register addressRegister = this.GetRegisterOrK1ForValue(addressValue);

        this.LoadValueToRegister(valueValue, valueRegister);
        this.LoadValueToRegister(addressValue, addressRegister);

        new MipsLsu(MipsLsu.LsuType.SW, valueRegister, addressRegister, 0);
    }
}
