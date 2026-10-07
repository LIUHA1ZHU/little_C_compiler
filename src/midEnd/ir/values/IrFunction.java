package midEnd.ir.values;

import backend.mips.MipsBuilder;
import backend.mips.Register;
import backend.mips.assembly.MipsAlu;
import backend.mips.assembly.MipsLabel;
import backend.mips.assembly.MipsLsu;
import midEnd.ir.IrValue;
import midEnd.ir.IrValueType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class IrFunction extends IrGlobalValue {
    public enum IrFunctionType {
        voidFunc,
        intFunc,
    }
    private final IrFunctionType irFunctionType;
    private final ArrayList<IrBasicBlock> basicBlocks;
    private ArrayList<IrVariable> parameters;

    private HashMap<IrValue, Register> valueRegisterMap;

    public IrFunction(String name, IrFunctionType functionType) {
        super(name, IrValueType.Function);
        this.irFunctionType = functionType;
        this.basicBlocks = new ArrayList<>();
        this.parameters = new ArrayList<>();
        this.valueRegisterMap = new HashMap<>();
    }

    public IrFunctionType getIrFunctionType() {
        return irFunctionType;
    }

    public void setParameters(ArrayList<IrVariable> parameters) {
        this.parameters = parameters;
    }

    public ArrayList<IrBasicBlock> getBasicBlocks() {
        return basicBlocks;
    }

    public ArrayList<IrVariable> getParameters() {
        return parameters;
    }

    public void addBasicBlock(IrBasicBlock irBasicBlock) {
        basicBlocks.add(irBasicBlock);
    }

    public HashMap<IrValue, Register> getValueRegisterMap() {
        return this.valueRegisterMap;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (name.equals("main")) {
             sb.append("define dso_local i32 @main() {\n");
        } else {
            if (irFunctionType.equals(IrFunctionType.voidFunc)) {
                sb.append("define dso_local void @").append(name);
            } else {
                sb.append("define dso_local i32 @").append(name);
            }

            sb.append("(");
            for (int i = 0; i < parameters.size(); i++) {
                IrVariable var = parameters.get(i);
                sb.append(var.isArray() ? "i32* " : "i32 ").append(var.getName());
                if (i != parameters.size() -1) sb.append(", ");
            }
            sb.append(")");

            sb.append(" {\n");

        }
        for (IrBasicBlock basicBlock : basicBlocks) {
            sb.append(basicBlock.toString());
        }
        return sb.append("}\n").toString();
    }

    public void toMips() {
        new MipsLabel(getMipsName());
        MipsBuilder.SetCurrentFunction(this);

        // Capture start index for back-patching
        java.util.ArrayList<backend.mips.assembly.MipsAssembly> textSegment =
            backend.mips.MipsBuilder.GetCurrentModule().GetTextSegment();
        int startIndex = textSegment.size();

        // Snapshot of allocator decisions
        // Note: MipsBuilder.SetCurrentFunction uses the LIVE map object.
        // We must be careful not to overwrite the allocation info needed for the body.
        Map<IrValue, Register> allocatorMap = new HashMap<>(this.getValueRegisterMap());

        int totalParamCount = this.parameters.size();

            // Collect actions for first 3 parameters
            java.util.List<ParamAction> actions = new java.util.ArrayList<>();

            for (int i = 0; i < this.parameters.size(); i++) {
                IrVariable param = this.parameters.get(i);

                // Calculate Positive Offset in Caller's Stack Frame
                // Logic: Offset = 4 * (Total - 1 - i)
                int offsetFromSp = 4 * (totalParamCount - 1 - i);

                // Manually set stack offset for this parameter (Positive!)
                MipsBuilder.SetStackValueOffset(param, offsetFromSp);

                Register allocatedReg = allocatorMap.get(param);

                if (i < 3) {
                    // Passed in Register ($a1-$a3)
                    Register incomingReg = Register.get(Register.A0.ordinal() + i + 1);

                    if (allocatedReg != null) {
                        // Allocator assigned a register.
                        MipsBuilder.AllocateRegForParam(param, allocatedReg);
                        if (allocatedReg != incomingReg) {
                            actions.add(new ParamAction(incomingReg, allocatedReg, null));
                        }
                    } else {
                        // Spilled.
                        // Store incoming reg to its reserved stack slot (Positive Offset)
                        actions.add(new ParamAction(incomingReg, null, param));
                    }
                } else {
                    // Passed on Stack (arguments > 3)

                    if (allocatedReg != null) {
                        // Load directly into allocated register
                        MipsBuilder.AllocateRegForParam(param, allocatedReg);
                        new MipsLsu(MipsLsu.LsuType.LW, allocatedReg, Register.SP, offsetFromSp);
                    } else {
                        // Spilled.
                        // It is ALREADY on stack at offsetFromSp.
                        // MipsBuilder knows its location (we SetStackValueOffset above).
                        // So no code generation needed!
                    }
                }
            }

            // Execute actions for first 3 parameters safely
            while (!actions.isEmpty()) {
                int safeIndex = -1;
                for (int i = 0; i < actions.size(); i++) {
                    ParamAction action = actions.get(i);
                    Register dst = action.dst;
                    // Check if dst overwrites any source of OTHER actions
                    if (dst == null) {
                        // Writing to stack (spill) never overwrites a register source
                        // So it's safe unless... wait.
                        // Spilling reads src. It doesn't write dst.
                        // So it's always safe to execute?
                        // Yes, because it consumes src and produces nothing (register-wise).
                        // It doesn't block anyone.
                        safeIndex = i;
                        break;
                    }

                    boolean isSource = false;
                    for (int j = 0; j < actions.size(); j++) {
                        if (i == j) continue;
                        if (actions.get(j).src == dst) {
                            isSource = true;
                            break;
                        }
                    }
                    if (!isSource) {
                        safeIndex = i;
                        break;
                    }
                }

                if (safeIndex != -1) {
                    ParamAction action = actions.remove(safeIndex);
                    if (action.dst != null) {
                        // Move
                        new MipsAlu(MipsAlu.AluType.ADDU, action.dst, action.src, Register.ZERO);
                    } else {
                        // Spill
                        int localOffset = MipsBuilder.GetStackValueOffset(action.param);
                        new MipsLsu(MipsLsu.LsuType.SW, action.src, Register.SP, localOffset);
                    }
                } else {
                    // Cycle detected.
                    ParamAction action = actions.get(0);
                    // Move src to K0
                    new MipsAlu(MipsAlu.AluType.ADDU, Register.K0, action.src, Register.ZERO);
                    // Update action to read from K0
                    action.src = Register.K0;
                    // K0 is safe source
                }
            }

        for (IrBasicBlock irBasicBlock : this.basicBlocks) {
            irBasicBlock.toMips();
        }

    }

    private static class ParamAction {
        Register src;
        Register dst; // null if spill
        IrVariable param; // for spill info

        public ParamAction(Register src, Register dst, IrVariable param) {
            this.src = src;
            this.dst = dst;
            this.param = param;
        }
    }
}
