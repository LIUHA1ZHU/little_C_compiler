package node.nodes;

import node.ExpAlikeNode;
import node.Node;
import node.NodeType;
import token.Token;

/**
 * LAndExp → EqExp | LAndExp '&&' EqExp
 */
public class LAndExpNode extends ExpAlikeNode {
    private final LAndExpNode lAndExpNode;
    private final Token opToken;
    private final EqExpNode eqExpNode;

    public LAndExpNode(LAndExpNode lAndExpNode, Token opToken, EqExpNode eqExpNode) {
        super(NodeType.LAndExp);
        this.lAndExpNode = lAndExpNode;
        this.opToken = opToken;
        this.eqExpNode = eqExpNode;
    }

    @Override
    public void evaluate() {
        if (opToken == null) {
            eqExpNode.evaluate();
            if (eqExpNode.isConst()) {
                isConst = true;
                constValue = eqExpNode.getConstValue();
            }
        } else {
            lAndExpNode.evaluate();
            eqExpNode.evaluate();
            if (eqExpNode.isConst() && lAndExpNode.isConst()) {
                isConst = true;
                constValue = lAndExpNode.getConstValue() & eqExpNode.getConstValue();
            }
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (opToken != null) {
            sb.append(lAndExpNode).append(opToken).append(eqExpNode);
        } else {
            sb.append(eqExpNode);
        }
        return sb.append(printNodeType()).toString();
    }
}
