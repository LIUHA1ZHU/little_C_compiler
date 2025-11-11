package midEnd.visitor;

import error.Error;
import error.ErrorHandler;
import error.ErrorType;
import midEnd.ir.IrValue;
import midEnd.ir.values.IrConstant;
import midEnd.ir.values.IrFunction;
import midEnd.ir.values.instructions.IrCallInstruction;
import midEnd.symbol.FuncSymbol;
import midEnd.symbol.Symbol;
import midEnd.symbol.SymbolManager;
import midEnd.symbol.ValueSymbol;
import node.nodes.ExpNode;
import node.nodes.FuncRParamsNode;
import token.Token;

import java.util.ArrayList;

public class FuncCallVisitor {

    /**
     * Called in evaluate.
     * check error in function call:
     *      function undefined
     *      number of parameters unmatched
     *      type of parameters unmatched
     * @param identToken identifier of the function name
     * @param funcRParamsNode contains real parameters
     */
    public static void visit(Token identToken, FuncRParamsNode funcRParamsNode) {
        if (identToken.getContent().equals("getint")) { // pardon
            return;
        }
        ArrayList<Symbol> symbols = new ArrayList<>();
        if (funcRParamsNode == null) {
            funcRParamsNode = new FuncRParamsNode(new ArrayList<>(), new ArrayList<>());
        }
        for (ExpNode expNode : funcRParamsNode.getExpNodeList()) {
            //param names like arr[0] are arr[] here
            Symbol symbol = SymbolManager.getSymbolDefined(expNode.propagateSymbolName(), identToken.getLineNum());

            if (symbol != null) {
                symbols.add(symbol);
            } else {
                // placeHolder for a const or exp
                symbols.add(new Symbol("placeHolder", Symbol.SymbolType.Int, 0));
            }
        }
        checkFuncCall(identToken, symbols);
    }

    public static IrValue getFuncIr(Token identToken, FuncRParamsNode funcRParamsNode) {
        if (ErrorHandler.hasError()) return new IrConstant(0);

        FuncSymbol funcSymbol = SymbolManager.getFuncSymbol(identToken.getContent());
        IrValue funcIr = funcSymbol.getIrValue();

        ArrayList<IrValue> params = new ArrayList<>();

        ArrayList<ExpNode> realParams = new ArrayList<>();
        if (funcRParamsNode != null) {
               realParams = funcRParamsNode.getExpNodeList();
        }
        for (ExpNode exp : realParams) {
            // ignore exp like `arr + 1` where arr is an array
            params.add(ExpVisitor.visit(exp));
        }

        return new IrCallInstruction((IrFunction) funcIr, params);
    }

    private static void checkFuncCall(Token identToken, ArrayList<Symbol> funcRParamSymbols) {
        Symbol symbol = SymbolManager.getSymbolDefined(identToken.getContent(), identToken.getLineNum());
        if (symbol instanceof ValueSymbol) {
            ErrorHandler.addError(new Error(ErrorType.c, identToken.getLineNum()));
            return;
        }
        if (symbol == null) return;

        int funcRPNum = ((FuncSymbol) symbol).getParams().size();
        if (funcRPNum != funcRParamSymbols.size()) {
            ErrorHandler.addError(new Error(ErrorType.d, identToken.getLineNum()));
            return;
        }

        // check funcRParams type
        ArrayList<Symbol> formalParams = ((FuncSymbol) symbol).getParams();
        for (int i = 0; i < funcRPNum; i++) {
            if (formalParams.get(i).getSymbolType().equals(Symbol.SymbolType.Int)) {
                // int
                Symbol.SymbolType realType = funcRParamSymbols.get(i).getSymbolType();
                if (!(realType.equals(Symbol.SymbolType.Int) || realType.equals(Symbol.SymbolType.ConstInt)
                        || realType.equals(Symbol.SymbolType.StaticInt) || realType.equals(Symbol.SymbolType.IntFunc))) {
                    ErrorHandler.addError(new Error(ErrorType.e, identToken.getLineNum()));
                }
            } else if (formalParams.get(i).getSymbolType().equals(Symbol.SymbolType.IntArray)) {
                // array
                Symbol.SymbolType realType = funcRParamSymbols.get(i).getSymbolType();
                if (!(realType.equals(Symbol.SymbolType.IntArray) || realType.equals(Symbol.SymbolType.ConstIntArray)
                        || realType.equals(Symbol.SymbolType.StaticIntArray))) {
                    ErrorHandler.addError(new Error(ErrorType.e, identToken.getLineNum()));
                }
            }
        }
    }
}
