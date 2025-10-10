package node.nodes;

import node.Node;
import node.NodeType;

/**
 * ConstExp → AddExp
 */
public class ConstExpNode extends Node {
    private final AddExpNode addExpNode;

    public ConstExpNode(AddExpNode addExpNode) {
        super(NodeType.ConstExp);
        this.addExpNode = addExpNode;
    }

    @Override
    public String toString() {
        return addExpNode + printNodeType();
    }
}
