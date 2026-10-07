package optimizers.modifiers;

import midEnd.ir.IrModule;
import midEnd.ir.IrValue;
import midEnd.ir.values.IrBasicBlock;
import midEnd.ir.values.IrConstant;
import midEnd.ir.values.IrFunction;
import midEnd.ir.values.IrInstruction;
import midEnd.ir.values.instructions.*;
import midEnd.ir.values.instructions.IOInstructions.IrGetintInstruction;
import midEnd.ir.values.instructions.phi.PhiInstr;

import java.util.*;

public class SCCP {

    private enum LatticeType {
        TOP, CONSTANT, BOTTOM
    }

    private static class LatticeCell {
        LatticeType type;
        int value;

        LatticeCell(LatticeType type, int value) {
            this.type = type;
            this.value = value;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            LatticeCell that = (LatticeCell) o;
            return value == that.value && type == that.type;
        }

        @Override
        public int hashCode() {
            return Objects.hash(type, value);
        }
        
        @Override
        public String toString() {
            return type + (type == LatticeType.CONSTANT ? "(" + value + ")" : "");
        }
    }

    private static class Edge {
        IrBasicBlock src;
        IrBasicBlock dst;

        Edge(IrBasicBlock src, IrBasicBlock dst) {
            this.src = src;
            this.dst = dst;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Edge edge = (Edge) o;
            return Objects.equals(src, edge.src) && Objects.equals(dst, edge.dst);
        }

        @Override
        public int hashCode() {
            return Objects.hash(src, dst);
        }
    }

    private static Map<IrValue, LatticeCell> latticeValues;
    private static Set<Edge> executableEdges;
    private static Set<IrBasicBlock> visitedBlocks;
    private static Stack<Edge> flowWorkList;
    private static Stack<IrInstruction> ssaWorkList;

    public static void run(IrModule module) {
        latticeValues = new HashMap<>();
        executableEdges = new HashSet<>();
        visitedBlocks = new HashSet<>();
        flowWorkList = new Stack<>();
        ssaWorkList = new Stack<>();

        for (IrFunction func : module.getFunctions()) {
            runOnFunction(func);
        }
    }

    private static void runOnFunction(IrFunction func) {
        latticeValues.clear();
        executableEdges.clear();
        visitedBlocks.clear();
        flowWorkList.clear();
        ssaWorkList.clear();

        // 1. Initialize Lattice Values
        for (IrBasicBlock bb : func.getBasicBlocks()) {
            for (IrInstruction instr : bb.getInstructions()) {
                latticeValues.put(instr, new LatticeCell(LatticeType.TOP, 0));
            }
        }
        for (IrValue param : func.getParameters()) {
            latticeValues.put(param, new LatticeCell(LatticeType.BOTTOM, 0));
        }

        // 2. Add entry edges
        if (!func.getBasicBlocks().isEmpty()) {
            IrBasicBlock entry = func.getBasicBlocks().get(0);
            flowWorkList.push(new Edge(null, entry));
        }

        // 3. Process Worklists
        while (!flowWorkList.isEmpty() || !ssaWorkList.isEmpty()) {
            if (!flowWorkList.isEmpty()) {
                Edge edge = flowWorkList.pop();
                IrBasicBlock dst = edge.dst;
                
                // If edge is already executable, skip (unless it's the special null entry edge which we process once)
                boolean isNewEdge = (edge.src == null) || !executableEdges.contains(edge);
                
                if (isNewEdge) {
                    if (edge.src != null) executableEdges.add(edge);
                    
                    // Evaluate Phis in dst
                    for (IrInstruction instr : dst.getInstructions()) {
                        if (instr instanceof PhiInstr) {
                            visitPhi((PhiInstr) instr);
                        }
                    }

                    // If first time visiting block, evaluate all non-phi instructions
                    if (!visitedBlocks.contains(dst)) {
                        visitedBlocks.add(dst);
                        for (IrInstruction instr : dst.getInstructions()) {
                            if (!(instr instanceof PhiInstr)) {
                                visitInstruction(instr);
                            }
                        }
                    } 
                }
            }

            if (!ssaWorkList.isEmpty()) {
                IrInstruction instr = ssaWorkList.pop();
                if (instr instanceof PhiInstr) {
                    visitPhi((PhiInstr) instr);
                } else {
                    visitInstruction(instr);
                }
            }
        }

        // 4. Rewrite
        rewriteFunction(func);
    }

    private static void visitPhi(PhiInstr phi) {
        LatticeCell result = new LatticeCell(LatticeType.TOP, 0);
        IrBasicBlock parent = phi.getParent();
        ArrayList<IrBasicBlock> preds = phi.getBeforeBlockList();
        ArrayList<IrValue> values = phi.getUseValueList();

        for (int i = 0; i < preds.size(); i++) {
            IrBasicBlock pred = preds.get(i);
            Edge edge = new Edge(pred, parent);
            
            if (executableEdges.contains(edge)) {
                IrValue val = values.get(i);
                LatticeCell valLattice = getLatticeValue(val);
                result = meet(result, valLattice);
            }
        }
        
        updateLatticeValue(phi, result);
    }

    private static void visitInstruction(IrInstruction instr) {
        LatticeCell result = evaluate(instr);
        updateLatticeValue(instr, result);

        if (instr instanceof IrBranchInstruction) {
            handleBranch((IrBranchInstruction) instr);
        }
    }

    private static void handleBranch(IrBranchInstruction branch) {
        IrBasicBlock currBlock = branch.getParent();
        
        // Unconditional branch
        if (branch.getObjectiveUseValue() == null) {
            IrBasicBlock dest = (IrBasicBlock) (branch.getFirstUseValue() != null ? 
                                branch.getFirstUseValue() : branch.getSecondUseValue());
            flowWorkList.push(new Edge(currBlock, dest));
            return;
        }

        // Conditional branch
        LatticeCell condLattice = getLatticeValue(branch.getObjectiveUseValue());
        IrBasicBlock trueDest = (IrBasicBlock) branch.getFirstUseValue();
        IrBasicBlock falseDest = (IrBasicBlock) branch.getSecondUseValue();

        if (condLattice.type == LatticeType.BOTTOM) {
            flowWorkList.push(new Edge(currBlock, trueDest));
            flowWorkList.push(new Edge(currBlock, falseDest));
        } else if (condLattice.type == LatticeType.CONSTANT) {
            if (condLattice.value != 0) {
                flowWorkList.push(new Edge(currBlock, trueDest));
            } else {
                flowWorkList.push(new Edge(currBlock, falseDest));
            }
        }
        // If TOP, do nothing
    }

    private static LatticeCell evaluate(IrInstruction instr) {
        if (instr instanceof IrArithmeticInstruction) {
            return evaluateArithmetic((IrArithmeticInstruction) instr);
        }
        
        if (instr instanceof IrIcmpInstruction) {
            return evaluateIcmp((IrIcmpInstruction) instr);
        }
        
        // Instructions that cannot be folded
        if (instr instanceof IrCallInstruction || 
            instr instanceof IrLoadInstruction || 
            instr instanceof IrAllocaInstruction ||
            instr instanceof IrGetintInstruction) {
            return new LatticeCell(LatticeType.BOTTOM, 0);
        }
        
        // Store, Branch, Return have no value
        return new LatticeCell(LatticeType.BOTTOM, 0);
    }

    private static LatticeCell evaluateArithmetic(IrArithmeticInstruction instr) {
        LatticeCell v1 = getLatticeValue(instr.getFirstUseValue());
        LatticeCell v2 = getLatticeValue(instr.getSecondUseValue());

        if (v1.type == LatticeType.BOTTOM || v2.type == LatticeType.BOTTOM) {
            return new LatticeCell(LatticeType.BOTTOM, 0);
        }
        if (v1.type == LatticeType.TOP || v2.type == LatticeType.TOP) {
            return new LatticeCell(LatticeType.TOP, 0);
        }

        // Both CONSTANT
        int res = 0;
        switch (instr.getArithmeticType()) {
            case add: res = v1.value + v2.value; break;
            case sub: res = v1.value - v2.value; break;
            case mul: res = v1.value * v2.value; break;
            case sdiv: 
                if (v2.value == 0) return new LatticeCell(LatticeType.BOTTOM, 0); // Div by zero
                res = v1.value / v2.value; 
                break;
            case srem: 
                if (v2.value == 0) return new LatticeCell(LatticeType.BOTTOM, 0);
                res = v1.value % v2.value; 
                break;
            default: return new LatticeCell(LatticeType.BOTTOM, 0);
        }
        return new LatticeCell(LatticeType.CONSTANT, res);
    }

    private static LatticeCell evaluateIcmp(IrIcmpInstruction instr) {
        LatticeCell v1 = getLatticeValue(instr.getFirstUseValue());
        LatticeCell v2 = getLatticeValue(instr.getSecondUseValue());

        if (v1.type == LatticeType.BOTTOM || v2.type == LatticeType.BOTTOM) {
            return new LatticeCell(LatticeType.BOTTOM, 0);
        }
        if (v1.type == LatticeType.TOP || v2.type == LatticeType.TOP) {
            return new LatticeCell(LatticeType.TOP, 0);
        }

        boolean res = false;
        switch (instr.getIcmpCondType()) {
            case eq: res = v1.value == v2.value; break;
            case ne: res = v1.value != v2.value; break;
            case sgt: res = v1.value > v2.value; break;
            case sge: res = v1.value >= v2.value; break;
            case slt: res = v1.value < v2.value; break;
            case sle: res = v1.value <= v2.value; break;
        }
        return new LatticeCell(LatticeType.CONSTANT, res ? 1 : 0);
    }

    private static LatticeCell getLatticeValue(IrValue value) {
        if (value instanceof IrConstant) {
            return new LatticeCell(LatticeType.CONSTANT, ((IrConstant) value).getConstValue());
        }
        if (latticeValues.containsKey(value)) {
            return latticeValues.get(value);
        }
        // Default for unknown values (e.g. globals)
        return new LatticeCell(LatticeType.BOTTOM, 0);
    }

    private static LatticeCell meet(LatticeCell l1, LatticeCell l2) {
        if (l1.type == LatticeType.TOP) return l2;
        if (l2.type == LatticeType.TOP) return l1;
        if (l1.type == LatticeType.BOTTOM || l2.type == LatticeType.BOTTOM) {
            return new LatticeCell(LatticeType.BOTTOM, 0);
        }
        if (l1.type == LatticeType.CONSTANT && l2.type == LatticeType.CONSTANT) {
            if (l1.value == l2.value) return l1;
        }
        return new LatticeCell(LatticeType.BOTTOM, 0);
    }

    private static void updateLatticeValue(IrInstruction instr, LatticeCell newVal) {
        LatticeCell oldVal = latticeValues.get(instr);
        if (oldVal == null) oldVal = new LatticeCell(LatticeType.TOP, 0);

        if (!oldVal.equals(newVal)) {
            latticeValues.put(instr, newVal);
            // Add users to SSA worklist
            for (midEnd.ir.values.IrUser user : instr.getUserList()) {
                if (user instanceof IrInstruction) {
                    ssaWorkList.push((IrInstruction) user);
                }
            }
        }
    }

    private static void rewriteFunction(IrFunction func) {
        // 1. Replace CONSTANT values
        for (IrBasicBlock bb : func.getBasicBlocks()) {
            // Use iterator to safely modify list if needed (though we only replace uses here)
            for (IrInstruction instr : bb.getInstructions()) {
                if (latticeValues.containsKey(instr)) {
                    LatticeCell lattice = latticeValues.get(instr);
                    if (lattice.type == LatticeType.CONSTANT) {
                        IrConstant constant = new IrConstant(lattice.value);
                        instr.modifyAllUsersToNewValue(constant);
                    }
                }
            }
        }

        // 2. Simplify Branches
        for (IrBasicBlock bb : func.getBasicBlocks()) {
            if (!bb.getInstructions().isEmpty()) {
                IrInstruction last = bb.getInstructions().get(bb.getInstructions().size() - 1);
                if (last instanceof IrBranchInstruction) {
                    IrBranchInstruction branch = (IrBranchInstruction) last;
                    if (branch.getObjectiveUseValue() != null) { // Conditional
                        LatticeCell condLattice = getLatticeValue(branch.getObjectiveUseValue());
                        if (condLattice.type == LatticeType.CONSTANT) {
                            IrBasicBlock dest;
                            if (condLattice.value != 0) {
                                dest = (IrBasicBlock) branch.getFirstUseValue();
                            } else {
                                dest = (IrBasicBlock) branch.getSecondUseValue();
                            }
                            // Replace with unconditional branch
                            IrBranchInstruction newBranch = new IrBranchInstruction(null, false);
                            newBranch.setTrueDestination(dest);
                            newBranch.setParent(bb);
                            
                            // Replace in instruction list
                            branch.removeAllValueUse();
                            bb.getInstructions().set(bb.getInstructions().size() - 1, newBranch);
                        }
                    }
                }
            }
        }
        
        // 3. Remove unreachable blocks
        Iterator<IrBasicBlock> it = func.getBasicBlocks().iterator();
        while (it.hasNext()) {
            IrBasicBlock bb = it.next();
            if (!visitedBlocks.contains(bb)) {
                // Remove all instructions uses
                for (IrInstruction instr : bb.getInstructions()) {
                    instr.removeAllValueUse();
                }
                it.remove();
            }
        }
    }
}
