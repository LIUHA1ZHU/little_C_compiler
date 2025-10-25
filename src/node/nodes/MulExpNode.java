package node.nodes;

import node.ExpAlikeNode;
import node.Node;
import node.NodeType;
import token.Token;

/**
 * MulExp → UnaryExp | MulExp ('*' | '/' | '%') UnaryExp
 */
public class MulExpNode extends ExpAlikeNode {
    private final UnaryExpNode unaryExpNode;
    private final Token opToken;
    private final MulExpNode mulExpNode;

    public MulExpNode(UnaryExpNode unaryExpNode, Token opToken, MulExpNode mulExpNode) {
        super(NodeType.MulExp);
        this.unaryExpNode = unaryExpNode;
        this.opToken = opToken;
        this.mulExpNode = mulExpNode;
    }

    @Override
    public void evaluate() {
        if (opToken == null) {
            unaryExpNode.evaluate();
            if (unaryExpNode.isConst()) {
                isConst = true;
                constValue = unaryExpNode.getConstValue();
            }
        } else {
            unaryExpNode.evaluate();
            mulExpNode.evaluate();
            if (unaryExpNode.isConst() && mulExpNode.isConst()) {
                isConst = true;
                constValue = switch (opToken.getTokenType()) {
                    case MULT -> mulExpNode.getConstValue() * unaryExpNode.getConstValue();
                    case DIV -> mulExpNode.getConstValue() / unaryExpNode.getConstValue();
                    case MOD -> mulExpNode.getConstValue() % unaryExpNode.getConstValue();
                    default -> throw new RuntimeException("invalid mulOperator");
                };
            }
        }
    }

    // in funcRParams type check, only check single-symbol exp
    public String propagateSymbolName() {
        return unaryExpNode.propagateSymbolName();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (opToken != null) {
            sb.append(mulExpNode).append(opToken).append(unaryExpNode);
        } else {
            sb.append(unaryExpNode);
        }
        return sb.append(printNodeType()).toString();
    }
}
