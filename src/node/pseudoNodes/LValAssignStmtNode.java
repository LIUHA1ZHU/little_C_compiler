package node.pseudoNodes;

import node.PseudoNode;
import node.PseudoNodeType;
import node.nodes.ExpNode;
import node.nodes.LValNode;
import token.Token;

/**
 *  LVal '=' Exp ';'
 */
public class LValAssignStmtNode extends PseudoNode {
    private final LValNode lValNode;
    private final Token assignToken;
    private final ExpNode expNode;
    private final Token semiToken;

    public LValAssignStmtNode(LValNode lValNode, Token assignToken, ExpNode expNode, Token semiToken) {
        super(PseudoNodeType.LValAssignStmt);
        this.lValNode = lValNode;
        this.assignToken = assignToken;
        this.expNode = expNode;
        this.semiToken = semiToken;
    }

    public LValNode getlValNode() {
        return lValNode;
    }

    public ExpNode getExpNode() {
        return expNode;
    }

    @Override
    public String toString() {
        return String.valueOf(lValNode) + assignToken + expNode + semiToken;
    }
}
