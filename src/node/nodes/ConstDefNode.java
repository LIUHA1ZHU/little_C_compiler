package node.nodes;

import node.Node;
import node.NodeType;
import token.Token;

/**
 * ConstDef → Ident [ '[' ConstExp ']' ] '=' ConstInitVal
 */
public class ConstDefNode extends Node {
    private final Token identToken;
    private final Token lBracket;
    private final ConstExpNode constExpNode;
    private final Token rBracket;
    private final Token assignToken;
    private final ConstInitValNode constInitValNode;

    public ConstDefNode(Token identToken, Token lBracket, ConstExpNode constExpNode, Token rBracket, Token assignToken, ConstInitValNode constInitValNode) {
        super(NodeType.ConstDef);
        this.identToken = identToken;
        this.lBracket = lBracket;
        this.constExpNode = constExpNode;
        this.rBracket = rBracket;
        this.assignToken = assignToken;
        this.constInitValNode = constInitValNode;
    }

    public Token getIdentToken() {
        return identToken;
    }

    public ConstExpNode getConstExpNode() {
        return constExpNode;
    }

    public ConstInitValNode getConstInitValNode() {
        return constInitValNode;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(identToken);
        if (lBracket != null) {
            sb.append(lBracket).append(constExpNode).append(rBracket);
        }
        sb.append(assignToken).append(constInitValNode);
        return sb.append(printNodeType()).toString();
    }
}
