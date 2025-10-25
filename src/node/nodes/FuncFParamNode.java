package node.nodes;

import node.Node;
import node.NodeType;
import token.Token;

/**
 * FuncFParam → BType Ident ['[' ']']
 */
public class FuncFParamNode extends Node {
    private final BTypeNode bTypeNode;
    private final Token identToken;
    private final Token lBracket;
    private final Token rBracket;

    public FuncFParamNode(BTypeNode bTypeNode, Token identToken, Token lBracket, Token rBracket) {
        super(NodeType.FuncFParam);
        this.bTypeNode = bTypeNode;
        this.identToken = identToken;
        this.lBracket = lBracket;
        this.rBracket = rBracket;
    }

    public BTypeNode getbTypeNode() {
        return bTypeNode;
    }

    public Token getIdentToken() {
        return identToken;
    }

    public Token getlBracket() {
        return lBracket;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(bTypeNode).append(identToken);
        if (lBracket != null) {
            sb.append(lBracket).append(rBracket);
        }
        return sb.append(printNodeType()).toString();
    }
}
