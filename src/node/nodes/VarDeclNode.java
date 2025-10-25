package node.nodes;

import node.Node;
import node.NodeType;
import token.Token;

import java.util.ArrayList;

/**
 *  VarDecl → [ 'static' ] BType VarDef { ',' VarDef } ';'
 */
public class VarDeclNode extends Node {
    private final Token staticToken;
    private final BTypeNode bTypeNode;
    private final ArrayList<VarDefNode> varDefNodes;
    private final ArrayList<Token> commas;
    private final Token semiToken;

    public VarDeclNode(Token staticToken, BTypeNode bTypeNode, ArrayList<VarDefNode> varDefNodes, ArrayList<Token> commas, Token semiToken) {
        super(NodeType.VarDecl);
        this.staticToken = staticToken;
        this.bTypeNode = bTypeNode;
        this.varDefNodes = varDefNodes;
        this.commas = commas;
        this.semiToken = semiToken;
    }

    public Token getStaticToken() {
        return staticToken;
    }

    public BTypeNode getbTypeNode() {
        return bTypeNode;
    }

    public ArrayList<VarDefNode> getVarDefNodes() {
        return varDefNodes;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (staticToken != null) {
            sb.append(staticToken);
        }
        sb.append(bTypeNode);
        for (int i = 0; i < varDefNodes.size(); i++) {
            sb.append(varDefNodes.get(i));
            if (i != varDefNodes.size() - 1) {
                sb.append(commas.get(i));
            }
        }
        sb.append(semiToken);
        return sb.append(printNodeType()).toString();
    }
}
