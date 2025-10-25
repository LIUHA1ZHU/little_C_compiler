package node.nodes;

import node.Node;
import node.NodeType;

import java.util.ArrayList;

/**
 * CompUnit → {Decl} {FuncDef} MainFuncDef
 * All done
 */
public class CompUnitNode extends Node {
    private final ArrayList<DeclNode> declNodes;
    private final ArrayList<FuncDefNode> funcDefNodes;
    private final MainFuncDefNode mainFuncDefNode;

    public CompUnitNode(ArrayList<DeclNode> declNodes, ArrayList<FuncDefNode> funcDefNodes, MainFuncDefNode mainFuncDefNode) {
        super(NodeType.CompUnit);
        this.declNodes = declNodes;
        this.funcDefNodes = funcDefNodes;
        this.mainFuncDefNode = mainFuncDefNode;
    }

    public ArrayList<DeclNode> getDeclNodes() {
        return declNodes;
    }

    public ArrayList<FuncDefNode> getFuncDefNodes() {
        return funcDefNodes;
    }

    public MainFuncDefNode getMainFuncDefNode() {
        return mainFuncDefNode;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (DeclNode declNode : declNodes) {
            sb.append(declNode);
        }
        for (FuncDefNode funcDefNode : funcDefNodes) {
            sb.append(funcDefNode);
        }
        sb.append(mainFuncDefNode);
        return sb.append(printNodeType()).toString();
    }
}
