package midEnd.symbol;

public class Symbol {
    public enum SymbolType {
        ConstInt,       Int, 	        VoidFunc,
        ConstIntArray,  IntArray,       IntFunc,
        StaticInt,      StaticIntArray
    }
    private final String name;
    private final SymbolType symbolType;
    private final int lineNum;

    public Symbol(String name, SymbolType symbolType, int lineNum) {
        this.name = name;
        this.symbolType = symbolType;
        this.lineNum = lineNum;
    }

    public String getName() {
        return name;
    }

    public SymbolType getSymbolType() {
        return symbolType;
    }

    public int getLineNum() {
        return lineNum;
    }
}
