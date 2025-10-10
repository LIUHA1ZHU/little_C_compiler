package node.nodes;

import node.Node;
import node.NodeType;
import token.Token;

/**
 * EqExp → RelExp | EqExp ('==' | '!=') RelExp
 */
public class EqExpNode extends Node {
    private final EqExpNode eqExpNode;
    private final Token opToken;
    private final RelExpNode relExpNode;

    public EqExpNode(EqExpNode eqExpNode, Token opToken, RelExpNode relExpNode) {
        super(NodeType.EqExp);
        this.eqExpNode = eqExpNode;
        this.opToken = opToken;
        this.relExpNode = relExpNode;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (opToken != null) {
            sb.append(eqExpNode).append(opToken).append(relExpNode);
        } else {
            sb.append(relExpNode);
        }
        return sb.append(printNodeType()).toString();
    }
}
