package optimizers.allocators;

import backend.mips.Register;
import midEnd.ir.IrModule;
import midEnd.ir.IrValue;
import midEnd.ir.values.IrBasicBlock;
import midEnd.ir.values.IrFunction;
import midEnd.ir.values.IrGlobalValue;
import midEnd.ir.values.IrInstruction;
import midEnd.ir.values.IrVariable;
import optimizers.analysers.LivenessAnalyser;

import java.util.*;

public class GraphColoringAllocator {

    private GraphColoringAllocator() {}

    public static void run(IrModule irModule) {
        // Ensure liveness analysis is up to date
        LivenessAnalyser.run(irModule);

        ArrayList<Register> usableRegisters = new ArrayList<>();
        for (Register reg : Register.GetUsAbleRegisters()) {
            if (reg != Register.T8 && reg != Register.T9) {
                usableRegisters.add(reg);
            }
        }

        for (IrGlobalValue globalValue : irModule.getGlobalValues()) {
            if (globalValue instanceof IrFunction) {
                allocateForFunction((IrFunction) globalValue, usableRegisters);
            }
        }
    }

    private static void allocateForFunction(IrFunction function, ArrayList<Register> usableRegisters) {
        // Clear previous allocation to ensure spilled variables don't keep old registers
        function.getValueRegisterMap().clear();

        // 1. Build Interference Graph
        Map<IrValue, Set<IrValue>> adjList = new HashMap<>();
        Map<IrValue, Integer> degree = new HashMap<>();
        Set<IrValue> nodes = new HashSet<>();

        // Collect all definitions as nodes
        for (IrBasicBlock block : function.getBasicBlocks()) {
            for (IrInstruction instr : block.getInstructions()) {
                if (!LivenessAnalyser.isNonDefInstr(instr) && !(instr instanceof midEnd.ir.values.instructions.IrAllocaInstruction)) {
                    nodes.add(instr);
                    adjList.putIfAbsent(instr, new HashSet<>());
                    degree.putIfAbsent(instr, 0);
                }
            }
        }

        /*
        for (IrVariable param : function.getParameters()) {
            nodes.add(param);
            adjList.putIfAbsent(param, new HashSet<>());
            degree.putIfAbsent(param, 0);
        }
        */

        // Add edges
        for (IrBasicBlock block : function.getBasicBlocks()) {
            for (IrInstruction instr : block.getInstructions()) {
                if (!LivenessAnalyser.isNonDefInstr(instr)) {
                    // Definition point
                    IrValue def = instr;
                    
                    // Collect both liveOut and liveIn
                    Set<IrValue> liveVars = new HashSet<>();
                    liveVars.addAll(LivenessAnalyser.getLiveOut(instr));
                    liveVars.addAll(LivenessAnalyser.getLiveIn(instr));

                    for (IrValue live : liveVars) {
                        if (nodes.contains(def) && nodes.contains(live) && !live.equals(def)) {
                            addEdge(adjList, degree, def, live);
                        }
                    }
                }
            }
        }

        // 2. Simplify
        Stack<IrValue> stack = new Stack<>();
        Set<IrValue> removed = new HashSet<>();
  
        Map<IrValue, Integer> currentDegree = new HashMap<>(degree);
        
        int k = usableRegisters.size();
        
        boolean progress = true;
        while (progress) {
            progress = false;

            IrValue nodeToRemove = null;
            
            // Try to find a node < K
            for (IrValue node : nodes) {
                if (!removed.contains(node)) {
                    if (currentDegree.get(node) < k) {
                        nodeToRemove = node;
                        break;
                    }
                }
            }
            
            // If not found
            if (nodeToRemove == null) {
                for (IrValue node : nodes) {
                    if (!removed.contains(node)) {
                        nodeToRemove = node;
                        break;
                    }
                }
            }
            
            if (nodeToRemove != null) {
                removed.add(nodeToRemove);
                stack.push(nodeToRemove);
                progress = true;
                
                // Decrement neighbors
                for (IrValue neighbor : adjList.get(nodeToRemove)) {
                    if (!removed.contains(neighbor)) {
                        currentDegree.put(neighbor, currentDegree.get(neighbor) - 1);
                    }
                }
            }
        }
        
        // 3. Coloring
        Map<IrValue, Register> coloring = new HashMap<>();
        
        while (!stack.isEmpty()) {
            IrValue node = stack.pop();
            Set<Register> usedColors = new HashSet<>();
            
            for (IrValue neighbor : adjList.get(node)) {
                if (coloring.containsKey(neighbor)) {
                    usedColors.add(coloring.get(neighbor));
                }
            }
            
            Register assignedColor = null;
            for (Register reg : usableRegisters) {
                if (!usedColors.contains(reg)) {
                    assignedColor = reg;
                    break;
                }
            }
            
            if (assignedColor != null) {
                coloring.put(node, assignedColor);
            } else {
                // Spill
                // System.err.println("Warning: Spilled variable " + node.getName());
            }
        }
        
        // 4. Update IrFunction
        for (Map.Entry<IrValue, Register> entry : coloring.entrySet()) {
            function.getValueRegisterMap().put(entry.getKey(), entry.getValue());
        }
    }

    private static void addEdge(Map<IrValue, Set<IrValue>> adj, Map<IrValue, Integer> degree, IrValue u, IrValue v) {
        if (adj.get(u).add(v)) {
            degree.put(u, degree.get(u) + 1);
            
            adj.putIfAbsent(v, new HashSet<>());
            adj.get(v).add(u);
            degree.put(v, degree.getOrDefault(v, 0) + 1);
        }
    }
}
