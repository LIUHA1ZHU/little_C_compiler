package node.nodes;

import node.Node;
import node.NodeType;

/**
 * BlockItem → Decl | Stmt
 */
public class BlockItemNode extends Node {
    private final DeclNode declNode;
    private final StmtNode stmtNode;

    public BlockItemNode(DeclNode declNode, StmtNode stmtNode) {
        super(NodeType.BlockItem);
        this.declNode = declNode;
        this.stmtNode = stmtNode;
    }

    @Override
    public String toString() {
        if (declNode != null) {
            return declNode.toString();
        } else {
            return stmtNode.toString();
        }
    }
}
