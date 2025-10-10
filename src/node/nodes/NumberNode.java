package node.nodes;

import node.Node;
import node.NodeType;
import token.Token;

/**
 * Number → IntConst
 */
public class NumberNode extends Node {
    private final Token intConstToken;

    public NumberNode(Token intConstToken) {
        super(NodeType.Number);
        this.intConstToken = intConstToken;
    }

    @Override
    public String toString() {
        return intConstToken + printNodeType();
    }
}
