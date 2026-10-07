package optimizers.analysers;

import midEnd.ir.IrModule;
import midEnd.ir.values.IrBasicBlock;
import midEnd.ir.values.IrFunction;
import midEnd.ir.values.IrInstruction;
import midEnd.ir.values.instructions.IrBranchInstruction;


import java.util.ArrayList;
import java.util.HashMap;

public class CFGAnalyser {
    private static HashMap<IrFunction, IrBasicBlock> funcEntryBlockMap;

    public static void run(IrModule irModule) {
        CFGAnalyser analyser = new CFGAnalyser();
        analyser.analyse(irModule);
    }

    public void analyse(IrModule irModule) {
        funcEntryBlockMap = new HashMap<>();

        irModule.getGlobalValues().stream()
                .filter(IrFunction.class::isInstance)
                .map(IrFunction.class::cast)
                .forEach(function -> function.getBasicBlocks().forEach(basicBlock -> {
                    // clear previous CFG info
                    basicBlock.getPredecessors().clear();
                    basicBlock.getSuccessors().clear();
                }));

        irModule.getGlobalValues().stream()
                .filter(IrFunction.class::isInstance)
                .map(IrFunction.class::cast)
                .forEach(CFGAnalyser::analyseFuncCFG);
    }

    private static void analyseFuncCFG(IrFunction function) {
        for (IrBasicBlock basicBlock : function.getBasicBlocks()) {
            ArrayList<IrInstruction> instructions = basicBlock.getInstructions();
            IrInstruction instr = instructions.get(instructions.size() - 1);
            if (instr instanceof IrBranchInstruction) {
                if (instr.getFirstUseValue() != null) {
                    basicBlock.addSuc((IrBasicBlock) instr.getFirstUseValue());
                    ((IrBasicBlock) instr.getFirstUseValue()).addPre(basicBlock);
                }
                if (instr.getSecondUseValue() != null) {
                    basicBlock.addSuc((IrBasicBlock) instr.getSecondUseValue());
                    ((IrBasicBlock) instr.getSecondUseValue()).addPre(basicBlock);
                }
            }
        }
        funcEntryBlockMap.put(function, function.getBasicBlocks().get(0));
    }

    public static HashMap<IrFunction, IrBasicBlock> getFuncEntryBlockMap() {
        return funcEntryBlockMap;
    }
}
