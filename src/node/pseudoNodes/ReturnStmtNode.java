package node.pseudoNodes;

import node.PseudoNode;
import node.PseudoNodeType;
import node.nodes.ExpNode;
import token.Token;

/**
 * 'return' [Exp] ';'
 */
public class ReturnStmtNode extends PseudoNode {
    private final Token returnToken;
    private final ExpNode expNode;
    private final Token semiToken;

    public ReturnStmtNode(Token returnToken, ExpNode expNode, Token semiToken) {
        super(PseudoNodeType.ReturnStmt);
        this.returnToken = returnToken;
        this.expNode = expNode;
        this.semiToken = semiToken;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(returnToken);
        if (expNode != null) {
            sb.append(expNode);
        }
        sb.append(semiToken);
        return sb.toString();
    }
}
