package node.pseudoNodes;

import node.PseudoNode;
import node.PseudoNodeType;
import node.nodes.CondNode;
import node.nodes.ForStmtNode;
import node.nodes.StmtNode;
import token.Token;

/**
 * 'for' '(' [ForStmt] ';' [Cond] ';' [ForStmt] ')' Stmt
 */
public class ForNode extends PseudoNode {
    private final Token forToken;
    private final Token lParent;
    private final ForStmtNode forStmtNode1;
    private final Token semiToken1;
    private final CondNode condNode;
    private final Token semiToken2;
    private final ForStmtNode forStmtNode2;
    private final Token rParent;
    private final StmtNode stmtNode;

    public ForNode(Token forToken, Token lParent, ForStmtNode forStmtNode1, Token semiToken1, CondNode condNode,
                   Token semiToken2, ForStmtNode forStmtNode2, Token rParent, StmtNode stmtNode) {
        super(PseudoNodeType.For);
        this.forToken = forToken;
        this.lParent = lParent;
        this.forStmtNode1 = forStmtNode1;
        this.semiToken1 = semiToken1;
        this.condNode = condNode;
        this.semiToken2 = semiToken2;
        this.forStmtNode2 = forStmtNode2;
        this.rParent = rParent;
        this.stmtNode = stmtNode;
    }


    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(forToken).append(lParent);
        if (forStmtNode1 != null) {
            sb.append(forStmtNode1);
        }
        sb.append(semiToken1);
        if (condNode != null) {
            sb.append(condNode);
        }
        sb.append(semiToken2);
        if (forStmtNode2 != null) {
            sb.append(forStmtNode2);
        }
        sb.append(rParent).append(stmtNode);
        return sb.toString();
    }
}
