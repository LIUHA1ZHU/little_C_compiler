package optimizers.analysers;

import midEnd.ir.IrModule;
import midEnd.ir.values.IrBasicBlock;
import midEnd.ir.values.IrFunction;
import midEnd.ir.values.IrGlobalValue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class DominanceAnalyser {

    public static void run(IrModule module) {
        for (IrGlobalValue globalValue : module.getGlobalValues()) {
            if (globalValue instanceof IrFunction function) {
                runFunc(function);
            }
        }
    }

    public static void runFunc(IrFunction function) {
        ArrayList<IrBasicBlock> allBasicBlocks = function.getBasicBlocks();
        if (allBasicBlocks.isEmpty()) {
            return;
        }

        IrBasicBlock entry = allBasicBlocks.get(0);
        
        // Filter unreachable blocks
        HashSet<IrBasicBlock> reachable = new HashSet<>();
        ArrayList<IrBasicBlock> queue = new ArrayList<>();
        queue.add(entry);
        reachable.add(entry);
        int head = 0;
        while(head < queue.size()){
            IrBasicBlock curr = queue.get(head++);
            for(IrBasicBlock succ : curr.getSuccessors()){
                if(!reachable.contains(succ)){
                    reachable.add(succ);
                    queue.add(succ);
                }
            }
        }
        
        // Use only reachable blocks for analysis
        ArrayList<IrBasicBlock> blocks = new ArrayList<>(reachable);

        // Map to store current dominator sets
        HashMap<IrBasicBlock, HashSet<IrBasicBlock>> domSets = new HashMap<>();
        HashSet<IrBasicBlock> allBlocks = new HashSet<>(blocks);

        // 1. Initialization
        for (IrBasicBlock b : blocks) {
            if (b == entry) {
                HashSet<IrBasicBlock> s = new HashSet<>();
                s.add(entry);
                domSets.put(b, s);
            } else {
                domSets.put(b, new HashSet<>(allBlocks));
            }
        }

        // 2. Iterative update
        boolean changed = true;
        while (changed) {
            changed = false;
            for (IrBasicBlock b : blocks) {
                if (b == entry) {
                    continue;
                }

                // Calculate intersection of predecessors' dom sets
                ArrayList<IrBasicBlock> preds = b.getPredecessors();
                
                HashSet<IrBasicBlock> newDom = null;

                for (IrBasicBlock p : preds) {
                    if (reachable.contains(p) && domSets.containsKey(p)) {
                        if (newDom == null) {
                            newDom = new HashSet<>(domSets.get(p));
                        } else {
                            newDom.retainAll(domSets.get(p));
                        }
                    }
                }
                
                if (newDom == null) {
                    newDom = new HashSet<>(allBlocks); 
                }

                newDom.add(b);

                if (!newDom.equals(domSets.get(b))) {
                    domSets.put(b, newDom);
                    changed = true;
                }
            }
        }

        // 3. Write back to IrBasicBlock
        for (IrBasicBlock b : blocks) {
            b.getDominators().clear();
            b.getDominators().addAll(domSets.get(b));
        }

        // 4. Calculate Immediate Dominator (IDOM)
        for (IrBasicBlock b : blocks) {
            if (b == entry) {
                b.setIdom(null);
                continue;
            }

            IrBasicBlock idom = null;
            int maxDomSize = -1;

            for (IrBasicBlock dom : b.getDominators()) {
                if (dom != b) {
                    // It is a strict dominator
                    // We want the one that is "closest", i.e. has the largest dominator set
                    int size = dom.getDominators().size();
                    if (size > maxDomSize) {
                        maxDomSize = size;
                        idom = dom;
                    }
                }
            }
            b.setIdom(idom);
        }

        // 4.5. Populate Dominator Tree Children
        for (IrBasicBlock b : blocks) {
            b.clearDomTreeChildren();
        }
        for (IrBasicBlock b : blocks) {
            if (b.getIdom() != null) {
                b.getIdom().addDomTreeChild(b);
            }
        }

        // 5. Calculate Dominance Frontier
        computeDominanceFrontier(blocks);
    }

    private static void computeDominanceFrontier(ArrayList<IrBasicBlock> blocks) {
        for (IrBasicBlock b : blocks) {
            b.getDominanceFrontier().clear();
        }

        for (IrBasicBlock a : blocks) {
            for (IrBasicBlock b : a.getSuccessors()) {
                IrBasicBlock x = a;
                while (x != null && !isStrictlyDominates(x, b)) {
                    if (!x.getDominanceFrontier().contains(b)) {
                        x.getDominanceFrontier().add(b);
                    }
                    x = x.getIdom();
                }
            }
        }
    }

    /**
     * Query if dominator strictly dominates block
     */
    public static boolean isStrictlyDominates(IrBasicBlock dominator, IrBasicBlock block) {
        if (dominator == block) {
            return false;
        }
        return block.getDominators().contains(dominator);
    }

    public static IrBasicBlock getImmediateDominator(IrBasicBlock block) {
        return block.getIdom();
    }

    public static ArrayList<IrBasicBlock> getDominanceFrontier(IrBasicBlock block) {
        return block.getDominanceFrontier();
    }
}
