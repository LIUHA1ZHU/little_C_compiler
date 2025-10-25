package midEnd.symbol;

import error.Error;
import error.ErrorHandler;
import error.ErrorType;
import utils.FileIO;

import java.util.ArrayList;
import java.util.HashMap;

public class SymbolTable {
    private int scope;
    private final FuncSymbol curFuncSymbol;
    private SymbolTable fatherTable;
    private ArrayList<SymbolTable> sonTables;
    private int sonIndex;
    private ArrayList<Symbol> symbolList;
    private HashMap<String, Symbol> symbolMap;

    private boolean lastIsReturn = false;

    public SymbolTable(int scope, SymbolTable fatherTable, FuncSymbol curFuncSymbol) {
        this.scope = scope;
        this.curFuncSymbol = curFuncSymbol;
        this.fatherTable = fatherTable;
        this.sonTables = new ArrayList<>();
        this.sonIndex = 0;
        this.symbolList = new ArrayList<>();
        this.symbolMap = new HashMap<>();
    }

    public SymbolTable getFatherTable() {
        return fatherTable;
    }

    public int getScope() {
        return scope;
    }

    public FuncSymbol getCurFuncSymbol() {
        return curFuncSymbol;
    }

    public boolean lastIsReturn() {
        return lastIsReturn;
    }

    public void setLastIsReturn(boolean lastIsReturn) {
        this.lastIsReturn = lastIsReturn;
    }

    public void addSonTable(SymbolTable symbolTable) {
        sonTables.add(symbolTable);
    }

    public void addSymbol(Symbol symbol) {
        symbolList.add(symbol);
        symbolMap.put(symbol.getName(), symbol);
    }

    /**
     * recursively check symbol
     * @return null if not defined
     */
    public Symbol getSymbolDefined(String name) {
        Symbol sameNameSymbol = symbolMap.get(name);
        if (sameNameSymbol != null) {
            return sameNameSymbol;
        }
        if (fatherTable != null) {
            return fatherTable.getSymbolDefined(name);
        }
        return null;
    }

    public boolean containsSymbol(Symbol symbol) {
        return symbolMap.containsKey(symbol.getName());
    }

    public String outputSymbol() {
        StringBuilder sb = new StringBuilder();
        for (Symbol symbol : symbolList) {
            sb.append(scope).append(" ").append(symbol.getName()).append(" ").append(symbol.getSymbolType()).append("\n");
        }
        return sb.toString();
    }
}
