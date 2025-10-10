package node.nodes;

import node.Node;
import node.NodeType;
import token.Token;

/**
 * MulExp → UnaryExp | MulExp ('*' | '/' | '%') UnaryExp
 */
public class MulExpNode extends Node {
    private final UnaryExpNode unaryExpNode;
    private final Token opToken;
    private final MulExpNode mulExpNode;

    public MulExpNode(UnaryExpNode unaryExpNode, Token opToken, MulExpNode mulExpNode) {
        super(NodeType.MulExp);
        this.unaryExpNode = unaryExpNode;
        this.opToken = opToken;
        this.mulExpNode = mulExpNode;
    }


    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (opToken != null) {
            sb.append(mulExpNode).append(opToken).append(unaryExpNode);
        } else {
            sb.append(unaryExpNode);
        }
        return sb.append(printNodeType()).toString();
    }
}
