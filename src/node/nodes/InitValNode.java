package node.nodes;

import node.Node;
import node.NodeType;
import token.Token;

import java.util.ArrayList;

/**
 * InitVal → Exp | '{' [ Exp { ',' Exp } ] '}'
 */
public class InitValNode extends Node {
    private final Token lBrace;
    private final ArrayList<ExpNode> expNodes;
    private final ArrayList<Token> commas;
    private final Token rBrace;

    public InitValNode(Token lBrace, ArrayList<ExpNode> expNodes, ArrayList<Token> commas, Token rBrace) {
        super(NodeType.InitVal);
        this.lBrace = lBrace;
        this.expNodes = expNodes;
        this.commas = commas;
        this.rBrace = rBrace;
    }

    public ArrayList<ExpNode> getExpNodes() {
        return expNodes;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (lBrace != null) {
            sb.append(lBrace);
        }
        for (int i = 0; i < expNodes.size(); i++) {
            sb.append(expNodes.get(i));
            if (i != expNodes.size() - 1) {
                sb.append(commas.get(i));
            }
        }
        if (rBrace != null) {
            sb.append(rBrace);
        }
        return sb.append(printNodeType()).toString();
    }
}
