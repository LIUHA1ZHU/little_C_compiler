package node.nodes;

import node.Node;
import node.NodeType;
import token.Token;

import java.util.ArrayList;

/**
 * FuncFParams → FuncFParam { ',' FuncFParam }
 */
public class FuncFParamsNode extends Node {
    private final ArrayList<FuncFParamNode> funcFParamNodes;
    private final ArrayList<Token> commas;

    public FuncFParamsNode(ArrayList<FuncFParamNode> funcFParamNodes, ArrayList<Token> commas) {
        super(NodeType.FuncFParams);
        this.funcFParamNodes = funcFParamNodes;
        this.commas = commas;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < funcFParamNodes.size(); i++) {
            sb.append(funcFParamNodes.get(i));
            if (i != funcFParamNodes.size() - 1) {
                sb.append(commas.get(i));
            }
        }
        return sb.append(printNodeType()).toString();
    }
}
