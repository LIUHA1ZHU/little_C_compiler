package node.nodes;

import node.Node;
import node.NodeType;
import token.Token;

/**
 * AddExp → MulExp | AddExp ('+' | '−') MulExp
 */
public class AddExpNode extends Node {
    private final AddExpNode addExpNode;
    private final Token opToken;
    private final MulExpNode mulExpNode;

    public AddExpNode(AddExpNode addExpNode, Token opToken, MulExpNode mulExpNode) {
        super(NodeType.AddExp);
        this.addExpNode = addExpNode;
        this.opToken = opToken;
        this.mulExpNode = mulExpNode;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (opToken != null) {
            sb.append(addExpNode).append(opToken).append(mulExpNode);
        } else {
            sb.append(mulExpNode);
        }
        return sb.append(printNodeType()).toString();
    }
}
