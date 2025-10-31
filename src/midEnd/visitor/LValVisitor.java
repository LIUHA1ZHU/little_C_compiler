package midEnd.visitor;

import midEnd.ir.IrValue;
import midEnd.ir.IrValueType;
import midEnd.ir.values.IrGlobalVariable;
import midEnd.ir.values.IrVariable;
import midEnd.ir.values.instructions.IrLoadInstruction;
import midEnd.symbol.Symbol;
import midEnd.symbol.SymbolManager;
import node.nodes.LValNode;

public class LValVisitor {

    /**
     * Called in evaluate.
     * Just for error check
     */
    public static void evaluate(LValNode lValNode) {
        Symbol symbol = SymbolManager.getSymbolDefined(lValNode.getIdentToken().getContent(), lValNode.getIdentToken().getLineNum());
    }

    /**
     * get symbol & generate an IrLoadInstruction
     * @return IrLoadInstruction
     */
    public static IrValue visit(LValNode lValNode) {
        String name = lValNode.getIdentToken().getContent();
        Symbol symbol = SymbolManager.getSymbolDefined(name, lValNode.getIdentToken().getLineNum());
        boolean isGlobal = SymbolManager.checkSymbolIsGlobal(symbol);


        if (symbol != null) {
            IrValue irVariable = new IrVariable(name + symbol.getShadowingNum(), isGlobal);
            return new IrLoadInstruction(irVariable);
        } else return null; // has error
    }
}
