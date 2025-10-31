package midEnd.visitor;

import midEnd.ir.IrBuilder;
import midEnd.ir.IrValue;
import midEnd.ir.values.IrConstant;
import midEnd.ir.values.IrGlobalVariable;
import midEnd.ir.values.instructions.IrAllocaInstruction;
import midEnd.ir.values.instructions.IrStoreInstruction;
import midEnd.symbol.Symbol;
import midEnd.symbol.SymbolManager;
import midEnd.symbol.ValueSymbol;
import node.ExpAlikeNode;
import node.nodes.*;
import token.Token;

import java.util.ArrayList;

// TODO so ugly now
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
        //TODO global
        Token identToken = constDefNode.getIdentToken();
        String name = identToken.getContent();
        ValueSymbol valueSymbol;

        // get initVal first
        // ArrayList<IrConstant>
        ArrayList<IrValue> irInitVals = visitInitVal(constDefNode.getConstInitValNode());

        if (constDefNode.getConstExpNode() != null) { // Array type
            valueSymbol = new ValueSymbol(name, Symbol.SymbolType.ConstIntArray, identToken.getLineNum(), 0);
            SymbolManager.addSymbol(valueSymbol);
        } else {
            valueSymbol = new ValueSymbol(name, Symbol.SymbolType.ConstInt, identToken.getLineNum(), 1);
            SymbolManager.addSymbol(valueSymbol);
            // TODO check shadowing
            // alloca and store
            if (!IrBuilder.isInGlobal()) {
                IrValue variableAllocate = new IrAllocaInstruction(name + valueSymbol.getShadowingNum());
                new IrStoreInstruction(name + valueSymbol.getShadowingNum(), irInitVals.get(0), variableAllocate);
            } else {
                new IrGlobalVariable(name + valueSymbol.getShadowingNum(), irInitVals.get(0));
            }
        }


    }

    public static void visitVarDef(VarDefNode varDefNode, boolean isStatic) {
        //TODO add arrayLength info
        //TODO global
        String name = varDefNode.getIdentToken().getContent();
        int lineNum = varDefNode.getIdentToken().getLineNum();
        ValueSymbol valueSymbol;

        // get initVal first
        ArrayList<IrValue> irInitVals = null;
        if (varDefNode.getInitValNode() != null) {
            irInitVals = visitInitVal(varDefNode.getInitValNode());
        }

        if (varDefNode.getConstExpNode() != null) { // Array type
            if (isStatic) valueSymbol = new ValueSymbol(name, Symbol.SymbolType.StaticIntArray, lineNum, 0);
            else valueSymbol = new ValueSymbol(name, Symbol.SymbolType.IntArray, lineNum, 0);
            SymbolManager.addSymbol(valueSymbol);
        } else {
            if (isStatic) valueSymbol = new ValueSymbol(name, Symbol.SymbolType.StaticInt, lineNum, 1);
            else valueSymbol = new ValueSymbol(name, Symbol.SymbolType.Int, lineNum, 1);
            SymbolManager.addSymbol(valueSymbol);
            // TODO check shadowing

            // alloca and store
            if (!IrBuilder.isInGlobal()) {
                IrValue variableAllocate = new IrAllocaInstruction(name + valueSymbol.getShadowingNum());
                if (irInitVals != null) { // variable with initVal
                    new IrStoreInstruction(name + valueSymbol.getShadowingNum(), irInitVals.get(0), variableAllocate);
                }
            } else {
                new IrGlobalVariable(name + valueSymbol.getShadowingNum(), irInitVals != null ? irInitVals.get(0) : new IrConstant(0));
            }
        }


    }

    public static ArrayList<IrValue> visitInitVal(Init initValNode) {
        ArrayList<IrValue> irValueArrayList = new ArrayList<>();
        for (ExpAlikeNode expNode : initValNode.getExpNodes()) {
            irValueArrayList.add(ExpVisitor.visit(expNode));
        }
        return irValueArrayList;
    }
}
