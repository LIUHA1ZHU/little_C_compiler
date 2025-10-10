package node.nodes;

import node.Node;
import node.NodeType;
import token.Token;

/**
 * FuncType → 'void' | 'int'
 */
public class FuncTypeNode extends Node {
    private final Token funcType;

    public FuncTypeNode(Token funcType) {
        super(NodeType.FuncType);
        this.funcType = funcType;
    }

    @Override
    public String toString() {
        return funcType + printNodeType();
    }
}
