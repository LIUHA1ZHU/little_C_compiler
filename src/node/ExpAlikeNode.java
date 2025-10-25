package node;

import midEnd.symbol.Symbol;

/**
 * for evaluating exp
 */
public abstract class ExpAlikeNode extends Node{
    protected boolean isConst = false;
    protected int constValue = 0;

    public ExpAlikeNode(NodeType type) {
        super(type);
    }

    public boolean isConst() {
        return isConst;
    }

    public int getConstValue() {
        return constValue;
    }

    // call son Node's evaluate and evaluate
    public abstract void evaluate();
}
