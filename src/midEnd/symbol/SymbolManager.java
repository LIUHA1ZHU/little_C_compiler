package midEnd.symbol;

import error.Error;
import error.ErrorHandler;
import error.ErrorType;
import utils.Config;
import utils.FileIO;

import java.util.ArrayList;
import java.util.Comparator;

public class SymbolManager {
    private static SymbolTable rootTable = null;
    private static SymbolTable curTable = null;
    private static ArrayList<SymbolTable> symbolLog;
    private static int curMaxScope = 1;

    private static int forLoopDepth = 0;

    public static void init() {
        rootTable = new SymbolTable(curMaxScope, null, null);
        curMaxScope++;
        curTable = rootTable;
        symbolLog = new ArrayList<>();
    }

    /**
     * create a new symbolTable. It will be called every time a block is traversed
     * @param funcSymbol if in function declaration, a funcSymbol is needed for error handling
     * @param inheritFuncSymbol whether to inherit the funcSymbol from father symbolTable.
     *                          True only if the block is a StmtItem.
     */
    public static void createTableAndChangeCur(FuncSymbol funcSymbol, boolean inheritFuncSymbol) {
        if (inheritFuncSymbol) funcSymbol = curTable.getCurFuncSymbol();
        SymbolTable newTable = new SymbolTable(curMaxScope, curTable, funcSymbol);
        curMaxScope++;
        curTable.addSonTable(newTable);
        curTable = newTable;
    }

    public static void goToFatherTable() {
        if (Config.SymbolOutput) {
            symbolLog.add(curTable);
        }
        curTable = curTable.getFatherTable();
    }

    public static void addSymbol(Symbol symbol) {
        if (!checkSymbolRedefined(symbol)) {
            // check variable shadowing
            Symbol potentialShadowing = getPreviousShadowing(symbol, curTable);
            Symbol sonsShadowing = recurGetSonsShadowing(symbol, curTable);
            if (potentialShadowing == null && sonsShadowing == null) { // first appearance
                symbol.setShadowingNum(0);
            } else {
                // get the symbol with max shadowingNum
                Symbol shadowing = potentialShadowing == null ? sonsShadowing :
                        (sonsShadowing == null ? potentialShadowing :
                                (potentialShadowing.getShadowingNum() > sonsShadowing.getShadowingNum() ? potentialShadowing : sonsShadowing));
                symbol.setShadowingNum(shadowing.getShadowingNum() + 1);
            }
            curTable.addSymbol(symbol);
        }
    }

    private static Symbol getPreviousShadowing(Symbol symbol, SymbolTable table) {
        if (table.containsSymbol(symbol)) {
            return table.getSymbolDefined(symbol.getName());
        }
        if (table.getFatherTable() != null) {
            return getPreviousShadowing(symbol, table.getFatherTable());
        }
        return null;
    }

    /**
     * check recursively if son table contains a shadowing variable
     */
    private static Symbol recurGetSonsShadowing(Symbol symbol, SymbolTable table) {
        Symbol maxShadowing = null;
        Symbol curShadowing = null;
        maxShadowing = table.getSymbolDefined(symbol.getName());
        for (SymbolTable sonTable : table.getSonTables()) {
            if (sonTable != table) {
                curShadowing = recurGetSonsShadowing(symbol, sonTable);
                maxShadowing = maxShadowing == null ? curShadowing :
                        (curShadowing.getShadowingNum() > maxShadowing.getShadowingNum() ? curShadowing : maxShadowing);
            }
        }
        return maxShadowing;
    }

    public static void end() {
        if (Config.SymbolOutput) {
            symbolLog.add(curTable);
        }
    }

    public static FuncSymbol getCurFuncSymbol() {
        return curTable.getCurFuncSymbol();
    }

    public static boolean getLastIsReturn() {
        return curTable.lastIsReturn();
    }

    public static void setLastIsReturn(boolean lastIsReturn) {
        curTable.setLastIsReturn(lastIsReturn);
    }

    /**
     * check recursively if a name is defined.
     * @return the symbol if defined
     */
    public static Symbol getSymbolDefined(String name, int lineNum) {
        try { // intConst or exp pardon
            Integer.parseInt(name.trim());
            return null;
        } catch (NumberFormatException e) { // symbol
            boolean isArrayElement = false;
            if (name.endsWith("[]")) {
                name = name.substring(0, name.length() - 2);
                isArrayElement = true;
            }
            Symbol symbol = curTable.getSymbolDefined(name);

            if (isArrayElement) return new Symbol(name+ "[]", Symbol.SymbolType.Int, symbol.getLineNum());
            if (symbol != null) return symbol;
            ErrorHandler.addError(new Error(ErrorType.c, lineNum));
            return null;
        }
    }

    public static boolean checkSymbolRedefined(Symbol symbol) {
        boolean defined = curTable.containsSymbol(symbol);
        if (defined) {
            ErrorHandler.addError(new Error(ErrorType.b, symbol.getLineNum()));
            return true;
        }
        return false;
    }

    public static boolean checkSymbolIsGlobal(Symbol symbol) {
        return rootTable.containsSymbol(symbol);
    }

    public static void enterForLoop() {
        forLoopDepth++;
    }

    public static void exitForLoop() {
        forLoopDepth--;
    }

    public static int getForLoopDepth() {
        return forLoopDepth;
    }

    public static void outputSymbol() {
        symbolLog.sort(Comparator.comparingInt(SymbolTable::getScope));
        StringBuilder sb = new StringBuilder();
        for (SymbolTable symbolTable : symbolLog) {
            sb.append(symbolTable.outputSymbol());
        }
        FileIO.write(FileIO.IOType.SEMANTICS, sb.toString());
    }
}
