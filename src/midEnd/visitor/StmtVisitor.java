package midEnd.visitor;

import error.Error;
import error.ErrorHandler;
import error.ErrorType;
import midEnd.ir.IrBuilder;
import midEnd.ir.IrValue;
import midEnd.ir.values.IrBasicBlock;
import midEnd.ir.values.IrStringConstant;
import midEnd.ir.values.IrVariable;
import midEnd.ir.values.instructions.*;
import midEnd.ir.values.instructions.IOInstructions.IrPutintInstruction;
import midEnd.ir.values.instructions.IOInstructions.IrPutstrInstruction;
import midEnd.symbol.Symbol;
import midEnd.symbol.SymbolManager;
import node.PseudoNode;
import node.nodes.ExpNode;
import node.nodes.ForStmtNode;
import node.nodes.LValNode;
import node.nodes.StmtNode;
import node.pseudoNodes.*;
import token.Token;
import token.TokenType;

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
        if (ErrorHandler.hasError()) {
            SymbolManager.setLastIsReturn(false);
            return;
        }
        if (singleBranchToken.getTokenType().equals(TokenType.CONTINUETK)) {
            IrBranchInstruction branch = new IrBranchInstruction(null);
            IrBuilder.getForStepStack().peek().add(branch);
        } else { // must be Break
            IrBranchInstruction branch = new IrBranchInstruction(null);
            IrBuilder.getForEndStack().peek().add(branch);
        }
        IrBuilder.createBasicBlock("afterSingleBranch" + IrBuilder.getBlockNum());

        SymbolManager.setLastIsReturn(false);
    }

    // LVal '=' Exp ';'
    public static void visitLValAssign(LValAssignStmtNode lValAssignStmtNode) {
        Token lValToken = lValAssignStmtNode.getlValNode().getIdentToken();
        Symbol symbol = SymbolManager.getSymbolDefined(lValToken.getContent(), lValToken.getLineNum());
        if (symbol != null) {
            if (symbol.getSymbolType().equals(Symbol.SymbolType.ConstInt) || symbol.getSymbolType().equals(Symbol.SymbolType.ConstIntArray)) {
                ErrorHandler.addError(new Error(ErrorType.h, lValToken.getLineNum()));
                SymbolManager.setLastIsReturn(false);
                return;
            }

            // build IR
            lValAssignStmtNode.getExpNode().evaluate();
            IrValue val = ExpVisitor.visit(lValAssignStmtNode.getExpNode());

            LValNode lVal = lValAssignStmtNode.getlValNode();
            if (lVal.getExpNode() != null) { // Array
                lVal.getExpNode().evaluate();
                IrValue index = ExpVisitor.visit(lVal.getExpNode());
                IrGEPInstruction gep = new IrGEPInstruction(symbol.getIrValue(), index);
                new IrStoreInstruction("store", val, gep);
            } else {
                String name = symbol.getName();
                new IrStoreInstruction("store", val, symbol.getIrValue());
            }
        }

        SymbolManager.setLastIsReturn(false);
    }

    public static void visitExp(ExpStmtNode expStmtNode) {
        if (expStmtNode.getExpNode() != null) {
            ExpVisitor.visit(expStmtNode.getExpNode());
        }

        SymbolManager.setLastIsReturn(false);
    }

    public static void visitIf(IfStmtNode ifStmtNode) {
        IrBranchInstruction toCond = new IrBranchInstruction(null);
        IrBasicBlock ifBlock = IrBuilder.createBasicBlock("if" + IrBuilder.getBlockNum());
        toCond.setTrueDestination(ifBlock);

        ExpVisitor.visitCond(ifStmtNode.getCondNode());

        if (ifStmtNode.getStmtNodeElse() == null) {
            // generate BasicBlock & backPatch
            IrBasicBlock basicBlock = IrBuilder.createBasicBlock("if.then" + IrBuilder.getBlockNum());
            ifStmtNode.getCondNode().getTrueList().forEach(branch -> branch.setTrueDestination(basicBlock));
            // visit then stmt
            StmtVisitor.visit(ifStmtNode.getStmtNodeIf());
            IrBranchInstruction branch = new IrBranchInstruction(null);
            // merge nextList
            ifStmtNode.getCondNode().getFalseList().forEach(ifStmtNode::addToNextList);
            ifStmtNode.addToNextList(branch);

            // create next BasicBlock
            IrBasicBlock nextBlock = IrBuilder.createBasicBlock("outIf" + IrBuilder.getBlockNum());
            ifStmtNode.getNextList().forEach(branch1 -> branch1.setFalseDestination(nextBlock));

        } else {
            // generate BasicBlock & backPatch
            IrBasicBlock basicBlock = IrBuilder.createBasicBlock("if.then" + IrBuilder.getBlockNum());
            ifStmtNode.getCondNode().getTrueList().forEach(branch -> branch.setTrueDestination(basicBlock));
            // visit then stmt
            StmtVisitor.visit(ifStmtNode.getStmtNodeIf());
            IrBranchInstruction branchThen = new IrBranchInstruction(null);
            // generate else BasicBlock & backPatch
            IrBasicBlock elseBlock = IrBuilder.createBasicBlock("if.else" + IrBuilder.getBlockNum());
            ifStmtNode.getCondNode().getFalseList().forEach(branch1 -> branch1.setFalseDestination(elseBlock));

            // visit else stmt
            StmtVisitor.visit(ifStmtNode.getStmtNodeElse());
            IrBranchInstruction branchElse = new IrBranchInstruction(null);

            // merge nextList
            ifStmtNode.addToNextList(branchThen);
            ifStmtNode.addToNextList(branchElse);
            // create next BasicBlock
            IrBasicBlock nextBlock = IrBuilder.createBasicBlock("outIf" + IrBuilder.getBlockNum());
            ifStmtNode.getNextList().forEach(branch1 -> branch1.setTrueDestination(nextBlock));
        }

        SymbolManager.setLastIsReturn(false);
    }

    public static void visitFor(ForNode forNode) {
        if (!ErrorHandler.hasError()) {
            IrBuilder.getForStepStack().push(new ArrayList<>());
            IrBuilder.getForEndStack().push(new ArrayList<>());
        }

        visitForStmt(forNode.getForStmtNode1());

        IrBranchInstruction branchToCond = new IrBranchInstruction(null);
        IrBasicBlock condBlock = IrBuilder.createBasicBlock("for.cond" + IrBuilder.getBlockNum());
        branchToCond.setTrueDestination(condBlock);
        // visit cond
        ExpVisitor.visitCond(forNode.getCondNode());
        IrBranchInstruction branchNoCond = null;
        if (forNode.getCondNode() == null) {
             branchNoCond = new IrBranchInstruction(null);
        }

        // visit step block
        IrBasicBlock forStepBlock = IrBuilder.createBasicBlock("for.step" + IrBuilder.getBlockNum());
        visitForStmt(forNode.getForStmtNode2());
        IrBranchInstruction branchStepToCond = new IrBranchInstruction(null);
        branchStepToCond.setTrueDestination(condBlock);

        // create for body
        IrBasicBlock forBodyBlock = IrBuilder.createBasicBlock("for.body" + IrBuilder.getBlockNum());
        if (forNode.getCondNode() == null) {
            branchNoCond.setTrueDestination(forBodyBlock);
        }

        if (forNode.getCondNode() != null) {
            forNode.getCondNode().getTrueList().forEach(branch1 -> branch1.setTrueDestination(forBodyBlock));
        }
        // visit for body
        SymbolManager.enterForLoop();
        StmtVisitor.visit(forNode.getStmtNode());
        SymbolManager.exitForLoop();

        // end for block
        IrBranchInstruction branchBodyToStep = new IrBranchInstruction(null);
        branchBodyToStep.setTrueDestination(forStepBlock);
        IrBasicBlock forEndBlock = IrBuilder.createBasicBlock("for.end" + IrBuilder.getBlockNum());
        if (forNode.getCondNode() != null) {
            forNode.getCondNode().getFalseList().forEach(branch2 -> branch2.setFalseDestination(forEndBlock));
        }

        // set break & continue
        if (!ErrorHandler.hasError()) {
            ArrayList<IrBranchInstruction> toSteps = IrBuilder.getForStepStack().pop();
            toSteps.forEach(branch3 -> branch3.setTrueDestination(forStepBlock));
            ArrayList<IrBranchInstruction> toEnds = IrBuilder.getForEndStack().pop();
            toEnds.forEach(branch3 -> branch3.setTrueDestination(forEndBlock));
        }

        SymbolManager.setLastIsReturn(false);
    }

    /**
     * return instruction doesn't need to be used. So there's no need to return an IrValue
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
            new IrReturnInstruction("ret", IrInstructionType.ReturnVoidInstr);
        }

        SymbolManager.setLastIsReturn(true);
    }

    public static void visitPrintf(PrintfStmtNode printfStmtNode) {
        //check string const
        String str = printfStmtNode.getStringConstToken().getContent();
        int FSCount = countFormatSpecifiers(str);
        if (FSCount != printfStmtNode.getExpNodes().size()) {
            ErrorHandler.addError(new Error(ErrorType.l, printfStmtNode.getPrintfToken().getLineNum()));
            SymbolManager.setLastIsReturn(false);
            return;
        }
        ArrayList<IrValue> expValues = new ArrayList<>();
        for (ExpNode expNode : printfStmtNode.getExpNodes()) {
            expValues.add(ExpVisitor.visit(expNode));
        }
        // remove " " mark
        splitAndPrint(str.replaceAll("^\"|\"$", ""), expValues);

        SymbolManager.setLastIsReturn(false);
    }

    public static void visitForStmt(ForStmtNode forStmtNode) {
        if (forStmtNode == null) return;
        ArrayList<LValNode> lValNodes = forStmtNode.getlValNodes();
        ArrayList<ExpNode> expNodes = forStmtNode.getExpNodes();

        for (LValAssignStmtNode lValAssign : forStmtNode.getlValAssignStmtNodes()) {
            visitLValAssign(lValAssign);
        }

//        for (int i = 0; i < lValNodes.size(); i++) {
//            Token lValToken = lValNodes.get(i).getIdentToken();
//            Symbol symbol = SymbolManager.getSymbolDefined(lValToken.getContent(), lValToken.getLineNum());
//            if (symbol != null) {
//                if (symbol.getSymbolType().equals(Symbol.SymbolType.ConstInt) || symbol.getSymbolType().equals(Symbol.SymbolType.ConstIntArray)) {
//                    ErrorHandler.addError(new Error(ErrorType.h, lValToken.getLineNum()));
//                }
//            }
//            ExpVisitor.visit(expNodes.get(i));
//        }
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

    private static void splitAndPrint(String input, ArrayList<IrValue> expValues) {
        if (input == null || input.isEmpty()) {
            return ;
        }

        int start = 0;
        int index;
        int expIndex = 0;
        while ((index = input.indexOf("%d", start)) != -1) {
            if (index > start) {
                new IrPutstrInstruction(new IrStringConstant(input.substring(start, index)));
            }
            new IrPutintInstruction(expValues.get(expIndex));
            expIndex++;
            start = index + 2;
        }

        if (start < input.length()) {
            new IrPutstrInstruction(new IrStringConstant(input.substring(start)));
        }
    }
}
