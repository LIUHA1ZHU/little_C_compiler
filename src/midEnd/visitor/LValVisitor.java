package midEnd.visitor;

import midEnd.symbol.Symbol;
import midEnd.symbol.SymbolManager;
import node.nodes.LValNode;

public class LValVisitor {

    public static void visit(LValNode lValNode) {
        Symbol symbol = SymbolManager.getSymbolDefined(lValNode.getIdentToken().getContent(), lValNode.getIdentToken().getLineNum());
    }
}
