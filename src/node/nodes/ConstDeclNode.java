package node.nodes;

import node.Node;
import node.NodeType;
import token.Token;

import java.util.ArrayList;

/**
 * ConstDecl → 'const' BType ConstDef { ',' ConstDef } ';'
 */
public class ConstDeclNode extends Node {
    private final Token constToken;
    private final BTypeNode bTypeNode;
    private final ArrayList<ConstDefNode> constDefNodes;
    private final ArrayList<Token> commas;
    private final Token semiToken;

    public ConstDeclNode(Token constToken, BTypeNode bTypeNode, ArrayList<ConstDefNode> constDefNodes, ArrayList<Token> commas, Token semiToken) {
        super(NodeType.ConstDecl);
        this.constToken = constToken;
        this.bTypeNode = bTypeNode;
        this.constDefNodes = constDefNodes;
        this.commas = commas;
        this.semiToken = semiToken;
    }

    public Token getConstToken() {
        return constToken;
    }

    public BTypeNode getbTypeNode() {
        return bTypeNode;
    }

    public ArrayList<ConstDefNode> getConstDefNodes() {
        return constDefNodes;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(constToken).append(bTypeNode);
        for (int i = 0; i < constDefNodes.size(); i++) {
            sb.append(constDefNodes.get(i));
            if (i != constDefNodes.size() -1) {
                sb.append(commas.get(i));
            }
        }
        sb.append(semiToken);
        return sb.append(printNodeType()).toString();
    }
}
