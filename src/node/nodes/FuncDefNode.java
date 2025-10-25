package node.nodes;

import node.Node;
import node.NodeType;
import token.Token;

/**
 *  FuncDef → FuncType Ident '(' [FuncFParams] ')' Block
 */
public class FuncDefNode extends Node {
    private final FuncTypeNode funcTypeNode;
    private final Token identToken;
    private final Token lParent;
    private final FuncFParamsNode funcFParamsNode;
    private final Token rParent;
    private final BlockNode blockNode;

    public FuncDefNode(FuncTypeNode funcTypeNode, Token identToken, Token lParent, FuncFParamsNode funcFParamsNode, Token rParent, BlockNode blockNode) {
        super(NodeType.FuncDef);
        this.funcTypeNode = funcTypeNode;
        this.identToken = identToken;
        this.lParent = lParent;
        this.funcFParamsNode = funcFParamsNode;
        this.rParent = rParent;
        this.blockNode = blockNode;
    }

    public FuncTypeNode getFuncTypeNode() {
        return funcTypeNode;
    }

    public Token getIdentToken() {
        return identToken;
    }

    public FuncFParamsNode getFuncFParamsNode() {
        return funcFParamsNode;
    }

    public BlockNode getBlockNode() {
        return blockNode;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(funcTypeNode).append(identToken).append(lParent);
        if (funcFParamsNode != null) {
            sb.append(funcFParamsNode);
        }
        sb.append(rParent).append(blockNode);
        return sb.append(printNodeType()).toString();
    }
}
