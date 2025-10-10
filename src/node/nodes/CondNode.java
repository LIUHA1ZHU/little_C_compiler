package node.nodes;

import node.Node;
import node.NodeType;

/**
 * Cond → LOrExp
 */
public class CondNode extends Node {
    private final LOrExpNode lOrExpNode;

    public CondNode(LOrExpNode lOrExpNode) {
        super(NodeType.Cond);
        this.lOrExpNode = lOrExpNode;
    }

    @Override
    public String toString() {
        return lOrExpNode + printNodeType();
    }
}
