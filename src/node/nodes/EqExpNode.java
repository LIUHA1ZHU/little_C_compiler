package node.nodes;

import node.CondAlikeNode;
import node.ExpAlikeNode;
import node.Node;
import node.NodeType;
import token.Token;

/**
 * EqExp → RelExp | EqExp ('==' | '!=') RelExp
 */
public class EqExpNode extends CondAlikeNode {
    private final EqExpNode eqExpNode;
    private final Token opToken;
    private final RelExpNode relExpNode;

    public EqExpNode(EqExpNode eqExpNode, Token opToken, RelExpNode relExpNode) {
        super(NodeType.EqExp);
        this.eqExpNode = eqExpNode;
        this.opToken = opToken;
        this.relExpNode = relExpNode;
    }

    public EqExpNode getEqExpNode() {
        return eqExpNode;
    }

    public Token getOpToken() {
        return opToken;
    }

    public RelExpNode getRelExpNode() {
        return relExpNode;
    }

    @Override
    public void evaluate() {
        if (opToken == null) {
            relExpNode.evaluate();
            if (relExpNode.isConst()) {
                isConst = true;
                constValue = relExpNode.getConstValue();
            }
        } else {
            eqExpNode.evaluate();
            relExpNode.evaluate();
            if (eqExpNode.isConst() && relExpNode.isConst()) {
                isConst = true;
                constValue = switch (opToken.getTokenType()) {
                    case EQL -> eqExpNode.getConstValue() == relExpNode.getConstValue() ? 1 : 0;
                    case NEQ -> eqExpNode.getConstValue() != relExpNode.getConstValue() ? 1 : 0;
                    default -> throw new RuntimeException("invalid EqualOperator");
                };
            }
        }
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
