package midEnd.symbol;

import midEnd.ir.IrValue;

public class Symbol {
    public enum SymbolType {
        ConstInt,       Int, 	        VoidFunc,
        ConstIntArray,  IntArray,       IntFunc,
        StaticInt,      StaticIntArray
    }
    private final String name;
    private final SymbolType symbolType;
    private final int lineNum;
    private int shadowingNum;


    private IrValue irValue;

    public Symbol(String name, SymbolType symbolType, int lineNum) {
        this.name = name;
        this.symbolType = symbolType;
        this.lineNum = lineNum;
        this.shadowingNum = 0;
    }

    public int getShadowingNum() {
        return shadowingNum;
    }

    public void setShadowingNum(int shadowingNum) {
        this.shadowingNum = shadowingNum;
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

    public void setIrValue(IrValue irValue) {
        this.irValue = irValue;
    }

    public IrValue getIrValue() {
        return irValue;
    }

}
