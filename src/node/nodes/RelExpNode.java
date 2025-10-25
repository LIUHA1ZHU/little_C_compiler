package node.nodes;

import node.ExpAlikeNode;
import node.Node;
import node.NodeType;
import token.Token;

/**
 * RelExp → AddExp | RelExp ('<' | '>' | '<=' | '>=') AddExp
 */
public class RelExpNode extends ExpAlikeNode {
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
    public void evaluate() {
        if (opToken == null) {
            addExpNode.evaluate();
            if (addExpNode.isConst()) {
                isConst = true;
                constValue = addExpNode.getConstValue();
            }
        } else {
            relExpNode.evaluate();
            addExpNode.evaluate();
            if (relExpNode.isConst() && addExpNode.isConst()) {
                isConst = true;
                constValue = switch (opToken.getTokenType()) {
                    case LSS -> relExpNode.getConstValue() < addExpNode.getConstValue() ? 1 : 0;
                    case GRE -> relExpNode.getConstValue() > addExpNode.getConstValue() ? 1 : 0;
                    case LEQ -> relExpNode.getConstValue() <= addExpNode.getConstValue() ? 1 : 0;
                    case GEQ -> relExpNode.getConstValue() >= addExpNode.getConstValue() ? 1 : 0;
                    default -> throw new RuntimeException("invalid RelationOperator");
                };
            }
        }
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
