package node.nodes;

import node.Node;
import node.NodeType;
import token.Token;


import java.util.ArrayList;

/**
 * FuncRParams → Exp { ',' Exp }
 */
public class FuncRParamsNode extends Node {
    private ArrayList<ExpNode> expNodeList;
    private ArrayList<Token> commaList;

    public FuncRParamsNode(ArrayList<ExpNode> expNodeList, ArrayList<Token> commaList) {
        super(NodeType.FuncRParams);
        this.expNodeList = expNodeList;
        this.commaList = commaList;
    }


    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(expNodeList.get(0));
        for (int i = 0; i < commaList.size(); i++) {
            sb.append(commaList.get(i)).append(expNodeList.get(i + 1));
        }
        return sb.append(printNodeType()).toString();
    }
}
