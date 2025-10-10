package node.nodes;

import node.Node;
import node.NodeType;
import token.Token;

/**
 * PrimaryExp → '(' Exp ')' | LVal | Number
 */
public class PrimaryExpNode extends Node {
    private final Token lParent;
    private final ExpNode expNode;
    private final Token rParent;
    private final LValNode lValNode;
    private final NumberNode numberNode;

    public PrimaryExpNode(Token lParent, ExpNode expNode, Token rParent, LValNode lValNode, NumberNode numberNode) {
        super(NodeType.PrimaryExp);
        this.lParent = lParent;
        this.expNode = expNode;
        this.rParent = rParent;
        this.lValNode = lValNode;
        this.numberNode = numberNode;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (lParent != null) {
            sb.append(lParent).append(expNode).append(rParent);
        } else if (lValNode != null) {
            sb.append(lValNode);
        } else {
            sb.append(numberNode);
        }
        return sb.append(printNodeType()).toString();
    }
}
