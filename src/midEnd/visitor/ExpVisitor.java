package midEnd.visitor;

import midEnd.ir.IrBuilder;
import midEnd.ir.IrValue;
import midEnd.ir.IrValueType;
import midEnd.ir.values.IrBasicBlock;
import midEnd.ir.values.IrConstant;
import midEnd.ir.values.IrVariable;
import midEnd.ir.values.instructions.*;
import midEnd.ir.values.instructions.IOInstructions.IrGetintInstruction;
import node.ExpAlikeNode;
import node.nodes.*;

import java.util.ArrayList;

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
            visitLOrExpNode(condNode.getlOrExpNode());
            condNode.getlOrExpNode().getTrueList().forEach(condNode::addToTrueList);
            condNode.getlOrExpNode().getFalseList().forEach(condNode::addToFalseList);
        }
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
        } else if (unaryExpNode.getUnaryOpNode() != null){ // + <unary>
            if (unaryExpNode.getUnaryOpNode().getOp().equals("+")) {
                return visitUnaryExpNode(unaryExpNode.getUnaryExpNode());
            } else if (unaryExpNode.getUnaryOpNode().getOp().equals("-")){ // - <unary>
                return new IrArithmeticInstruction(IrArithmeticInstruction.IrArithmeticType.sub, new IrConstant(0),
                        visitUnaryExpNode(unaryExpNode.getUnaryExpNode()));
            } else { // ! <unary>
                IrIcmpInstruction icmp = new IrIcmpInstruction(IrIcmpInstruction.IcmpCondType.eq,
                        visitUnaryExpNode(unaryExpNode.getUnaryExpNode()), new IrConstant(0));
                return new IrExtendInstruction(icmp);
            }
        } else if (unaryExpNode.getPrimaryExpNode() != null) { // inherit primary
            return visitPrimaryExpNode(unaryExpNode.getPrimaryExpNode());
        } else { // func call
            String name = unaryExpNode.getIdent().getContent();
            if (name.equals("getint")) {
                return new IrGetintInstruction();
            } else {
                return FuncCallVisitor.getFuncIr(unaryExpNode.getIdent(), unaryExpNode.getFuncRParamsNode());
            }
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

    //--------------------------------
    //              cond
    //--------------------------------
    private static void visitLOrExpNode(LOrExpNode lOrExpNode) {
        if (lOrExpNode.getOpToken() == null) {
            visitLAndExpNode(lOrExpNode.getlAndExpNode());
            lOrExpNode.getlAndExpNode().getTrueList().forEach(lOrExpNode::addToTrueList);
            lOrExpNode.getlAndExpNode().getFalseList().forEach(lOrExpNode::addToFalseList);
            return;
        }
        // E1
        visitLOrExpNode(lOrExpNode.getlOrExpNode());
        // generate BasicBlock & backPatch
        IrBasicBlock basicBlock = IrBuilder.createBasicBlock("cond" + IrBuilder.getBlockNum());
        lOrExpNode.getlOrExpNode().getFalseList().forEach(branch -> branch.setFalseDestination(basicBlock));
        // E2
        visitLAndExpNode(lOrExpNode.getlAndExpNode());
        lOrExpNode.getlOrExpNode().getTrueList().forEach(lOrExpNode::addToTrueList);
        lOrExpNode.getlAndExpNode().getTrueList().forEach(lOrExpNode::addToTrueList);
        lOrExpNode.getlAndExpNode().getFalseList().forEach(lOrExpNode::addToFalseList);
    }

    /**
     * all condition below this level must be IrBranchInstr here
     */
    private static void visitLAndExpNode(LAndExpNode lAndExpNode) {
        if (lAndExpNode.getOpToken() == null) {
            IrValue singleEqValue = visitEqExpNode(lAndExpNode.getEqExpNode());
            if (!(singleEqValue instanceof IrIcmpInstruction) && !(singleEqValue instanceof IrBranchInstruction)) {
                singleEqValue = new IrIcmpInstruction(IrIcmpInstruction.IcmpCondType.ne, singleEqValue, new IrConstant(0));
            }
            // if eq icmp not branch
            if (singleEqValue instanceof IrIcmpInstruction) {
                singleEqValue = new IrBranchInstruction(singleEqValue);
                lAndExpNode.addToTrueList((IrBranchInstruction) singleEqValue);
                lAndExpNode.addToFalseList((IrBranchInstruction) singleEqValue);
            }

            lAndExpNode.getEqExpNode().getTrueList().forEach(lAndExpNode::addToTrueList);
            lAndExpNode.getEqExpNode().getFalseList().forEach(lAndExpNode::addToFalseList);
            return;
        }
        // E1
        visitLAndExpNode(lAndExpNode.getlAndExpNode());
        // generate BasicBlock & backPatch
        IrBasicBlock basicBlock = IrBuilder.createBasicBlock("cond" + IrBuilder.getBlockNum());
        lAndExpNode.getlAndExpNode().getTrueList().forEach(branch -> branch.setTrueDestination(basicBlock));
        // E2
        IrValue e2Val = visitEqExpNode(lAndExpNode.getEqExpNode());
        if (!(e2Val instanceof IrIcmpInstruction) && !(e2Val instanceof IrBranchInstruction)) {
            e2Val = new IrIcmpInstruction(IrIcmpInstruction.IcmpCondType.ne, e2Val, new IrConstant(0));
        }
        // if eq icmp not branch
        if (e2Val instanceof IrIcmpInstruction) {
            e2Val = new IrBranchInstruction(e2Val);
            lAndExpNode.addToTrueList((IrBranchInstruction) e2Val);
            lAndExpNode.addToFalseList((IrBranchInstruction) e2Val);
        }
        lAndExpNode.getlAndExpNode().getFalseList().forEach(lAndExpNode::addToFalseList);
        lAndExpNode.getEqExpNode().getFalseList().forEach(lAndExpNode::addToFalseList);
        lAndExpNode.getEqExpNode().getTrueList().forEach(lAndExpNode::addToTrueList);
    }

    private static IrValue visitEqExpNode(EqExpNode eqExpNode) {
        if (eqExpNode.getOpToken() == null) {
            return visitRelExpNode(eqExpNode.getRelExpNode());
        }
        IrIcmpInstruction.IcmpCondType condType = switch (eqExpNode.getOpToken().getTokenType()) {
            case EQL -> IrIcmpInstruction.IcmpCondType.eq;
            case NEQ -> IrIcmpInstruction.IcmpCondType.ne;
            default -> throw new RuntimeException("WARNING: invalid eqToken");
        };
        IrValue val1 = visitEqExpNode(eqExpNode.getEqExpNode());
        IrValue val2 = visitRelExpNode(eqExpNode.getRelExpNode());
        IrValue cond = new IrIcmpInstruction(condType, val1, val2);
        IrBranchInstruction branchInstruction = new IrBranchInstruction(cond);
        //IrBuilder.createBasicBlock("cond" + IrBuilder.getBlockNum());
        eqExpNode.addToTrueList(branchInstruction);
        eqExpNode.addToFalseList(branchInstruction);
        return branchInstruction;

    }

    private static IrValue visitRelExpNode(RelExpNode relExpNode) {
        if (relExpNode.isConst()) {
            int number = relExpNode.getConstValue();
            return new IrConstant(number);
        }
        if (relExpNode.getOpToken() == null) {
            return visitAddExp(relExpNode.getAddExpNode());
        }
        IrIcmpInstruction.IcmpCondType condType = switch (relExpNode.getOpToken().getTokenType()) {
            case GRE -> IrIcmpInstruction.IcmpCondType.sgt;
            case GEQ -> IrIcmpInstruction.IcmpCondType.sge;
            case LSS -> IrIcmpInstruction.IcmpCondType.slt;
            case LEQ -> IrIcmpInstruction.IcmpCondType.sle;
            default -> throw new RuntimeException("Warning: invalid relToken");
        };
        return new IrIcmpInstruction(condType, visitRelExpNode(relExpNode.getRelExpNode()),
                visitAddExp(relExpNode.getAddExpNode()));

    }
}
