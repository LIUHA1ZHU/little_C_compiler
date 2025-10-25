package node.nodes;

import node.Node;
import node.NodeType;
import token.Token;

import java.util.ArrayList;

/**
 *  ConstExp | '{' [ ConstExp { ',' ConstExp } ] '}'
 */
public class ConstInitValNode extends Node {
    private final Token lBrace;
    private final ArrayList<ConstExpNode> constExpNodes;
    private final ArrayList<Token> commas;
    private final Token rBrace;

    public ConstInitValNode(Token lBrace, ArrayList<ConstExpNode> constExpNodes, ArrayList<Token> commas, Token rBrace) {
        super(NodeType.ConstInitVal);
        this.lBrace = lBrace;
        this.constExpNodes = constExpNodes;
        this.commas = commas;
        this.rBrace = rBrace;
    }

    public ArrayList<ConstExpNode> getConstExpNodes() {
        return constExpNodes;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (lBrace != null) {
            sb.append(lBrace);
        }
        for (int i = 0; i < constExpNodes.size(); i++) {
            sb.append(constExpNodes.get(i));
            if (i != constExpNodes.size() - 1) {
                sb.append(commas.get(i));
            }
        }
        if (rBrace != null) {
            sb.append(rBrace);
        }
        return sb.append(printNodeType()).toString();
    }
}
