package node.pseudoNodes;

import midEnd.ir.values.instructions.IrBranchInstruction;
import node.PseudoNode;
import node.PseudoNodeType;
import node.nodes.CondNode;
import node.nodes.StmtNode;
import token.Token;

import java.util.ArrayList;

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

    private ArrayList<IrBranchInstruction> nextList = new ArrayList<>();

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

    public ArrayList<IrBranchInstruction> getNextList() {
        return nextList;
    }

    public void addToNextList(IrBranchInstruction instruction) {
        this.nextList.add(instruction);
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
