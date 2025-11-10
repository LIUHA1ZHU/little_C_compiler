package node.nodes;

import node.CondAlikeNode;
import node.NodeType;

/**
 * Cond → LOrExp
 */
public class CondNode extends CondAlikeNode {
    private final LOrExpNode lOrExpNode;

    public CondNode(LOrExpNode lOrExpNode) {
        super(NodeType.Cond);
        this.lOrExpNode = lOrExpNode;
    }

    public LOrExpNode getlOrExpNode() {
        return lOrExpNode;
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
