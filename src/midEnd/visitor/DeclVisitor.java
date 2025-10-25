package midEnd.visitor;

import midEnd.symbol.Symbol;
import midEnd.symbol.SymbolManager;
import midEnd.symbol.ValueSymbol;
import node.nodes.*;
import token.Token;

public class DeclVisitor {

    public static void visit(DeclNode declNode) {
        ConstDeclNode constDeclNode = declNode.getConstDeclNode();
        if (constDeclNode != null) {
            visitConstDecl(constDeclNode);
            SymbolManager.setLastIsReturn(false);
            return;
        }
        VarDeclNode varDeclNode = declNode.getVarDeclNode();
        if (varDeclNode != null) {
            visitVarDecl(varDeclNode);
            SymbolManager.setLastIsReturn(false);
        }
    }

    public static void visitConstDecl(ConstDeclNode constDeclNode) {
        // const declarations must have 'const' token

        // only 'int' type
        for (ConstDefNode constDefNode : constDeclNode.getConstDefNodes()) {
            visitConstDef(constDefNode);
        }
    }

    public static void visitVarDecl(VarDeclNode varDeclNode) {
        boolean isStatic = (varDeclNode.getStaticToken() != null);

        // only 'int' type
        for (VarDefNode varDefNode : varDeclNode.getVarDefNodes()) {
            visitVarDef(varDefNode, isStatic);
        }
    }

    public static void visitConstDef(ConstDefNode constDefNode) {
        //TODO add arrayLength info
        Token identToken = constDefNode.getIdentToken();
        ValueSymbol valueSymbol;
        if (constDefNode.getConstExpNode() != null) { // Array type
            valueSymbol = new ValueSymbol(identToken.getContent(), Symbol.SymbolType.ConstIntArray, identToken.getLineNum(), 0);
        } else {
            valueSymbol = new ValueSymbol(identToken.getContent(), Symbol.SymbolType.ConstInt, identToken.getLineNum(), 1);
        }
        SymbolManager.addSymbol(valueSymbol);

        visitConstInitVal(constDefNode.getConstInitValNode());
    }

    public static void visitVarDef(VarDefNode varDefNode, boolean isStatic) {
        //TODO add arrayLength info
        String name = varDefNode.getIdentToken().getContent();
        int lineNum = varDefNode.getIdentToken().getLineNum();
        ValueSymbol valueSymbol;
        if (varDefNode.getConstExpNode() != null) { // Array type
            if (isStatic) valueSymbol = new ValueSymbol(name, Symbol.SymbolType.StaticIntArray, lineNum, 0);
            else valueSymbol = new ValueSymbol(name, Symbol.SymbolType.IntArray, lineNum, 0);
        } else {
            if (isStatic) valueSymbol = new ValueSymbol(name, Symbol.SymbolType.StaticInt, lineNum, 1);
            else valueSymbol = new ValueSymbol(name, Symbol.SymbolType.Int, lineNum, 1);
        }
        SymbolManager.addSymbol(valueSymbol);


        if (varDefNode.getInitValNode() != null) {
            visitVarInitVal(varDefNode.getInitValNode());
        }
    }

    public static void visitConstInitVal(ConstInitValNode constInitValNode) {
        for (ConstExpNode constExpNode : constInitValNode.getConstExpNodes()) {
            ExpVisitor.visitConstExp(constExpNode);
        }
    }

    public static void visitVarInitVal(InitValNode initValNode) {
        for (ExpNode expNode : initValNode.getExpNodes()) {
            ExpVisitor.visit(expNode);
        }
    }
}
