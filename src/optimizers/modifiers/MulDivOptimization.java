package optimizers.modifiers;

import midEnd.ir.IrModule;
import midEnd.ir.IrValue;
import midEnd.ir.values.IrBasicBlock;
import midEnd.ir.values.IrConstant;
import midEnd.ir.values.IrFunction;
import midEnd.ir.values.IrInstruction;
import midEnd.ir.values.instructions.IrArithmeticInstruction;
import midEnd.ir.values.instructions.IrInstructionType;

import java.util.ArrayList;
import java.util.List;

public class MulDivOptimization {

    public static void run(IrModule module) {
        for (IrFunction function : module.getFunctions()) {
            runOnFunction(function);
        }
    }

    private static void runOnFunction(IrFunction function) {
        for (IrBasicBlock bb : function.getBasicBlocks()) {
            runOnBlock(bb);
        }
    }

    private static void runOnBlock(IrBasicBlock bb) {
        List<IrInstruction> instructions = new ArrayList<>(bb.getInstructions());
        for (IrInstruction instr : instructions) {
            if (instr instanceof IrArithmeticInstruction) {
                optimizeInstruction((IrArithmeticInstruction) instr, bb);
            }
        }
    }

    private static void optimizeInstruction(IrArithmeticInstruction instr, IrBasicBlock bb) {
        IrValue op1 = instr.getFirstUseValue();
        IrValue op2 = instr.getSecondUseValue();
        IrArithmeticInstruction.IrArithmeticType type = instr.getArithmeticType();

        // Check for constant operand
        // Mul is commutative, so check both. Div requires op2 to be constant.
        
        IrConstant c = null;
        IrValue x = null;
        boolean isCommuted = false;

        if (type == IrArithmeticInstruction.IrArithmeticType.mul) {
            if (op2 instanceof IrConstant) {
                c = (IrConstant) op2;
                x = op1;
            } else if (op1 instanceof IrConstant) {
                c = (IrConstant) op1;
                x = op2;
                isCommuted = true;
            }
        } else if (type == IrArithmeticInstruction.IrArithmeticType.sdiv) {
            if (op2 instanceof IrConstant) {
                c = (IrConstant) op2;
                x = op1;
            }
        }

        if (c == null) return;

        int constVal = c.getConstValue();
        
        // 1. Special values
        if (type == IrArithmeticInstruction.IrArithmeticType.mul) {
            if (constVal == 0) {
                // Replace with 0
                replaceWithConstant(instr, 0);
                return;
            }
            if (constVal == 1) {
                // Replace with x
                instr.modifyAllUsersToNewValue(x);
                instr.removeAllValueUse();
                bb.getInstructions().remove(instr);
                return;
            }
            if (constVal == -1) {
                // Replace with -x (sub 0, x)
                createNeg(instr, x);
                return;
            }
        } else if (type == IrArithmeticInstruction.IrArithmeticType.sdiv) {
            if (constVal == 1) {
                // Replace with x
                instr.modifyAllUsersToNewValue(x);
                instr.removeAllValueUse();
                bb.getInstructions().remove(instr);
                return;
            }
            if (constVal == -1) {
                // Replace with -x (sub 0, x)
                createNeg(instr, x);
                return;
            }
            if (constVal == 0) {
                // Ignore
                return;
            }
        }

        // 2. Power of 2
        if (isPowerOfTwo(constVal)) {
            int shift = Integer.numberOfTrailingZeros(constVal);
            if (type == IrArithmeticInstruction.IrArithmeticType.mul) {
                // x << shift
                createShift(instr, x, shift, IrArithmeticInstruction.IrArithmeticType.shl);
                return;
            } else if (type == IrArithmeticInstruction.IrArithmeticType.sdiv) {
                // Optimized signed division by power of 2
                // (x + (x < 0 ? 2^k - 1 : 0)) >> k
                // Implemented as:
                // sign = x >> 31 (ashr)
                // mask = sign >>> (32 - k) (lshr)
                // dividend = x + mask
                // result = dividend >> k (ashr)
                createDivShift(instr, x, shift);
                return;
            }
        }

        // 3. Mul decomposition
        if (type == IrArithmeticInstruction.IrArithmeticType.mul) {
            // Check cost
            // Cost of Mul = 5
            // Cost of decomposition = (set_bits) * 1 (shift) + (set_bits - 1) * 1 (add)
            // = 2 * set_bits - 1
            int setBits = Integer.bitCount(constVal);
            int cost = 2 * setBits - 1;
            
            if (cost < 5) { // User said "not higher than"
                decomposeMul(instr, x, constVal);
                return;
            }
        }
        
        // 4. Div optimization (Magic Number)
        // Only if we could support 64-bit mul or mulh.
        // Since we added mulh, we can try to implement if multiplier fits in 32 bits?
        // But multiplier for standard algorithm usually overflows 32 bits.
        // We skip for now to ensure correctness (avoiding overflow truncation).
    }

    private static boolean isPowerOfTwo(int n) {
        return n > 0 && (n & (n - 1)) == 0;
    }

    private static void replaceWithConstant(IrInstruction instr, int val) {
        IrConstant c = new IrConstant(val);
        // IrConstant is not an instruction.
        // We can just replace uses.
        instr.modifyAllUsersToNewValue(c);
        instr.removeAllValueUse();
        instr.getParent().getInstructions().remove(instr);
    }

    private static void createNeg(IrInstruction oldInstr, IrValue x) {
        IrBasicBlock bb = oldInstr.getParent();
        int idx = bb.getInstructions().indexOf(oldInstr);

        // sub 0, x
        IrConstant zero = new IrConstant(0);
        IrArithmeticInstruction negInstr = new IrArithmeticInstruction(
                IrArithmeticInstruction.IrArithmeticType.sub, zero, x, false
        );

        bb.getInstructions().add(idx, negInstr);
        negInstr.setParent(bb);

        oldInstr.modifyAllUsersToNewValue(negInstr);
        oldInstr.removeAllValueUse();
        bb.getInstructions().remove(oldInstr);
    }

    private static void createDivShift(IrInstruction oldInstr, IrValue x, int k) {
        IrBasicBlock bb = oldInstr.getParent();
        int idx = bb.getInstructions().indexOf(oldInstr);

        // We want to implement:
        // sign = x >> 31 (ashr)
        // mask = sign >>> (32 - k) (lshr)
        // dividend = x + mask
        // result = dividend >> k (ashr)

        // 1. sign = x >> 31
        IrConstant shift31 = new IrConstant(31);
        IrArithmeticInstruction signInstr = new IrArithmeticInstruction(
                IrArithmeticInstruction.IrArithmeticType.ashr, x, shift31, false
        );
        bb.getInstructions().add(idx, signInstr);
        signInstr.setParent(bb);
        idx++;

        // 2. mask = sign >>> (32 - k)
        IrConstant shiftMaskVal = new IrConstant(32 - k);
        IrArithmeticInstruction maskInstr = new IrArithmeticInstruction(
                IrArithmeticInstruction.IrArithmeticType.lshr, signInstr, shiftMaskVal, false
        );
        bb.getInstructions().add(idx, maskInstr);
        maskInstr.setParent(bb);
        idx++;

        // 3. dividend = x + mask
        IrArithmeticInstruction dividendInstr = new IrArithmeticInstruction(
                IrArithmeticInstruction.IrArithmeticType.add, x, maskInstr, false
        );
        bb.getInstructions().add(idx, dividendInstr);
        dividendInstr.setParent(bb);
        idx++;

        // 4. result = dividend >> k
        IrConstant kVal = new IrConstant(k);
        IrArithmeticInstruction resultInstr = new IrArithmeticInstruction(
                IrArithmeticInstruction.IrArithmeticType.ashr, dividendInstr, kVal, false
        );
        bb.getInstructions().add(idx, resultInstr);
        resultInstr.setParent(bb);
        idx++;

        // Replace old instruction
        oldInstr.modifyAllUsersToNewValue(resultInstr);
        oldInstr.removeAllValueUse();
        bb.getInstructions().remove(oldInstr);
    }

    private static void createShift(IrInstruction oldInstr, IrValue x, int shift, IrArithmeticInstruction.IrArithmeticType type) {
        IrBasicBlock bb = oldInstr.getParent();
        int idx = bb.getInstructions().indexOf(oldInstr);
        
        IrConstant shiftVal = new IrConstant(shift);
        IrArithmeticInstruction newInstr = new IrArithmeticInstruction(
                type, x, shiftVal, false // Don't auto add
        );
        
        // Add before oldInstr
        bb.getInstructions().add(idx, newInstr);
        newInstr.setParent(bb);
        
        oldInstr.modifyAllUsersToNewValue(newInstr);
        oldInstr.removeAllValueUse();
        bb.getInstructions().remove(oldInstr);
    }

    private static void decomposeMul(IrInstruction oldInstr, IrValue x, int constVal) {
        // Decompose into shifts and adds
        // e.g. 6 (110) -> (x << 2) + (x << 1)
        IrBasicBlock bb = oldInstr.getParent();
        int idx = bb.getInstructions().indexOf(oldInstr);
        
        List<Integer> shifts = new ArrayList<>();
        int temp = constVal;
        int bitPos = 0;
        while (temp > 0) {
            if ((temp & 1) == 1) {
                shifts.add(bitPos);
            }
            temp >>= 1;
            bitPos++;
        }
        
        // Generate instructions
        // We need to accumulate results.
        IrValue currentSum = null;
        
        for (int s : shifts) {
            // Create shift
            IrValue shiftedVal;
            if (s == 0) {
                shiftedVal = x;
            } else {
                IrConstant shiftAmt = new IrConstant(s);
                IrArithmeticInstruction shiftInstr = new IrArithmeticInstruction(
                        IrArithmeticInstruction.IrArithmeticType.shl, x, shiftAmt, false
                );
                bb.getInstructions().add(idx, shiftInstr);
                shiftInstr.setParent(bb);
                idx++;
                shiftedVal = shiftInstr;
            }
            
            if (currentSum == null) {
                currentSum = shiftedVal;
            } else {
                // Add
                IrArithmeticInstruction addInstr = new IrArithmeticInstruction(
                        IrArithmeticInstruction.IrArithmeticType.add, currentSum, shiftedVal, false
                );
                bb.getInstructions().add(idx, addInstr);
                addInstr.setParent(bb);
                idx++;
                currentSum = addInstr;
            }
        }
        
        oldInstr.modifyAllUsersToNewValue(currentSum);
        oldInstr.removeAllValueUse();
        bb.getInstructions().remove(oldInstr);
    }
}
