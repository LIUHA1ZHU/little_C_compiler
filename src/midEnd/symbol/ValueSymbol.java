package midEnd.symbol;

public class ValueSymbol extends Symbol{
    private int length;

    public ValueSymbol(String name, SymbolType symbolType, int lineNum, int length) {
        super(name, symbolType, lineNum);
        this.length = length;
    }
}
