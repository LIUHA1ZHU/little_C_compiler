package midEnd.ir.values;

import backend.mips.assembly.MipsLabel;
import midEnd.ir.IrBuilder;
import midEnd.ir.IrValue;
import midEnd.ir.IrValueType;
import midEnd.ir.values.instructions.IrInstructionType;

import java.util.ArrayList;

public class IrBasicBlock extends IrValue {
    private final ArrayList<IrInstruction> instructions;
    private IrFunction parent;

    // for CFG analysis
    private final ArrayList<IrBasicBlock> predecessors;
    private final ArrayList<IrBasicBlock> successors;

    // for Dominance analysis
    private final ArrayList<IrBasicBlock> dominators;
    private IrBasicBlock idom;
    private final ArrayList<IrBasicBlock> dominanceFrontier;
    
    public void setParent(IrFunction parent) {
        this.parent = parent;
    }

    public IrFunction getParent() {
        return parent;
    }

    private final ArrayList<IrBasicBlock> domTreeChildren;


    public IrBasicBlock(String name) {
        super(name, IrValueType.BasicBlock);
        this.instructions = new ArrayList<>();
        this.predecessors = new ArrayList<>();
        this.successors = new ArrayList<>();
        this.dominators = new ArrayList<>();
        this.dominanceFrontier = new ArrayList<>();
        this.domTreeChildren = new ArrayList<>();
        this.parent = IrBuilder.getCurFunction();
    }

    public void addInstr(IrInstruction instruction) {
        instructions.add(instruction);
    }

    public void addInstrBeforeJump(IrInstruction instruction) {
        if (instructions.isEmpty()) {
            addInstr(instruction);
            return;
        }
        IrInstruction lastInstr = instructions.get(instructions.size() - 1);
        if (lastInstr.getInstructionType() == IrInstructionType.BranchInstr ||
            lastInstr.getInstructionType() == IrInstructionType.ReturnIntInstr ||
            lastInstr.getInstructionType() == IrInstructionType.ReturnVoidInstr) {
            instructions.add(instructions.size() - 1, instruction);
        } else {
            addInstr(instruction);
        }
    }

    public static IrBasicBlock addMiddleBlock(IrBasicBlock beforeBlock, IrBasicBlock nextBlock) {
        IrBasicBlock middleBlock = new IrBasicBlock(beforeBlock.getName() + "_mid_" + nextBlock.getName());
        beforeBlock.getParent().addBasicBlock(middleBlock);
        middleBlock.setParent(beforeBlock.getParent());
        
        // Update CFG
        // Remove direct connection
        beforeBlock.getSuccessors().remove(nextBlock);
        nextBlock.getPredecessors().remove(beforeBlock);
        
        // Add connections to middle block
        beforeBlock.addSuc(middleBlock);
        middleBlock.addPre(beforeBlock);
        
        middleBlock.addSuc(nextBlock);
        nextBlock.addPre(middleBlock);
        
        // Update Branch instruction in beforeBlock
        IrInstruction lastInstr = beforeBlock.getInstructions().get(beforeBlock.getInstructions().size() - 1);
        if (lastInstr instanceof midEnd.ir.values.instructions.IrBranchInstruction branchInstr) {
             if (branchInstr.getFirstUseValue() == nextBlock) {
                 branchInstr.setTrueDestination(middleBlock);
             } else if (branchInstr.getSecondUseValue() == nextBlock) {
                 branchInstr.setFalseDestination(middleBlock);
             }
        }
        
        // Add jump in middle block to nextBlock
         midEnd.ir.values.instructions.IrBranchInstruction jump = new midEnd.ir.values.instructions.IrBranchInstruction(null, false);
         jump.setTrueDestination(nextBlock);
         jump.setParent(middleBlock);
         middleBlock.addInstr(jump);
         
        
         
         return middleBlock;
     }

    public void addInstrToHead(IrInstruction instruction) {
        instructions.add(0, instruction);
    }

    public ArrayList<IrInstruction> getInstructions() {
        return instructions;
    }

    public boolean notEndWithTerminator() {
        if (instructions.isEmpty()) return true;
        IrInstructionType lastInstrType = instructions.get(instructions.size() - 1).getInstructionType();
        return !(lastInstrType.equals(IrInstructionType.ReturnVoidInstr) || lastInstrType.equals(IrInstructionType.ReturnIntInstr)
                || lastInstrType.equals(IrInstructionType.BranchInstr));
    }

    public void addPre(IrBasicBlock basicBlock) {
        predecessors.add(basicBlock);
    }

    public void addSuc(IrBasicBlock basicBlock) {
        successors.add(basicBlock);
    }

    public ArrayList<IrBasicBlock> getPredecessors() {
        return predecessors;
    }

    public ArrayList<IrBasicBlock> getSuccessors() {
        return successors;
    }

    public ArrayList<IrBasicBlock> getDominators() {
        return dominators;
    }

    public IrBasicBlock getIdom() {
        return idom;
    }

    public void setIdom(IrBasicBlock idom) {
        this.idom = idom;
    }

    public ArrayList<IrBasicBlock> getDominanceFrontier() {
        return dominanceFrontier;
    }

    public ArrayList<IrBasicBlock> getDirectDominateBlocks() {
        return domTreeChildren;
    }

    public void addDomTreeChild(IrBasicBlock child) {
        domTreeChildren.add(child);
    }

    public void clearDomTreeChildren() {
        domTreeChildren.clear();
    }

    public String getMipsLabel() {
        return parent.getMipsName() + "." + getMipsName();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(name).append(":\n");
        for (IrInstruction instruction : instructions) {
            sb.append("\t").append(instruction);
        }
        return sb.append("\n").toString();
    }

    @Override
    public void toMips() {
        new MipsLabel(getMipsLabel());
        for (IrInstruction instr : this.instructions) {
            instr.toMips();
        }
    }
}
