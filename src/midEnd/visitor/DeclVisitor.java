package midEnd.visitor;

import midEnd.ir.IrBuilder;
import midEnd.ir.IrValue;
import midEnd.ir.values.IrBasicBlock;
import midEnd.ir.values.IrConstant;
import midEnd.ir.values.IrGlobalVariable;
import midEnd.ir.values.instructions.*;
import midEnd.symbol.Symbol;
import midEnd.symbol.SymbolManager;
import midEnd.symbol.ValueSymbol;
import node.ExpAlikeNode;
import node.nodes.*;
import token.Token;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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

    //--------------------------------

    //--------------------------------
    public static ArrayList<IrValue> visitInitVal(Init initValNode) {
        ArrayList<IrValue> irValueArrayList = new ArrayList<>();
        if (initValNode == null) return irValueArrayList;

        for (ExpAlikeNode expNode : initValNode.getExpNodes()) {
            irValueArrayList.add(ExpVisitor.visit(expNode));
        }
        return irValueArrayList;
    }

    public static void visitConstDef(ConstDefNode constDefNode) {

        Init initNode = constDefNode.getConstInitValNode();
        ArrayList<IrValue> initVals = visitInitVal(initNode);
        if (constDefNode.getConstExpNode() != null) {
            constDefNode.getConstExpNode().evaluate();
        }

        if (IrBuilder.isInGlobal()) {
            if (constDefNode.getConstExpNode() != null) visitGlobalConstArray(constDefNode, initVals);
            else visitGlobalOrLocalConst(constDefNode, initVals);
        } else {
            if (constDefNode.getConstExpNode() != null) visitLocalConstArray(constDefNode, initVals);
            else visitGlobalOrLocalConst(constDefNode, initVals);
        }
    }

    public static void visitVarDef(VarDefNode varDefNode, boolean isStatic) {

        Init initNode = varDefNode.getInitValNode();
        ArrayList<IrValue> initVals = visitInitVal(initNode);
        if (varDefNode.getConstExpNode() != null) {
            varDefNode.getConstExpNode().evaluate();
        }

        if (IrBuilder.isInGlobal()) { // no static in global
            if (varDefNode.getConstExpNode() != null) visitGlobalArray(varDefNode, initVals);
            else visitGlobalVariable(varDefNode, initVals);
        } else {
            if (varDefNode.getConstExpNode() != null) {
                if (isStatic) visitStaticArray(varDefNode, initVals);
                else visitLocalArray(varDefNode, initVals);
            } else {
                if (isStatic) visitStaticVariable(varDefNode, initVals);
                else visitLocalVariable(varDefNode, initVals);
            }
        }
    }

    private static void visitGlobalConstArray(ConstDefNode constDefNode, ArrayList<IrValue> initVals) {
        // initVals must be const
        Token identToken = constDefNode.getIdentToken();
        String name = identToken.getContent();
        int length = constDefNode.getConstExpNode().getConstValue();
        ArrayList<Integer> constVals = new ArrayList<>();
        for (IrValue irValue : initVals) {
            constVals.add(((IrConstant) irValue).getConstValue());
        }
        int i = constVals.size();
        for (; i < length; i++) {
            constVals.add(0);
            initVals.add(new IrConstant(0));
        }

        ValueSymbol valueSymbol = new ValueSymbol(name, Symbol.SymbolType.ConstIntArray, identToken.getLineNum(), length, constVals);
        SymbolManager.addSymbol(valueSymbol);
        IrGlobalVariable val = new IrGlobalVariable(name, length, initVals,true);
        valueSymbol.setIrValue(val);
    }

    private static void visitGlobalOrLocalConst(ConstDefNode constDefNode, ArrayList<IrValue> initVals) {
        // initVals must be const
        Token identToken = constDefNode.getIdentToken();
        String name = identToken.getContent();
        ArrayList<Integer> constVals = new ArrayList<>();
        for (IrValue irValue : initVals) {
            constVals.add(((IrConstant) irValue).getConstValue());
        }

        ValueSymbol valueSymbol = new ValueSymbol(name, Symbol.SymbolType.ConstInt, identToken.getLineNum(), 1, constVals);
        SymbolManager.addSymbol(valueSymbol);
    }

    private static void visitLocalConstArray(ConstDefNode constDefNode, ArrayList<IrValue> initVals) {
        // initVals must be const
        Token identToken = constDefNode.getIdentToken();
        String name = identToken.getContent();
        int length = constDefNode.getConstExpNode().getConstValue();
        ArrayList<Integer> constVals = new ArrayList<>();
        for (IrValue irValue : initVals) {
            constVals.add(((IrConstant) irValue).getConstValue());
        }
        int i = constVals.size();
        for (; i < length; i++) {
            constVals.add(0);
        }

        ValueSymbol valueSymbol = new ValueSymbol(name, Symbol.SymbolType.ConstIntArray, identToken.getLineNum(), length, constVals);
        SymbolManager.addSymbol(valueSymbol);

        IrAllocaInstruction alloca = new IrAllocaInstruction(name, length);
        for (i = 0; i < constVals.size(); i++) {
            IrValue val = new IrConstant(constVals.get(i));
            IrGEPInstruction gep = new IrGEPInstruction(alloca, new IrConstant(i));
            new IrStoreInstruction("store", val, gep);
        }
        valueSymbol.setIrValue(alloca);
    }

    private static void visitGlobalArray(VarDefNode varDefNode, ArrayList<IrValue> initVals) {
        // initVals must be const
        Token identToken = varDefNode.getIdentToken();
        String name = identToken.getContent();
        int length = varDefNode.getConstExpNode().getConstValue();
        ArrayList<Integer> constVals = new ArrayList<>();
        for (IrValue irValue : initVals) {
            constVals.add(((IrConstant) irValue).getConstValue());
        }
        int  i = constVals.size();
        for (; i < length; i++) {
            constVals.add(0);
            initVals.add(new IrConstant(0));
        }

        Symbol.SymbolType symbolType = Symbol.SymbolType.IntArray;
        ValueSymbol valueSymbol = new ValueSymbol(name, symbolType, identToken.getLineNum(), length);
        SymbolManager.addSymbol(valueSymbol);
        IrGlobalVariable val = new IrGlobalVariable(name, length, initVals, true);

        valueSymbol.setIrValue(val);
    }

    private static void visitGlobalVariable(VarDefNode varDefNode, ArrayList<IrValue> initVals) {
        // initVals must be const
        Token identToken = varDefNode.getIdentToken();
        String name = identToken.getContent();

        Symbol.SymbolType symbolType = Symbol.SymbolType.Int;
        ValueSymbol valueSymbol = new ValueSymbol(name, symbolType, identToken.getLineNum(), 1);
        SymbolManager.addSymbol(valueSymbol);
        IrGlobalVariable val = new IrGlobalVariable(name, 1, initVals, false);

        valueSymbol.setIrValue(val);
    }

    private static void visitStaticArray(VarDefNode varDefNode, ArrayList<IrValue> initVals) {
        Token identToken = varDefNode.getIdentToken();
        String name = identToken.getContent();
        int length = varDefNode.getConstExpNode().getConstValue();
        int  i = initVals.size();
        for (; i < length; i++) {
            initVals.add(new IrConstant(0));
        }

        Symbol.SymbolType symbolType = Symbol.SymbolType.StaticIntArray;
        ValueSymbol valueSymbol = new ValueSymbol(name, symbolType, identToken.getLineNum(), length);
        SymbolManager.addSymbol(valueSymbol);
        IrGlobalVariable val = new IrGlobalVariable(name + "." + SymbolManager.getCurScope(), length, initVals, true);

        if (varDefNode.getInitValNode() != null) {
            initStatic(val, initVals);
        }

        valueSymbol.setIrValue(val);
    }

    private static void visitStaticVariable(VarDefNode varDefNode, ArrayList<IrValue> initVals) {
        Token identToken = varDefNode.getIdentToken();
        String name = identToken.getContent();

        Symbol.SymbolType symbolType = Symbol.SymbolType.StaticInt;
        ValueSymbol valueSymbol = new ValueSymbol(name, symbolType, identToken.getLineNum(), 1);
        SymbolManager.addSymbol(valueSymbol);
        IrGlobalVariable val = new IrGlobalVariable(name + "." + SymbolManager.getCurScope(), 1, initVals, false);

        if (varDefNode.getInitValNode() != null) {
            initStatic(val, initVals);
        }

        valueSymbol.setIrValue(val);
    }

    private static void visitLocalArray(VarDefNode varDefNode, ArrayList<IrValue> initVals) {
        Token identToken = varDefNode.getIdentToken();
        String name = identToken.getContent();
        int length = varDefNode.getConstExpNode().getConstValue();

        ValueSymbol valueSymbol = new ValueSymbol(name, Symbol.SymbolType.IntArray, identToken.getLineNum(), length);
        SymbolManager.addSymbol(valueSymbol);

        IrAllocaInstruction alloca = new IrAllocaInstruction(name, length);
        for (int i = 0; i < initVals.size(); i++) {
            IrValue val = initVals.get(i);
            IrGEPInstruction gep = new IrGEPInstruction(alloca, new IrConstant(i));
            new IrStoreInstruction("store", val, gep);
        }
        valueSymbol.setIrValue(alloca);
    }

    private static void visitLocalVariable(VarDefNode varDefNode, ArrayList<IrValue> initVals){
        Token identToken = varDefNode.getIdentToken();
        String name = identToken.getContent();

        ValueSymbol valueSymbol = new ValueSymbol(name, Symbol.SymbolType.Int, identToken.getLineNum(), 1);
        SymbolManager.addSymbol(valueSymbol);

        IrValue variableAllocate = new IrAllocaInstruction(name + valueSymbol.getShadowingNum());
        if (!initVals.isEmpty()) { // variable with initVal
            new IrStoreInstruction("store", initVals.get(0), variableAllocate);
        }
        valueSymbol.setIrValue(variableAllocate);
    }

    private static void initStatic(IrGlobalVariable val, ArrayList<IrValue> initVals) {
        IrGlobalVariable guard = new IrGlobalVariable("guard." + val.getName().replace("@", ""), 1,
                new ArrayList<>(List.of(new IrConstant(0))), false);

        IrLoadInstruction loadGuard = new IrLoadInstruction(guard);
        IrIcmpInstruction icmp = new IrIcmpInstruction(IrIcmpInstruction.IcmpCondType.eq, loadGuard, new IrConstant(0));
        IrBranchInstruction branchToInit = new IrBranchInstruction(icmp);

        IrBasicBlock initBlock = IrBuilder.createBasicBlock("init" + IrBuilder.getBlockNum());
        branchToInit.setTrueDestination(initBlock);
        if (val.isArray()) {
            for (int j = 0; j < initVals.size(); j++) {
                IrGEPInstruction gep = new IrGEPInstruction(val, new IrConstant(j));
                new IrStoreInstruction("store", initVals.get(j), gep);
            }
        } else {
            new IrStoreInstruction("store", initVals.get(0), val);
        }
        new IrStoreInstruction("store", new IrConstant(1), guard);

        IrBranchInstruction branchToEnd = new IrBranchInstruction(null);
        IrBasicBlock endBlock = IrBuilder.createBasicBlock("initEnd" + IrBuilder.getBlockNum());
        branchToInit.setFalseDestination(endBlock);
        branchToEnd.setTrueDestination(endBlock);
    }
}
