package node.nodes;

import node.ExpAlikeNode;
import node.Node;
import node.NodeType;

/**
 * Cond → LOrExp
 */
public class CondNode extends ExpAlikeNode {
    private final LOrExpNode lOrExpNode;

    public CondNode(LOrExpNode lOrExpNode) {
        super(NodeType.Cond);
        this.lOrExpNode = lOrExpNode;
    }

    @Override
    public void evaluate() {
        lOrExpNode.evaluate();
        if (lOrExpNode.isConst()) {
            isConst = true;
            constValue = lOrExpNode.getConstValue();
        }
    }

    @Override
    public String toString() {
        return lOrExpNode + printNodeType();
    }
}
