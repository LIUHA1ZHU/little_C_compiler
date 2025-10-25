package node.nodes;

import node.Node;
import node.NodeType;
import token.Token;

/**
 *  MainFuncDef → 'int' 'main' '(' ')' Block
 */
public class MainFuncDefNode extends Node {
    private final Token intToken;
    private final Token mainToken;
    private final Token lParent;
    private final Token rParent;
    private final BlockNode blockNode;

    public MainFuncDefNode(Token intToken, Token mainToken, Token lParent, Token rParent, BlockNode blockNode) {
        super(NodeType.MainFuncDef);
        this.intToken = intToken;
        this.mainToken = mainToken;
        this.lParent = lParent;
        this.rParent = rParent;
        this.blockNode = blockNode;
    }

    public Token getMainToken() {
        return mainToken;
    }

    public BlockNode getBlockNode() {
        return blockNode;
    }

    @Override
    public String toString() {
        return String.valueOf(intToken) + mainToken + lParent + rParent + blockNode + printNodeType();
    }
}
