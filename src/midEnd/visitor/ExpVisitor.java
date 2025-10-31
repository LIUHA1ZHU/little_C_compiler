package midEnd.visitor;

import midEnd.ir.IrValue;
import midEnd.ir.IrValueType;
import midEnd.ir.values.IrConstant;
import midEnd.ir.values.IrVariable;
import midEnd.ir.values.instructions.IrArithmeticInstruction;
import node.ExpAlikeNode;
import node.nodes.*;

public class ExpVisitor {

    /**
     *
     * @param expNode to evaluate & build Ir
     * @return an IrValue
     */
    public static IrValue visit(ExpAlikeNode expNode) {
        expNode.evaluate();
        return visitExp(expNode);
    }

    public static void visitCond(CondNode condNode) {
        if (condNode != null) {
            condNode.evaluate();
            //TODO
        }
        //TODO
    }

    private static IrValue visitExp(ExpAlikeNode expNode) {
        if (expNode.isConst()) {
            int number = expNode.getConstValue();
            return new IrConstant(number);
        } else {
            return visitAddExp(((ExpNode) expNode).getAddExpNode());
        }
    }

    private static IrValue visitAddExp(AddExpNode addExpNode) {
        if (addExpNode.isConst()) {
            int number = addExpNode.getConstValue();
            return new IrConstant(number);
        } else {
            if (addExpNode.getOpToken() == null) {
                return visitMulExp(addExpNode.getMulExpNode());
            }

            IrArithmeticInstruction.IrArithmeticType arithmeticType = switch (addExpNode.getOpToken().getTokenType()) {
                case PLUS -> IrArithmeticInstruction.IrArithmeticType.add;
                case MINU -> IrArithmeticInstruction.IrArithmeticType.sub;
                default -> throw new RuntimeException("Warning: invalid addToken");
            };
            return new IrArithmeticInstruction(arithmeticType, visitAddExp(addExpNode.getAddExpNode()),
                    visitMulExp(addExpNode.getMulExpNode()));
        }
    }

    private static IrValue visitMulExp(MulExpNode mulExpNode) {
        if (mulExpNode.isConst()) {
            int number = mulExpNode.getConstValue();
            return new IrConstant(number);
        } else {
            if (mulExpNode.getOpToken() == null) {
                return visitUnaryExpNode(mulExpNode.getUnaryExpNode());
            }

            IrArithmeticInstruction.IrArithmeticType arithmeticType = switch (mulExpNode.getOpToken().getTokenType()) {
                case MULT -> IrArithmeticInstruction.IrArithmeticType.mul;
                case DIV -> IrArithmeticInstruction.IrArithmeticType.sdiv;
                case MOD -> IrArithmeticInstruction.IrArithmeticType.srem;
                default -> throw new RuntimeException("Warning: invalid mulToken");
            };
            return new IrArithmeticInstruction(arithmeticType, visitMulExp(mulExpNode.getMulExpNode()),
                    visitUnaryExpNode(mulExpNode.getUnaryExpNode()));
        }
    }

    private static IrValue visitUnaryExpNode(UnaryExpNode unaryExpNode) {
        if (unaryExpNode.isConst()) {
            int number = unaryExpNode.getConstValue();
            return new IrConstant(number);
        } else if (unaryExpNode.getUnaryOpNode() != null){ // + <unary> / - <unary>
            if (unaryExpNode.getUnaryOpNode().getOp().equals("+")) {
                return visitUnaryExpNode(unaryExpNode.getUnaryExpNode());
            } else {
                return new IrArithmeticInstruction(IrArithmeticInstruction.IrArithmeticType.sub, new IrConstant(0),
                        visitUnaryExpNode(unaryExpNode.getUnaryExpNode()));
            }
        } else if (unaryExpNode.getPrimaryExpNode() != null) { // inherit primary
            return visitPrimaryExpNode(unaryExpNode.getPrimaryExpNode());
        } else { //TODO funcCall
            return null;
        }
    }

    private static IrValue visitPrimaryExpNode(PrimaryExpNode primaryExpNode) {
        if (primaryExpNode.isConst()) {
            int number = primaryExpNode.getConstValue();
            return new IrConstant(number);
        } else if (primaryExpNode.getlValNode() != null) { // LVal
            return LValVisitor.visit(primaryExpNode.getlValNode());
        } else { // non-constant Exp
            return visitExp(primaryExpNode.getExpNode());
        }
    }

    private static IrValue visitNumber(NumberNode numberNode) {
        int number = numberNode.getConstValue();
        return new IrConstant(number);
    }
}
