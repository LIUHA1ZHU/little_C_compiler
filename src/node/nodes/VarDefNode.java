package node.nodes;

import node.Node;
import node.NodeType;
import token.Token;

/**
 * VarDef → Ident [ '[' ConstExp ']' ] | Ident [ '[' ConstExp ']' ] '=' InitVal
 */
public class VarDefNode extends Node {
    private final Token identToken;
    private final Token lBracket;
    private final ConstExpNode constExpNode;
    private final Token rBracket;
    private final Token assignToken;
    private final InitValNode initValNode;

    public VarDefNode(Token identToken, Token lBracket, ConstExpNode constExpNode, Token rBracket, Token assignToken, InitValNode initValNode) {
        super(NodeType.VarDef);
        this.identToken = identToken;
        this.lBracket = lBracket;
        this.constExpNode = constExpNode;
        this.rBracket = rBracket;
        this.assignToken = assignToken;
        this.initValNode = initValNode;
    }

    public Token getIdentToken() {
        return identToken;
    }

    public ConstExpNode getConstExpNode() {
        return constExpNode;
    }

    public InitValNode getInitValNode() {
        return initValNode;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(identToken);
        if (lBracket != null) {
            sb.append(lBracket).append(constExpNode).append(rBracket);
        }
        if (assignToken != null) {
            sb.append(assignToken).append(initValNode);
        }
        return sb.append(printNodeType()).toString();
    }
}
