package node.pseudoNodes;

import node.PseudoNode;
import node.PseudoNodeType;
import node.nodes.ExpNode;
import token.Token;

/**
 *  [Exp] ';'
 */
public class ExpStmtNode extends PseudoNode {
    private final ExpNode expNode;
    private final Token semiToken;

    public ExpStmtNode(ExpNode expNode, Token semiToken) {
        super(PseudoNodeType.ExpStmt);
        this.expNode = expNode;
        this.semiToken = semiToken;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (expNode != null) {
            sb.append(expNode);
        }
        sb.append(semiToken);
        return sb.toString();
    }
}
