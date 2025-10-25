package node.nodes;

import node.ExpAlikeNode;
import node.Node;
import node.NodeType;
import token.Token;

/**
 * LOrExp → LAndExp | LOrExp '||' LAndExp
 */
public class LOrExpNode extends ExpAlikeNode {
    private final LOrExpNode lOrExpNode;
    private final Token opToken;
    private final LAndExpNode lAndExpNode;

    private boolean isConst = false;
    private int constValue = 0;

    public LOrExpNode(LOrExpNode lOrExpNode, Token opToken, LAndExpNode lAndExpNode) {
        super(NodeType.LOrExp);
        this.lOrExpNode = lOrExpNode;
        this.opToken = opToken;
        this.lAndExpNode = lAndExpNode;
    }

    @Override
    public void evaluate() {
        if (opToken == null) {
            lAndExpNode.evaluate();
            if (lAndExpNode.isConst()) {
                isConst = true;
                constValue = lAndExpNode.getConstValue();
            }
        } else {
            lAndExpNode.evaluate();
            lOrExpNode.evaluate();
            if (lOrExpNode.isConst() && lAndExpNode.isConst()) {
                isConst = true;
                constValue = lAndExpNode.getConstValue() | lOrExpNode.getConstValue();
            }
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (opToken != null) {
            sb.append(lOrExpNode).append(opToken).append(lAndExpNode);
        } else {
            sb.append(lAndExpNode);
        }
        return sb.append(printNodeType()).toString();
    }
}
