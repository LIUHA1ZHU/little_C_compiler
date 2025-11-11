package midEnd.visitor;

import midEnd.ir.IrValue;
import midEnd.ir.values.IrConstant;
import midEnd.ir.values.IrVariable;
import midEnd.ir.values.instructions.IrGEPInstruction;
import midEnd.ir.values.instructions.IrLoadInstruction;
import midEnd.symbol.Symbol;
import midEnd.symbol.SymbolManager;
import midEnd.symbol.ValueSymbol;
import node.nodes.LValNode;

public class LValVisitor {

    /**
     * get symbol & generate an IrLoadInstruction. (Constants are solved in evaluate)
     * @return IrLoadInstruction
     */
    public static IrValue visit(LValNode lValNode) {
        String name = lValNode.getIdentToken().getContent();
        Symbol symbol = SymbolManager.getSymbolDefined(name, lValNode.getIdentToken().getLineNum());

        if (symbol == null) return new IrConstant(0); // has error

        if (symbol.getSymbolType().equals(Symbol.SymbolType.ConstInt)) { // global or local
            return new IrConstant(((ValueSymbol) symbol).getConstValues().get(0));
        }

        if (symbol.getSymbolType().equals(Symbol.SymbolType.StaticInt)) {
            return new IrLoadInstruction(symbol.getIrValue());
        }

        if (symbol.getSymbolType().equals(Symbol.SymbolType.Int)) {
            if (symbol.getIrValue() instanceof IrVariable && ((IrVariable) symbol.getIrValue()).isFuncFormal()) return symbol.getIrValue();
            return new IrLoadInstruction(symbol.getIrValue());
        }

        // Array
        if (lValNode.getExpNode() == null) { // in funcRParam, array decays to a pointer
            return new IrGEPInstruction(symbol.getIrValue(), new IrConstant(0));
        }
        return visitArray(lValNode, symbol);
    }

    private static IrValue visitArray(LValNode lValNode, Symbol symbol) {
        lValNode.getExpNode().evaluate();
        if (lValNode.getExpNode().isConst()) { // index is const
            if (symbol.getSymbolType().equals(Symbol.SymbolType.ConstIntArray)) {
                return new IrConstant(((ValueSymbol) symbol).getConstValues().get(lValNode.getExpNode().getConstValue()));
            }
        }

        IrValue index = ExpVisitor.visit(lValNode.getExpNode());
        IrGEPInstruction gep = new IrGEPInstruction(symbol.getIrValue(), index);
        return new IrLoadInstruction(gep);
    }
}
