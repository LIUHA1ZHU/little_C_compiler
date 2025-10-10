package node.nodes;

import node.Node;
import node.NodeType;
import token.Token;

/**
 * LOrExp → LAndExp | LOrExp '||' LAndExp
 */
public class LOrExpNode extends Node {
    private final LOrExpNode lOrExpNode;
    private final Token opToken;
    private final LAndExpNode lAndExpNode;

    public LOrExpNode(LOrExpNode lOrExpNode, Token opToken, LAndExpNode lAndExpNode) {
        super(NodeType.LOrExp);
        this.lOrExpNode = lOrExpNode;
        this.opToken = opToken;
        this.lAndExpNode = lAndExpNode;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (opToken != null) {
            sb.append(lOrExpNode).append(opToken).append(lAndExpNode);
        } else {
            sb.append(lAndExpNode);
        }
        return sb.append(printNodeType()).toString();
    }
}
