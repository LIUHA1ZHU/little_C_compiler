package node.nodes;

import node.CondAlikeNode;
import node.ExpAlikeNode;
import node.Node;
import node.NodeType;
import token.Token;

/**
 * LOrExp → LAndExp | LOrExp '||' LAndExp
 */
public class LOrExpNode extends CondAlikeNode {
    private final LOrExpNode lOrExpNode;
    private final Token opToken;
    private final LAndExpNode lAndExpNode;

    public LOrExpNode(LOrExpNode lOrExpNode, Token opToken, LAndExpNode lAndExpNode) {
        super(NodeType.LOrExp);
        this.lOrExpNode = lOrExpNode;
        this.opToken = opToken;
        this.lAndExpNode = lAndExpNode;
    }

    public LOrExpNode getlOrExpNode() {
        return lOrExpNode;
    }

    public Token getOpToken() {
        return opToken;
    }

    public LAndExpNode getlAndExpNode() {
        return lAndExpNode;
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
