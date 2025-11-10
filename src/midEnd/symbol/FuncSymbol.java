package midEnd.symbol;

import midEnd.ir.IrValue;

import java.util.ArrayList;

public class FuncSymbol extends Symbol{
    private ArrayList<Symbol> params;

    public FuncSymbol(String name, SymbolType symbolType, int lineNum, ArrayList<Symbol> params) {
        super(name, symbolType,lineNum);
        this.params = params;
    }

    public ArrayList<Symbol> getParams() {
        return params;
    }
}
