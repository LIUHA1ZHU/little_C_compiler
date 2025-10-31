package node.nodes;

import node.ExpAlikeNode;
import node.Node;
import node.NodeType;
import token.Token;

/**
 * AddExp → MulExp | AddExp ('+' | '−') MulExp
 */
public class AddExpNode extends ExpAlikeNode {
    private final AddExpNode addExpNode;
    private final Token opToken;
    private final MulExpNode mulExpNode;

    public AddExpNode(AddExpNode addExpNode, Token opToken, MulExpNode mulExpNode) {
        super(NodeType.AddExp);
        this.addExpNode = addExpNode;
        this.opToken = opToken;
        this.mulExpNode = mulExpNode;
    }

    public AddExpNode getAddExpNode() {
        return addExpNode;
    }

    public Token getOpToken() {
        return opToken;
    }

    public MulExpNode getMulExpNode() {
        return mulExpNode;
    }

    @Override
    public void evaluate() {
        if (opToken == null) {
            mulExpNode.evaluate();
            if (mulExpNode.isConst()) {
                isConst = true;
                constValue = mulExpNode.getConstValue();
            }
        } else {
            mulExpNode.evaluate();
            addExpNode.evaluate();
            if (mulExpNode.isConst() && addExpNode.isConst()) {
                isConst = true;
                constValue = switch (opToken.getTokenType()) {
                    case PLUS -> addExpNode.getConstValue() + mulExpNode.getConstValue();
                    case MINU -> addExpNode.getConstValue() - mulExpNode.getConstValue();
                    default -> throw new RuntimeException("invalid Add/Minus Operator");
                };
            }
        }
    }

    // in funcRParams type check, only check single-symbol exp
    public String propagateSymbolName() {
        return mulExpNode.propagateSymbolName();
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
