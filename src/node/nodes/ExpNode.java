package node.nodes;

import node.ExpAlikeNode;
import node.Node;
import node.NodeType;

/**
 * Exp → AddExp
 */
public class ExpNode extends ExpAlikeNode {
    private final AddExpNode addExpNode;

    public ExpNode(AddExpNode addExpNode) {
        super(NodeType.Exp);
        this.addExpNode = addExpNode;
    }

    @Override
    public void evaluate() {
        addExpNode.evaluate();
        if (addExpNode.isConst()) {
            isConst = true;
            constValue = addExpNode.getConstValue();
        }
    }

    public AddExpNode getAddExpNode() {
        return addExpNode;
    }

    // in funcRParams type check, only check single-symbol exp
    public String propagateSymbolName() {
        return addExpNode.propagateSymbolName();
    }

    @Override
    public String toString() {
        return addExpNode + printNodeType();
    }
}
