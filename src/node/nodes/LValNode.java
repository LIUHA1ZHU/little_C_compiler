package node.nodes;

import error.Error;
import error.ErrorHandler;
import error.ErrorType;
import midEnd.symbol.FuncSymbol;
import midEnd.symbol.Symbol;
import midEnd.symbol.SymbolManager;
import midEnd.symbol.ValueSymbol;
import node.ExpAlikeNode;
import node.Node;
import node.NodeType;
import token.Token;

/**
 * LVal → Ident ['[' Exp ']']
 */
public class LValNode extends ExpAlikeNode {
    private final Token identToken;
    private final Token lBracket;
    private final ExpNode expNode;
    private final Token rBracket;

    public LValNode(Token identToken, Token lBracket, ExpNode expNode, Token rBracket) {
        super(NodeType.LVal);
        this.identToken = identToken;
        this.lBracket = lBracket;
        this.expNode = expNode;
        this.rBracket = rBracket;
    }

    public Token getIdentToken() {
        return identToken;
    }

    public ExpNode getExpNode() {
        return expNode;
    }

    // in funcRParams type check, only check single-symbol exp
    public String propagateSymbolName() {
        if (lBracket == null) {
            return identToken.getContent();
        } else {
            return identToken.getContent() + "[]";
        }
    }

    @Override
    public String toString() {
        if (lBracket == null) {
            return identToken + printNodeType();
        } else {
            return String.valueOf(identToken) + lBracket + expNode + rBracket + printNodeType();
        }
    }

    // TODO ugly
    @Override
    public void evaluate() { // must be a valueSymbol
        Symbol symbol = SymbolManager.getSymbolDefined(getIdentToken().getContent(), getIdentToken().getLineNum());
        if (symbol instanceof FuncSymbol) {
            ErrorHandler.addError(new Error(ErrorType.c, identToken.getLineNum()));
            return;
        }
        if (symbol == null) return;
        if (((ValueSymbol) symbol).getConstValues() != null && !((ValueSymbol) symbol).getConstValues().isEmpty() &&
                (symbol.getSymbolType().equals(Symbol.SymbolType.ConstInt) || symbol.getSymbolType().equals(Symbol.SymbolType.ConstIntArray))) { // const
            if (((ValueSymbol) symbol).getLength() != 1) { // array
                //TODO

            } else {
                constValue = ((ValueSymbol) symbol).getConstValues().get(0);
                isConst = true;
            }
        }
    }
}
