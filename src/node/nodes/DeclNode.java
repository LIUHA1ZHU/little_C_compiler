package node.nodes;

import node.Node;
import node.NodeType;

/**
 * Decl → ConstDecl | VarDecl
 */
public class DeclNode extends Node {
    private final ConstDeclNode constDeclNode;
    private final VarDeclNode varDeclNode;

    public DeclNode(ConstDeclNode constDeclNode, VarDeclNode varDeclNode) {
        super(NodeType.Decl);
        this.constDeclNode = constDeclNode;
        this.varDeclNode = varDeclNode;
    }

    public ConstDeclNode getConstDeclNode() {
        return constDeclNode;
    }

    public VarDeclNode getVarDeclNode() {
        return varDeclNode;
    }

    @Override
    public String toString() {
        if (constDeclNode != null) {
            return constDeclNode.toString();
        } else {
            return varDeclNode.toString();
        }
    }
}
