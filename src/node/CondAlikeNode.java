package node;

import midEnd.ir.values.IrBasicBlock;
import midEnd.ir.values.instructions.IrBranchInstruction;

import java.util.ArrayList;

public abstract class CondAlikeNode extends ExpAlikeNode {
    protected ArrayList<IrBranchInstruction> trueList = new ArrayList<>();
    protected ArrayList<IrBranchInstruction> falseList = new ArrayList<>();

    public CondAlikeNode(NodeType type) {
        super(type);
    }

    public ArrayList<IrBranchInstruction> getTrueList() {
        return trueList;
    }

    public void addToTrueList(IrBranchInstruction instruction) {
        this.trueList.add(instruction);
    }

    public ArrayList<IrBranchInstruction> getFalseList() {
        return falseList;
    }

    public void addToFalseList(IrBranchInstruction instruction) {
        this.falseList.add(instruction);
    }
}
