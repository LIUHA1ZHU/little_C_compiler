package midEnd.visitor;

import error.Error;
import error.ErrorHandler;
import error.ErrorType;
import midEnd.ir.IrBuilder;
import midEnd.ir.IrValue;
import midEnd.ir.values.IrBasicBlock;
import midEnd.ir.values.instructions.IrBranchInstruction;
import midEnd.ir.values.instructions.IrInstructionType;
import midEnd.ir.values.instructions.IrReturnInstruction;
import midEnd.symbol.Symbol;
import midEnd.symbol.SymbolManager;
import node.PseudoNode;
import node.nodes.ExpNode;
import node.nodes.ForStmtNode;
import node.nodes.LValNode;
import node.nodes.StmtNode;
import node.pseudoNodes.*;
import token.Token;

import java.util.ArrayList;

public class StmtVisitor {

    public static void visit(StmtNode stmtNode) {
        if (stmtNode == null) {
            return;
        }
        if (stmtNode.getPseudoNode() != null) {
            visitPseudo(stmtNode.getPseudoNode());
        } else if (stmtNode.getBlockNode() != null) {
            SymbolManager.createTableAndChangeCur(null, true);

            BlockVisitor.visit(stmtNode.getBlockNode());

            SymbolManager.goToFatherTable();
            // for error check
            SymbolManager.setLastIsReturn(false);

        } else {
            visitSingleBranch(stmtNode.getSingleBranchToken());
        }
    }

    public static void visitPseudo(PseudoNode pseudoNode) {
        switch (pseudoNode.getPseudoNodeType()) {
            case LValAssignStmt -> visitLValAssign((LValAssignStmtNode) pseudoNode);
            case ExpStmt -> visitExp((ExpStmtNode) pseudoNode);
            case IfStmt -> visitIf((IfStmtNode) pseudoNode);
            case For -> visitFor((ForNode) pseudoNode);
            case ReturnStmt -> visitReturn((ReturnStmtNode) pseudoNode);
            case PrintfStmt -> visitPrintf((PrintfStmtNode) pseudoNode);
        }
    }

    public static void visitSingleBranch(Token singleBranchToken) {
        if (SymbolManager.getForLoopDepth() == 0) {
            ErrorHandler.addError(new Error(ErrorType.m, singleBranchToken.getLineNum()));
        }

        SymbolManager.setLastIsReturn(false);
    }

    // LVal '=' Exp ';'
    public static void visitLValAssign(LValAssignStmtNode lValAssignStmtNode) {
        Token lValToken = lValAssignStmtNode.getlValNode().getIdentToken();
        Symbol symbol = SymbolManager.getSymbolDefined(lValToken.getContent(), lValToken.getLineNum());
        if (symbol != null) {
            if (symbol.getSymbolType().equals(Symbol.SymbolType.ConstInt) || symbol.getSymbolType().equals(Symbol.SymbolType.ConstIntArray)) {
                ErrorHandler.addError(new Error(ErrorType.h, lValToken.getLineNum()));
            }
        }

        ExpVisitor.visit(lValAssignStmtNode.getExpNode());

        SymbolManager.setLastIsReturn(false);
    }

    public static void visitExp(ExpStmtNode expStmtNode) {
        if (expStmtNode.getExpNode() != null) {
            ExpVisitor.visit(expStmtNode.getExpNode());
        }

        SymbolManager.setLastIsReturn(false);
    }

    public static void visitIf(IfStmtNode ifStmtNode) {
        ExpVisitor.visitCond(ifStmtNode.getCondNode());
        StmtVisitor.visit(ifStmtNode.getStmtNodeIf());
        StmtVisitor.visit(ifStmtNode.getStmtNodeElse());

        SymbolManager.setLastIsReturn(false);
    }

    public static void visitFor(ForNode forNode) {
        visitForStmt(forNode.getForStmtNode1());
        ExpVisitor.visitCond(forNode.getCondNode());
        visitForStmt(forNode.getForStmtNode2());

        SymbolManager.enterForLoop();
        StmtVisitor.visit(forNode.getStmtNode());
        SymbolManager.exitForLoop();

        SymbolManager.setLastIsReturn(false);
    }

    /**
     * return instruction doesn't need to be used. So there's no need to return a IrValue
     * @param returnStmtNode to visit
     */
    public static void visitReturn(ReturnStmtNode returnStmtNode) {
        if (returnStmtNode.getExpNode() != null) { // return <exp>;
            if (SymbolManager.getCurFuncSymbol().getSymbolType().equals(Symbol.SymbolType.VoidFunc)) {
                ErrorHandler.addError(new Error(ErrorType.f, returnStmtNode.getReturnToken().getLineNum()));
            }
            IrValue expValue = ExpVisitor.visit(returnStmtNode.getExpNode());
            new IrReturnInstruction("ret", IrInstructionType.ReturnIntInstr, expValue);

        } else {
            //TODO
        }

        SymbolManager.setLastIsReturn(true);
    }

    public static void visitPrintf(PrintfStmtNode printfStmtNode) {
        //check string const
        String str = printfStmtNode.getStringConstToken().getContent();
        int FSCount = countFormatSpecifiers(str);
        if (FSCount != printfStmtNode.getExpNodes().size()) {
            ErrorHandler.addError(new Error(ErrorType.l, printfStmtNode.getPrintfToken().getLineNum()));
        }
        for (ExpNode expNode : printfStmtNode.getExpNodes()) {
            ExpVisitor.visit(expNode);
        }

        SymbolManager.setLastIsReturn(false);
    }

    public static void visitForStmt(ForStmtNode forStmtNode) {
        if (forStmtNode == null) return;
        ArrayList<LValNode> lValNodes = forStmtNode.getlValNodes();
        ArrayList<ExpNode> expNodes = forStmtNode.getExpNodes();
        for (int i = 0; i < lValNodes.size(); i++) {
            Token lValToken = lValNodes.get(i).getIdentToken();
            Symbol symbol = SymbolManager.getSymbolDefined(lValToken.getContent(), lValToken.getLineNum());
            if (symbol != null) {
                if (symbol.getSymbolType().equals(Symbol.SymbolType.ConstInt) || symbol.getSymbolType().equals(Symbol.SymbolType.ConstIntArray)) {
                    ErrorHandler.addError(new Error(ErrorType.h, lValToken.getLineNum()));
                }
            }
            ExpVisitor.visit(expNodes.get(i));
        }
    }

    private static int countFormatSpecifiers(String str) {
        int count = 0;
        for (int i = 0; i < str.length() - 1; i++) {
            if (str.charAt(i) == '%' && str.charAt(i + 1) == 'd') {
                count++;
            }
        }
        return count;
    }
}
