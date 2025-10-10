package node.nodes;

import node.Node;
import node.NodeType;
import token.Token;
import token.TokenType;

/**
 * UnaryExp → PrimaryExp | Ident '(' [FuncRParams] ')' | UnaryOp UnaryExp
 */
public class UnaryExpNode extends Node {
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
