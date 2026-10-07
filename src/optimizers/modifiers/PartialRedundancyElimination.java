package optimizers.modifiers;

import midEnd.ir.IrModule;
import midEnd.ir.IrValue;
import midEnd.ir.values.IrBasicBlock;
import midEnd.ir.values.IrFunction;
import midEnd.ir.values.IrInstruction;
import midEnd.ir.values.instructions.IrArithmeticInstruction;
import midEnd.ir.values.instructions.IrBranchInstruction;
import midEnd.ir.values.instructions.IrIcmpInstruction;
import midEnd.ir.values.instructions.IrInstructionType;
import midEnd.ir.values.instructions.phi.PhiInstr;
import optimizers.analysers.DominanceAnalyser;

import java.util.*;

public class PartialRedundancyElimination {

    public static void run(IrModule module) {
        // Ensure CFG and dominance info is up to date
        optimizers.analysers.CFGAnalyser.run(module);
        DominanceAnalyser.run(module);
        
        for (IrFunction function : module.getFunctions()) {
            runOnFunction(function);
        }
    }

    private static class Expression {
        String opCode; // For arithmetic: ADD, SUB... For Icmp: EQ, NE...
        IrValue op1;
        IrValue op2;
        int id;

        public Expression(String opCode, IrValue op1, IrValue op2) {
            this.opCode = opCode;
            this.op1 = op1;
            this.op2 = op2;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Expression that = (Expression) o;
            return Objects.equals(opCode, that.opCode) &&
                   op1 == that.op1 && // Pointer equality for SSA values
                   op2 == that.op2;
        }

        @Override
        public int hashCode() {
            return Objects.hash(opCode, System.identityHashCode(op1), System.identityHashCode(op2));
        }
        
        @Override
        public String toString() {
            return opCode + " " + op1.getName() + ", " + op2.getName();
        }
    }

    private static void runOnFunction(IrFunction function) {
        if (function.getBasicBlocks().isEmpty()) return;

        // 1. Collect Expressions
        Map<Expression, Integer> exprToId = new HashMap<>();
        List<Expression> idToExpr = new ArrayList<>();
        Map<IrInstruction, Integer> instrToExprId = new HashMap<>();

        for (IrBasicBlock bb : function.getBasicBlocks()) {
            for (IrInstruction instr : bb.getInstructions()) {
                Expression expr = null;
                if (instr instanceof IrArithmeticInstruction) {
                    IrArithmeticInstruction arith = (IrArithmeticInstruction) instr;
                    // Skip potentially trapping instructions (div, rem) for speculative code motion safety
                    String op = arith.getArithmeticType().name();
                    if (!op.equals("sdiv") && !op.equals("srem")) {
                        expr = new Expression(op, arith.getFirstUseValue(), arith.getSecondUseValue());
                    }
                } else if (instr instanceof IrIcmpInstruction) {
                    IrIcmpInstruction icmp = (IrIcmpInstruction) instr;
                    expr = new Expression(icmp.getIcmpCondType().name(), icmp.getFirstUseValue(), icmp.getSecondUseValue());
                }

                if (expr != null) {
                    if (!exprToId.containsKey(expr)) {
                        expr.id = idToExpr.size();
                        exprToId.put(expr, expr.id);
                        idToExpr.add(expr);
                    }
                    instrToExprId.put(instr, exprToId.get(expr));
                }
            }
        }

        int numExprs = idToExpr.size();
        if (numExprs == 0) return;

        // 2. Dataflow Analysis
        Map<IrBasicBlock, BitSet> anticipatedIn = new HashMap<>();
        Map<IrBasicBlock, BitSet> anticipatedOut = new HashMap<>();
        Map<IrBasicBlock, BitSet> availableIn = new HashMap<>();
        Map<IrBasicBlock, BitSet> availableOut = new HashMap<>();
        Map<IrBasicBlock, BitSet> computed = new HashMap<>();

        // Initialize sets
        for (IrBasicBlock bb : function.getBasicBlocks()) {
            anticipatedIn.put(bb, new BitSet(numExprs));
            anticipatedOut.put(bb, new BitSet(numExprs));
            availableIn.put(bb, new BitSet(numExprs));
            availableOut.put(bb, new BitSet(numExprs));
            
            BitSet comp = new BitSet(numExprs);
            for (IrInstruction instr : bb.getInstructions()) {
                if (instrToExprId.containsKey(instr)) {
                    comp.set(instrToExprId.get(instr));
                }
            }
            computed.put(bb, comp);
        }

        // 2a. Anticipated (Backward)
        
        Map<IrBasicBlock, BitSet> transparent = new HashMap<>();
        for (IrBasicBlock bb : function.getBasicBlocks()) {
            BitSet trans = new BitSet(numExprs);
            trans.set(0, numExprs); // Default true
            
            // Check definitions in B
            for (IrInstruction instr : bb.getInstructions()) {
                // In SSA, instr defines itself
                for (int i = 0; i < numExprs; i++) {
                    Expression e = idToExpr.get(i);
                    if (e.op1 == instr || e.op2 == instr) {
                        trans.clear(i);
                    }
                }
            }
            transparent.put(bb, trans);
        }
        
        // Initial AntIn = Computed (first guess) -> actually init to 1111 for AntOut
        for (IrBasicBlock bb : function.getBasicBlocks()) {
             anticipatedOut.get(bb).set(0, numExprs);

             anticipatedIn.get(bb).set(0, numExprs); 
        }

        boolean changed = true;
        while (changed) {
            changed = false;
            // Reverse Post-Order or simple Reverse iteration
            List<IrBasicBlock> blocks = function.getBasicBlocks();
            for (int i = blocks.size() - 1; i >= 0; i--) {
                IrBasicBlock bb = blocks.get(i);
                
                // Calculate AntOut
                BitSet newAntOut = new BitSet(numExprs);
                if (bb.getSuccessors().isEmpty()) {
                    newAntOut.clear(); // Exit block
                } else {
                    newAntOut.set(0, numExprs);
                    for (IrBasicBlock succ : bb.getSuccessors()) {
                        newAntOut.and(anticipatedIn.get(succ));
                    }
                }
                
                if (!newAntOut.equals(anticipatedOut.get(bb))) {
                    anticipatedOut.put(bb, newAntOut);
                    changed = true;
                }
                
                // Calculate AntIn
                BitSet newAntIn = (BitSet) newAntOut.clone();
                newAntIn.and(transparent.get(bb));
                newAntIn.or(computed.get(bb));
                
                if (!newAntIn.equals(anticipatedIn.get(bb))) {
                    anticipatedIn.put(bb, newAntIn);
                    changed = true;
                }
            }
        }

        // 2b. Available (Forward)
        // Init AvailOut to 1111 (except Entry)
        for (IrBasicBlock bb : function.getBasicBlocks()) {
            availableOut.get(bb).set(0, numExprs);
        }
        availableOut.get(function.getBasicBlocks().get(0)).clear(); // Entry has nothing available
        
        changed = true;
        while (changed) {
            changed = false;
            for (IrBasicBlock bb : function.getBasicBlocks()) {
                BitSet newAvailIn = new BitSet(numExprs);
                if (bb.getPredecessors().isEmpty()) {
                    newAvailIn.clear();
                } else {
                    newAvailIn.set(0, numExprs);
                    for (IrBasicBlock pred : bb.getPredecessors()) {
                        newAvailIn.and(availableOut.get(pred));
                    }
                }
                
                if (!newAvailIn.equals(availableIn.get(bb))) {
                    availableIn.put(bb, newAvailIn);
                    changed = true;
                }
                
                BitSet newAvailOut = (BitSet) newAvailIn.clone();
                newAvailOut.and(transparent.get(bb));
                newAvailOut.or(computed.get(bb));
                
                if (!newAvailOut.equals(availableOut.get(bb))) {
                    availableOut.put(bb, newAvailOut);
                    changed = true;
                }
            }
        }
        
        // 3. Transformation
        
        // Store insertion requests: Edge -> List<ExprID>
        Map<IrBasicBlock, Map<IrBasicBlock, List<Integer>>> insertions = new HashMap<>();
        
        for (IrBasicBlock bb : function.getBasicBlocks()) {
            for (int i = 0; i < numExprs; i++) {
                if (anticipatedIn.get(bb).get(i) && !availableIn.get(bb).get(i)) {
                    // Check predecessors
                    boolean partial = false;
                    for (IrBasicBlock pred : bb.getPredecessors()) {
                        if (availableOut.get(pred).get(i)) {
                            partial = true;
                            break;
                        }
                    }
                    
                    if (partial) {
                        // This is a partial redundancy case.
                        // We want to insert E on edges where it is NOT available.
                        for (IrBasicBlock pred : bb.getPredecessors()) {
                            if (!availableOut.get(pred).get(i)) {
                                // Check if operands are available at Pred (Domination check)
                                Expression e = idToExpr.get(i);
                                if (dominates(e.op1, pred) && dominates(e.op2, pred)) {
                                    insertions.computeIfAbsent(pred, k -> new HashMap<>())
                                              .computeIfAbsent(bb, k -> new ArrayList<>())
                                              .add(i);
                                }
                            }
                        }
                    }
                }
            }
        }
        
        // Perform Insertions
        // Map to track the value of expression E available at end of Block B
        Map<IrBasicBlock, Map<Integer, IrValue>> availValues = new HashMap<>();
        
        // First, populate availValues with existing computations
        for (IrBasicBlock bb : function.getBasicBlocks()) {
            Map<Integer, IrValue> map = new HashMap<>();
            for (IrInstruction instr : bb.getInstructions()) {
                if (instrToExprId.containsKey(instr)) {
                    map.put(instrToExprId.get(instr), instr);
                }
            }
            availValues.put(bb, map);
        }
        
        // Apply insertions
        for (Map.Entry<IrBasicBlock, Map<IrBasicBlock, List<Integer>>> entry : insertions.entrySet()) {
            IrBasicBlock pred = entry.getKey();
            for (Map.Entry<IrBasicBlock, List<Integer>> targetEntry : entry.getValue().entrySet()) {
                IrBasicBlock succ = targetEntry.getKey();
                List<Integer> exprs = targetEntry.getValue();
                
                // Split edge
                IrBasicBlock midBlock = IrBasicBlock.addMiddleBlock(pred, succ);
                
                Map<Integer, IrValue> midMap = new HashMap<>();
                for (int exprId : exprs) {
                    Expression e = idToExpr.get(exprId);
                    IrInstruction newInstr = createInstruction(e);
                    midBlock.addInstrBeforeJump(newInstr);
                    midMap.put(exprId, newInstr);
                }
                availValues.put(midBlock, midMap);
            }
        }
        
        // 4. Elimination & Phi Insertion

        for (IrBasicBlock bb : function.getBasicBlocks()) {
            BitSet ant = anticipatedIn.get(bb);
            if (ant == null) continue; // New block

            Map<Integer, IrValue> currentAvail = new HashMap<>();

            // 4a. Initialize from Predecessors (Global Redundancy & Phi Insertion)
            for (int i = ant.nextSetBit(0); i >= 0; i = ant.nextSetBit(i+1)) {
                // Check if we have values from ALL predecessors
                List<IrValue> incomingValues = new ArrayList<>();
                boolean allAvailable = true;

                if (bb.getPredecessors().isEmpty()) allAvailable = false;

                for (IrBasicBlock pred : bb.getPredecessors()) {
                    IrValue val = findAvailableValue(pred, i, availValues, function);
                    if (val == null) {
                        allAvailable = false;
                        break;
                    }
                    incomingValues.add(val);
                }

                if (allAvailable) {
                    IrValue mergedVal = incomingValues.get(0);
                    boolean needPhi = false;
                    for (IrValue v : incomingValues) {
                        if (v != mergedVal) {
                            needPhi = true;
                            break;
                        }
                    }

                    if (needPhi) {
                        PhiInstr phi = new PhiInstr(bb);
                        for (int k = 0; k < bb.getPredecessors().size(); k++) {
                            phi.convertBlockToValue(incomingValues.get(k), bb.getPredecessors().get(k));
                        }
                        bb.getInstructions().add(0, phi);
                        mergedVal = phi;
                    }
                    currentAvail.put(i, mergedVal);
                }
            }

            // 4b. Local Elimination (Scan)
            List<IrInstruction> toRemove = new ArrayList<>();
            for (IrInstruction instr : bb.getInstructions()) {
                if (instrToExprId.containsKey(instr)) {
                    int id = instrToExprId.get(instr);
                    if (currentAvail.containsKey(id)) {
                        // Redundant!
                        IrValue val = currentAvail.get(id);
                        instr.modifyAllUsersToNewValue(val);
                        toRemove.add(instr);
                    } else {
                        // First computation in block (and not available from global)
                        currentAvail.put(id, instr);
                    }
                }
            }
            bb.getInstructions().removeAll(toRemove);
            
            // 4c. Update global map
            availValues.put(bb, currentAvail);
        }
    }
    
    // Find available value in a block
    private static IrValue findAvailableValue(IrBasicBlock block, int exprId, 
                                              Map<IrBasicBlock, Map<Integer, IrValue>> availValues,
                                              IrFunction func) {
        IrBasicBlock curr = block;
        while (curr != null) {
            if (availValues.containsKey(curr) && availValues.get(curr).containsKey(exprId)) {
                return availValues.get(curr).get(exprId);
            }
            curr = curr.getIdom();
        }
        return null;
    }

    private static boolean dominates(IrValue val, IrBasicBlock block) {
        if (val instanceof IrInstruction) {
            IrBasicBlock defBlock = ((IrInstruction) val).getParent();
            if (defBlock == block) {
                return true;
            }
            return block.getDominators().contains(defBlock);
        }
        // Arguments/Globals/Constants always dominate
        return true;
    }

    private static IrInstruction createInstruction(Expression e) {
        if (e.opCode.equals("add") || e.opCode.equals("sub") || e.opCode.equals("mul") || 
            e.opCode.equals("sdiv") || e.opCode.equals("srem")) {
            return new IrArithmeticInstruction(IrArithmeticInstruction.IrArithmeticType.valueOf(e.opCode), e.op1, e.op2, false);
        } else {
             // ICMP
             return new IrIcmpInstruction(IrIcmpInstruction.IcmpCondType.valueOf(e.opCode), e.op1, e.op2, false);
        }
    }
}
