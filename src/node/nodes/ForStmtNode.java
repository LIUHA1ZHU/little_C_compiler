package node.nodes;

import node.Node;
import node.NodeType;
import token.Token;

import java.util.ArrayList;

/**
 *  ForStmt → LVal '=' Exp { ',' LVal '=' Exp }
 */
public class ForStmtNode extends Node {
    private final ArrayList<LValNode> lValNodes;
    private final ArrayList<Token> assignTokens;
    private final ArrayList<ExpNode> expNodes;
    private final ArrayList<Token> commaTokens;

    public ForStmtNode(ArrayList<LValNode> lValNodes, ArrayList<Token> assignTokens, ArrayList<ExpNode> expNodes, ArrayList<Token> commaTokens) {
        super(NodeType.ForStmt);
        this.lValNodes = lValNodes;
        this.assignTokens = assignTokens;
        this.expNodes = expNodes;
        this.commaTokens = commaTokens;
    }

    public ArrayList<LValNode> getlValNodes() {
        return lValNodes;
    }

    public ArrayList<ExpNode> getExpNodes() {
        return expNodes;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < lValNodes.size(); i++) {
            sb.append(lValNodes.get(i)).append(assignTokens.get(i)).append(expNodes.get(i));
            if (i != lValNodes.size() - 1) sb.append(commaTokens.get(i));
        }
        return sb.append(printNodeType()).toString();
    }
}
