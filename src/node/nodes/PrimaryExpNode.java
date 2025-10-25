package node.nodes;

import midEnd.visitor.LValVisitor;
import node.ExpAlikeNode;
import node.Node;
import node.NodeType;
import token.Token;

/**
 * PrimaryExp → '(' Exp ')' | LVal | Number
 */
public class PrimaryExpNode extends ExpAlikeNode {
    private final Token lParent;
    private final ExpNode expNode;
    private final Token rParent;
    private final LValNode lValNode;
    private final NumberNode numberNode;

    public PrimaryExpNode(Token lParent, ExpNode expNode, Token rParent, LValNode lValNode, NumberNode numberNode) {
        super(NodeType.PrimaryExp);
        this.lParent = lParent;
        this.expNode = expNode;
        this.rParent = rParent;
        this.lValNode = lValNode;
        this.numberNode = numberNode;
    }

    @Override
    public void evaluate() {
        if (lValNode != null) {
            LValVisitor.visit(lValNode);
        } else if (numberNode != null) {
            // must be const
            numberNode.evaluate();
            constValue = numberNode.getConstValue();
            isConst = true;
        } else {
            if (expNode.isConst()) {
                expNode.evaluate();
                constValue = expNode.getConstValue();
                isConst = true;
            }
        }
    }

    // in funcRParams type check, only check single-symbol exp
    public String propagateSymbolName() {
        if (lValNode != null) {
            return lValNode.propagateSymbolName();
        } else if (numberNode != null) {
            return numberNode.propagateSymbolName();
        }
        return "0";
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (lParent != null) {
            sb.append(lParent).append(expNode).append(rParent);
        } else if (lValNode != null) {
            sb.append(lValNode);
        } else {
            sb.append(numberNode);
        }
        return sb.append(printNodeType()).toString();
    }
}
