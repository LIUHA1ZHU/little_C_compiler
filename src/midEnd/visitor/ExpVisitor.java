package midEnd.visitor;

import node.nodes.CondNode;
import node.nodes.ConstExpNode;
import node.nodes.ExpNode;

public class ExpVisitor {

    public static void visit(ExpNode expNode) {
        expNode.evaluate();
    }

    public static void visitConstExp(ConstExpNode constExpNode) {
        constExpNode.evaluate();
    }

    public static void visitCond(CondNode condNode) {
        if (condNode != null) {
            condNode.evaluate();
        }
    }
}
