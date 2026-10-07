package optimizers.modifiers;

import midEnd.ir.IrModule;
import midEnd.ir.IrValue;
import midEnd.ir.values.IrBasicBlock;
import midEnd.ir.values.IrFunction;
import midEnd.ir.values.IrInstruction;
import midEnd.ir.values.instructions.IrCallInstruction;
import midEnd.ir.values.instructions.IrInstructionType;
import midEnd.ir.values.instructions.IrLoadInstruction;
import midEnd.ir.values.instructions.IrStoreInstruction;

import java.util.*;

import midEnd.ir.values.instructions.IrGEPInstruction;

public class DeadStoreElimination {

    public static void run(IrModule module) {
        for (IrFunction function : module.getFunctions()) {
            runOnFunction(function);
        }
    }

    private static void runOnFunction(IrFunction function) {
        for (IrBasicBlock bb : function.getBasicBlocks()) {
            runOnBlock(bb);
        }
    }

    private static void runOnBlock(IrBasicBlock bb) {
        // Map<PointerValue, StoreInstruction>
        Map<IrValue, IrStoreInstruction> lastStore = new HashMap<>();
        List<IrInstruction> toRemove = new ArrayList<>();

        // Iterate over instructions in the block
        // We use an index loop because we might need to look ahead or just process sequentially
        List<IrInstruction> instructions = bb.getInstructions();
        for (IrInstruction instr : instructions) {
            
            // 1. Check for Store Instruction
            if (instr instanceof IrStoreInstruction) {
                IrStoreInstruction currentStore = (IrStoreInstruction) instr;
                IrValue pointer = currentStore.getObjectiveUseValue();

                // Check if there is a previous store to the same pointer
                if (lastStore.containsKey(pointer)) {
                    IrStoreInstruction prevStore = lastStore.get(pointer);
                    
                    toRemove.add(prevStore);
                }

                lastStore.put(pointer, currentStore);
            }
            // 2. Check for Load Instruction
            else if (instr instanceof IrLoadInstruction) {
                IrLoadInstruction load = (IrLoadInstruction) instr;
                if (load.getUserList().isEmpty()) {
                    continue;
                }

                IrValue pointer = load.getObjectiveUseValue();
                
                // A load kills the "dead store" opportunity for this pointer
                // because the value stored is now used.
                lastStore.remove(pointer);
                
            }
            // 3. Check for Call Instruction (Side Effects)
            else if (instr instanceof IrCallInstruction) {
                
                lastStore.clear();
            }
            
        }


        IrInstruction lastInstr = instructions.isEmpty() ? null : instructions.get(instructions.size() - 1);
        boolean isReturn = lastInstr != null && (lastInstr.getInstructionType() == IrInstructionType.ReturnIntInstr || lastInstr.getInstructionType() == IrInstructionType.ReturnVoidInstr);

        if (isReturn) {
            for (Map.Entry<IrValue, IrStoreInstruction> entry : lastStore.entrySet()) {
                IrValue ptr = entry.getKey();
                // Only remove if it's a local variable and NOT a global variable.
                if (isLocalAlloca(ptr)) {
                    toRemove.add(entry.getValue());
                }
            }
        }
        
        // Remove marked instructions
        for (IrInstruction i : toRemove) {
            i.removeAllValueUse();
            bb.getInstructions().remove(i);
        }
    }
        
    private static boolean isLocalAlloca(IrValue ptr) {
        
        if (ptr instanceof midEnd.ir.values.instructions.IrAllocaInstruction) {
            return true;
        }
    
        
        if (ptr instanceof IrGEPInstruction) {
                IrGEPInstruction gep = (IrGEPInstruction) ptr;
                return isLocalAlloca(gep.getFirstUseValue()); // Recursive check base
        }
        
        return false;
    }
}
