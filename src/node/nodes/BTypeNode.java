package node.nodes;

import node.Node;
import node.NodeType;
import token.Token;

/**
 * BType → 'int'
 */
public class BTypeNode extends Node {
    private final Token intToken;

    public BTypeNode(Token intToken) {
        super(NodeType.BType);
        this.intToken = intToken;
    }

    @Override
    public String toString() {
        return intToken.toString();
    }
}
