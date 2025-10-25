package node.nodes;

import node.Node;
import node.NodeType;
import node.PseudoNode;
import token.Token;

/**
 *  Stmt → LVal '=' Exp ';'                                     LValAssign
 *  | [Exp] ';'                                                 ExpStmt
 *  | Block
 *  | 'if' '(' Cond ')' Stmt [ 'else' Stmt ]                    IfStmt
 *  | 'for' '(' [ForStmt] ';' [Cond] ';' [ForStmt] ')' Stmt     For
 *  | 'break' ';' | 'continue' ';'
 *  | 'return' [Exp] ';'                                        ReturnStmt
 *  'printf''('StringConst {','Exp}')'';'                       PrintfStmt
 */
public class StmtNode extends Node {
    private final PseudoNode pseudoNode;
    private final BlockNode blockNode;
    private final Token singleBranchToken;
    private final Token semiToken;          // this semiToken is just for 'break' & 'continue' stmt

    public StmtNode(PseudoNode pseudoNode, BlockNode blockNode, Token singleBranchToken, Token semiToken) {
        super(NodeType.Stmt);
        this.pseudoNode = pseudoNode;
        this.blockNode = blockNode;
        this.singleBranchToken = singleBranchToken;
        this.semiToken = semiToken;
    }

    public PseudoNode getPseudoNode() {
        return pseudoNode;
    }

    public BlockNode getBlockNode() {
        return blockNode;
    }

    public Token getSingleBranchToken() {
        return singleBranchToken;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (pseudoNode != null) {
            sb.append(pseudoNode);
        } else if (blockNode != null) {
            sb.append(blockNode);
        } else {
            sb.append(singleBranchToken).append(semiToken);
        }
        return sb.append(printNodeType()).toString();
    }
}
