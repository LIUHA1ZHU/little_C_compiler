package node.nodes;

import node.Node;
import node.NodeType;
import token.Token;

/**
 * LVal → Ident ['[' Exp ']']
 */
public class LValNode extends Node {
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

    @Override
    public String toString() {
        if (lBracket == null) {
            return identToken + printNodeType();
        } else {
            return String.valueOf(identToken) + lBracket + expNode + rBracket + printNodeType();
        }
    }
}
