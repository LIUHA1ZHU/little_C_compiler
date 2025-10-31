package node.nodes;

import midEnd.visitor.FuncCallVisitor;
import node.ExpAlikeNode;
import node.Node;
import node.NodeType;
import token.Token;
import token.TokenType;

/**
 * UnaryExp → PrimaryExp | Ident '(' [FuncRParams] ')' | UnaryOp UnaryExp
 */
public class UnaryExpNode extends ExpAlikeNode {
    private final PrimaryExpNode primaryExpNode;
    private final Token ident;
    private final Token lParent;
    private final FuncRParamsNode funcRParamsNode;
    private final Token rParent;
    private final UnaryOpNode unaryOpNode;
    private final UnaryExpNode unaryExpNode;

    public UnaryExpNode(PrimaryExpNode primaryExpNode, Token ident, Token lParent,
                        FuncRParamsNode funcRParamsNode, Token rParent, UnaryOpNode unaryOpNode, UnaryExpNode unaryExpNode) {
        super(NodeType.UnaryExp);
        this.primaryExpNode = primaryExpNode;
        this.ident = ident;
        this.lParent = lParent;
        this.funcRParamsNode = funcRParamsNode;
        this.rParent = rParent;
        this.unaryOpNode = unaryOpNode;
        this.unaryExpNode = unaryExpNode;
    }

    public PrimaryExpNode getPrimaryExpNode() {
        return primaryExpNode;
    }

    public Token getIdent() {
        return ident;
    }

    public FuncRParamsNode getFuncRParamsNode() {
        return funcRParamsNode;
    }

    public UnaryOpNode getUnaryOpNode() {
        return unaryOpNode;
    }

    public UnaryExpNode getUnaryExpNode() {
        return unaryExpNode;
    }

    @Override
    public void evaluate() {
        if (primaryExpNode != null) {
            primaryExpNode.evaluate();
            if (primaryExpNode.isConst()) {
                isConst = true;
                constValue = primaryExpNode.getConstValue();
            }
        } else if (unaryOpNode != null) {
            unaryExpNode.evaluate();
            if (unaryExpNode.isConst()) {
                isConst = true;
                constValue = switch (unaryOpNode.getOp()) {
                    case "+" -> unaryExpNode.getConstValue();
                    case "-" -> - unaryExpNode.getConstValue();
                    case "!" -> unaryExpNode.getConstValue() == 0 ? 1 : 0;
                    default -> throw new RuntimeException("invalid unaryOperator");
                };
            }
        } else { // Ident '(' [FuncRParams] ')'
            FuncCallVisitor.visit(ident, funcRParamsNode);
        }
    }

    // in funcRParams type check, only check single-symbol exp
    public String propagateSymbolName() {
        if (primaryExpNode != null) {
            return primaryExpNode.propagateSymbolName();
        } else if (ident != null) {
            return ident.getContent();
        }
        return "0";
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (unaryOpNode != null) {
            sb.append(unaryOpNode).append(unaryExpNode);
        } else if (ident != null) {
            sb.append(ident).append(lParent);
            if (funcRParamsNode != null) {
                sb.append(funcRParamsNode);
            }
            sb.append(rParent);
        } else {
            sb.append(primaryExpNode);
        }
        return sb.append(printNodeType()).toString();
    }
}
