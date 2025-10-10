package node.nodes;

import node.Node;
import node.NodeType;
import token.Token;

/**
 * RelExp → AddExp | RelExp ('<' | '>' | '<=' | '>=') AddExp
 */
public class RelExpNode extends Node {
    private final RelExpNode relExpNode;
    private final Token opToken;
    private final AddExpNode addExpNode;

    public RelExpNode(RelExpNode relExpNode, Token opToken, AddExpNode addExpNode) {
        super(NodeType.RelExp);
        this.relExpNode = relExpNode;
        this.opToken = opToken;
        this.addExpNode = addExpNode;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (opToken != null) {
            sb.append(relExpNode).append(opToken).append(addExpNode);
        } else {
            sb.append(addExpNode);
        }
        return sb.append(printNodeType()).toString();
    }
}
