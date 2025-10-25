package node.nodes;

import node.ExpAlikeNode;
import node.Node;
import node.NodeType;

/**
 * ConstExp → AddExp
 */
public class ConstExpNode extends ExpAlikeNode {
    private final AddExpNode addExpNode;

    public ConstExpNode(AddExpNode addExpNode) {
        super(NodeType.ConstExp);
        this.addExpNode = addExpNode;
    }

    @Override
    public void evaluate() {
        addExpNode.evaluate();
        if (addExpNode.isConst()) {
            isConst = true;
            constValue = addExpNode.getConstValue();;
        }
    }

    @Override
    public String toString() {
        return addExpNode + printNodeType();
    }
}
