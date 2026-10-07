package midEnd.visitor;

import error.Error;
import error.ErrorHandler;
import error.ErrorType;
import midEnd.ir.IrBuilder;
import midEnd.ir.IrValue;
import midEnd.ir.values.IrConstant;
import midEnd.ir.values.IrFunction;
import midEnd.ir.values.IrVariable;
import midEnd.ir.values.instructions.IrAllocaInstruction;
import midEnd.ir.values.instructions.IrStoreInstruction;
import midEnd.symbol.FuncSymbol;
import midEnd.symbol.Symbol;
import midEnd.symbol.SymbolManager;
import midEnd.symbol.ValueSymbol;
import node.nodes.FuncDefNode;
import node.nodes.FuncFParamNode;
import token.Token;
import token.TokenType;

import java.util.ArrayList;

public class FuncDefVisitor {

    public static void visit(FuncDefNode funcDefNode) {

        String name = funcDefNode.getIdentToken().getContent();
        boolean isInt = funcDefNode.getFuncTypeNode().getFuncType().getTokenType().equals(TokenType.INTTK);
        // formal params
        ArrayList<Symbol> paramSymbolList = createParamSymbolList(funcDefNode);

        // create funcSymbol
        FuncSymbol funcSymbol;
        if (isInt) funcSymbol = new FuncSymbol(name, Symbol.SymbolType.IntFunc, funcDefNode.getIdentToken().getLineNum(), paramSymbolList);
        else funcSymbol = new FuncSymbol(name, Symbol.SymbolType.VoidFunc, funcDefNode.getIdentToken().getLineNum(), paramSymbolList);
        SymbolManager.addSymbol(funcSymbol);

        // create func IR
        IrFunction.IrFunctionType type = funcSymbol.getSymbolType().equals(Symbol.SymbolType.IntFunc) ? IrFunction.IrFunctionType.intFunc : IrFunction.IrFunctionType.voidFunc;
        IrValue fun = IrBuilder.createFunc(name, type);

        // formal params IrValue
        ArrayList<IrVariable> paramIRList = createParamIRList(paramSymbolList);
        funcSymbol.setIrValue(fun);
        IrBuilder.setParamIR(paramIRList);

        // add params & visit block
        SymbolManager.createTableAndChangeCur(funcSymbol, false);
        paramSymbolList.forEach(SymbolManager::addSymbol);

        IrBuilder.createBasicBlock("entry");
        allocateForParams(paramIRList, paramSymbolList);
        BlockVisitor.visit(funcDefNode.getBlockNode()); // visit block here
        IrBuilder.finishBasicBlockAndAddToFunc();

        if (!SymbolManager.getLastIsReturn() && funcSymbol.getSymbolType().equals(Symbol.SymbolType.IntFunc)) {
            ErrorHandler.addError(new Error(ErrorType.g, funcDefNode.getBlockNode().getrBrace().getLineNum()));
        }
        IrBuilder.finishFuncAndAddToModule();
        SymbolManager.goToFatherTable();
    }

    private static ArrayList<Symbol> createParamSymbolList(FuncDefNode funcDefNode) {
        if (funcDefNode.getFuncFParamsNode() == null) {
            return new ArrayList<>();
        }
        ArrayList<Symbol> paramSymbolList = new ArrayList<>();
        for (FuncFParamNode funcFParamNode : funcDefNode.getFuncFParamsNode().getFuncFParamNodes()) {
            ValueSymbol valueSymbol = createValueSymbol(funcFParamNode);
            paramSymbolList.add(valueSymbol);
        }
        return paramSymbolList;
    }

    private static ValueSymbol createValueSymbol(FuncFParamNode funcFParamNode) {
        ValueSymbol valueSymbol;
        Token identToken = funcFParamNode.getIdentToken();
        // must be 'int'
        if (funcFParamNode.getlBracket() != null) { // Array type
            // omit arrayLength here
            valueSymbol = new ValueSymbol(identToken.getContent(), Symbol.SymbolType.IntArray,
                    identToken.getLineNum(), 0);
        } else {
            valueSymbol = new ValueSymbol(identToken.getContent(), Symbol.SymbolType.Int,
                    identToken.getLineNum(), 1);
        }
        return valueSymbol;
    }

    private static ArrayList<IrVariable> createParamIRList(ArrayList<Symbol> symbols) {
        ArrayList<IrVariable> irVariables = new ArrayList<>();
        for (Symbol symbol : symbols) {
            IrVariable irVariable;
            if (symbol.getSymbolType().equals(Symbol.SymbolType.IntArray)) {
                irVariable = new IrVariable(symbol.getName(), false, ((ValueSymbol) symbol).getLength(), true);
            } else {
                irVariable = new IrVariable(symbol.getName(), false);
            }
            irVariables.add(irVariable);
        }
        return irVariables;
    }

    private static void allocateForParams(ArrayList<IrVariable> valList, ArrayList<Symbol> symbolList) {
        for (int i = 0; i < valList.size(); i++) {
            IrVariable irVariable = valList.get(i);
            Symbol symbol = symbolList.get(i);
            IrAllocaInstruction varAllocate;
            if (irVariable.isArray()) { // no need
                symbol.setIrValue(irVariable);
            } else {
                varAllocate = new IrAllocaInstruction(symbol.getName() + ".addr");
                new IrStoreInstruction("store", irVariable, varAllocate);
                symbol.setIrValue(varAllocate);
            }

        }
    }

}
