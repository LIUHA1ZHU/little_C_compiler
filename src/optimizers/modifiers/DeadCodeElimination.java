package optimizers.modifiers;

import midEnd.ir.IrModule;
import midEnd.ir.IrValue;
import midEnd.ir.values.IrBasicBlock;
import midEnd.ir.values.IrFunction;
import midEnd.ir.values.IrInstruction;
import midEnd.ir.values.instructions.*;
import midEnd.ir.values.instructions.IOInstructions.IrGetintInstruction;
import midEnd.ir.values.instructions.IOInstructions.IrPutintInstruction;
import midEnd.ir.values.instructions.IOInstructions.IrPutstrInstruction;
import midEnd.ir.values.instructions.phi.PhiInstr;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Stack;

public class DeadCodeElimination {

    public static void run(IrModule irModule) {
        HashSet<IrInstruction> activeInstrSet = GetActiveInstrSet(irModule);
        removeInactiveInstructions(irModule, activeInstrSet);
    }

    private static HashSet<IrInstruction> GetActiveInstrSet(IrModule irModule) {
        HashSet<IrInstruction> activeInstrSet = new HashSet<>();
        Stack<IrInstruction> todoInstrStack = new Stack<>();

        for (IrFunction irFunction : irModule.getFunctions()) {
            for (IrBasicBlock irBasicBlock : irFunction.getBasicBlocks()) {
                for (IrInstruction instr : irBasicBlock.getInstructions()) {
                    if (IsCriticalInstr(instr)) {
                        if (!activeInstrSet.contains(instr)) {
                            activeInstrSet.add(instr);
                            todoInstrStack.push(instr);
                        }
                    }
                }
            }
        }

        while (!todoInstrStack.isEmpty()) {
            IrInstruction todoInstr = todoInstrStack.pop();
            for (IrValue useValue : getUseValues(todoInstr)) {
                if (useValue instanceof IrInstruction useInstr) {
                    if (!activeInstrSet.contains(useInstr)) {
                        activeInstrSet.add(useInstr);
                        todoInstrStack.push(useInstr);
                    }
                }
            }
        }

        return activeInstrSet;
    }

    private static boolean IsCriticalInstr(IrInstruction instr) {
        // Store to memory
        if (instr instanceof IrStoreInstruction) return true;
        // Function return
        if (instr instanceof IrReturnInstruction) return true;
        // Function call (assume side effects)
        if (instr instanceof IrCallInstruction) return true;
        // IO Instructions
        if (instr instanceof IrGetintInstruction || 
            instr instanceof IrPutintInstruction || 
            instr instanceof IrPutstrInstruction) return true;
        // Control flow (Branch) - needed to preserve CFG structure
        if (instr instanceof IrBranchInstruction) return true;

        return false;
    }

    private static ArrayList<IrValue> getUseValues(IrInstruction instr) {
        ArrayList<IrValue> uses = new ArrayList<>();
        if (instr instanceof PhiInstr) {
            uses.addAll(((PhiInstr) instr).getUseValueList());
        } else if (instr instanceof IrCallInstruction) {
            IrCallInstruction call = (IrCallInstruction) instr;
            if (call.getFirstUseValue() != null) {
                uses.add(call.getFirstUseValue());
            }
            uses.addAll(call.getParamsList());
        } else {
            if (instr.getFirstUseValue() != null) uses.add(instr.getFirstUseValue());
            if (instr.getSecondUseValue() != null) uses.add(instr.getSecondUseValue());
            if (instr.getObjectiveUseValue() != null) uses.add(instr.getObjectiveUseValue());
        }
        return uses;
    }

    private static void removeInactiveInstructions(IrModule irModule, HashSet<IrInstruction> activeInstrSet) {
        for (IrFunction irFunction : irModule.getFunctions()) {
            for (IrBasicBlock irBasicBlock : irFunction.getBasicBlocks()) {
                ArrayList<IrInstruction> instrs = irBasicBlock.getInstructions();
                Iterator<IrInstruction> it = instrs.iterator();
                while (it.hasNext()) {
                    IrInstruction instr = it.next();
                    if (!activeInstrSet.contains(instr)) {
                        // Remove users first
                        instr.removeAllValueUse();
                        // Remove from basic block
                        it.remove();
                    }
                }
            }
        }
    }
}
