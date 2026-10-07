package optimizers.modifiers;

import midEnd.ir.IrModule;
import midEnd.ir.values.IrBasicBlock;
import midEnd.ir.values.IrFunction;
import midEnd.ir.values.IrGlobalValue;
import midEnd.ir.values.instructions.IrAllocaInstruction;
import midEnd.ir.values.IrInstruction;
import optimizers.analysers.DominanceAnalyser;

import java.util.ArrayList;

public class Mem2Reg {
    private IrModule irModule;

    public Mem2Reg(IrModule irModule) {
        this.irModule = irModule;
    }

    public static void run(IrModule module) {
        DominanceAnalyser.run(module);
        new Mem2Reg(module).optimize();
    }

    public void optimize() {
        for (IrGlobalValue global : irModule.getGlobalValues()) {
            if (global instanceof IrFunction irFunction) {
                if (irFunction.getBasicBlocks().isEmpty()) continue;

                IrBasicBlock entryBlock = irFunction.getBasicBlocks().get(0);
                for (IrBasicBlock irBasicBlock : irFunction.getBasicBlocks()) {
                    ArrayList<IrInstruction> instrList = new ArrayList<>(irBasicBlock.getInstructions());
                    for (IrInstruction instr : instrList) {
                        if (this.isValueAllocate(instr)) {
                            InsertPhi insertPhi = new InsertPhi((IrAllocaInstruction) instr, entryBlock);
                            insertPhi.addPhi();
                        }
                    }
                }
            }
        }
    }

    private boolean isValueAllocate(IrInstruction instr) {
        if (instr instanceof IrAllocaInstruction allocateInstr) {
            return !allocateInstr.isArray();
        }
        return false;
    }
}
