package node.pseudoNodes;

import node.PseudoNode;
import node.PseudoNodeType;
import node.nodes.CondNode;
import node.nodes.StmtNode;
import token.Token;

/**
 * 'if' '(' Cond ')' Stmt [ 'else' Stmt ]
 */
public class IfStmtNode extends PseudoNode {
    private final Token ifToken;
    private final Token lParent;
    private final CondNode condNode;
    private final Token rParent;
    private final StmtNode stmtNodeIf;
    private final Token elseToken;
    private final StmtNode stmtNodeElse;

    public IfStmtNode(Token ifToken, Token lParent, CondNode condNode, Token rParent, StmtNode stmtNodeIf,
                      Token elseToken, StmtNode stmtNodeElse) {
        super(PseudoNodeType.IfStmt);
        this.ifToken = ifToken;
        this.lParent = lParent;
        this.condNode = condNode;
        this.rParent = rParent;
        this.stmtNodeIf = stmtNodeIf;
        this.elseToken = elseToken;
        this.stmtNodeElse = stmtNodeElse;
    }

    public CondNode getCondNode() {
        return condNode;
    }

    public StmtNode getStmtNodeIf() {
        return stmtNodeIf;
    }

    public StmtNode getStmtNodeElse() {
        return stmtNodeElse;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(ifToken).append(lParent).append(condNode).append(rParent).append(stmtNodeIf);
        if (stmtNodeElse != null) {
            sb.append(elseToken).append(stmtNodeElse);
        }
        return sb.toString();
    }
}
