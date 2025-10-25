package node.nodes;

import node.ExpAlikeNode;
import node.Node;
import node.NodeType;
import token.Token;

/**
 * Number → IntConst
 */
public class NumberNode extends ExpAlikeNode {
    private final Token intConstToken;

    public NumberNode(Token intConstToken) {
        super(NodeType.Number);
        this.intConstToken = intConstToken;
    }

    @Override
    public void evaluate() {
        constValue = Integer.parseInt(intConstToken.getContent());
        isConst = true;
    }

    // in funcRParams type check, only check single-symbol exp
    public String propagateSymbolName() {
        return intConstToken.getContent();
    }

    @Override
    public String toString() {
        return intConstToken + printNodeType();
    }
}
