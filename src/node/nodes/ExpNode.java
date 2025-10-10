package node.nodes;

import node.Node;
import node.NodeType;

/**
 * Exp → AddExp
 */
public class ExpNode extends Node {
    private final AddExpNode addExpNode;

    public ExpNode(AddExpNode addExpNode) {
        super(NodeType.Exp);
        this.addExpNode = addExpNode;
    }

    @Override
    public String toString() {
        return addExpNode + printNodeType();
    }
}
