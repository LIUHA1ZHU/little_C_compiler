package node.nodes;

import node.Node;
import node.NodeType;
import token.Token;

/**
 * UnaryOp → '+' | '−' | '!'
 */
public class UnaryOpNode extends Node {
    private final Token opNode;

    public UnaryOpNode(Token opNode) {
        super(NodeType.UnaryOp);
        this.opNode = opNode;
    }

    public String getOp() {
        return opNode.getContent();
    }

    @Override
    public String toString() {
        return opNode + printNodeType();
    }
}
