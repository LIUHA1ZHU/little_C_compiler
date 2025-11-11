package optimizers.analysers;

import midEnd.ir.IrBuilder;
import midEnd.ir.IrModule;
import midEnd.ir.values.IrBasicBlock;
import midEnd.ir.values.IrFunction;


import java.util.HashMap;

public class CFGAnalyser {
    private static HashMap<IrFunction, IrBasicBlock> funcEntryBlockMap;

    public void analyse(IrModule irModule) {
        irModule.getGlobalValues().stream()
                .filter(IrFunction.class::isInstance)
                .map(IrFunction.class::cast)
                .forEach(CFGAnalyser::analyseFuncCFG);
    }

    private static void analyseFuncCFG(IrFunction function) {

    }


}
