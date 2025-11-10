package midEnd.symbol;

import midEnd.ir.IrValue;

import java.util.ArrayList;

public class ValueSymbol extends Symbol{
    private int length;

    private ArrayList<Integer> constValues;

    public ValueSymbol(String name, SymbolType symbolType, int lineNum, int length) {
        super(name, symbolType, lineNum);
        this.length = length;
        constValues = null;
    }

    public ValueSymbol(String name, SymbolType symbolType, int lineNum, int length, ArrayList<Integer> constValues) {
        super(name, symbolType, lineNum);
        this.length = length;
        this.constValues = constValues;
    }

    public ArrayList<Integer> getConstValues() {
        return constValues;
    }

    public int getLength() {
        return length;
    }
}
