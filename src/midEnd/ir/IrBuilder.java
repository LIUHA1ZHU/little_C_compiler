package midEnd.ir;

import error.ErrorHandler;
import midEnd.ir.values.*;
import midEnd.ir.values.instructions.IrBranchInstruction;
import midEnd.ir.values.instructions.IrInstructionType;
import utils.FileIO;

import java.util.ArrayList;

public class IrBuilder {
    public static final String LocalPrefix = "%";
    public static final String GlobalPrefix = "@";
    public static final String declares =
            "declare i32 @getint()          ;\n" +
            "declare void @putint(i32)      ;\n" +
            "declare void @putch(i32)       ;\n" +
            "declare void @putstr(i8*)      ;\n";

    private static final IrModule module = new IrModule();
    private static IrFunction curFunction;
    private static IrBasicBlock curBasicBlock;
    private static boolean inGlobal = true;
    private static IrBranchInstruction lastBranchInstr;

    /**
     * name for temporary variables, reset when entering other function
     */
    private static int tempVarNum = 0;
    private static int blockNum = 0;

    public static boolean isInGlobal() {
        return inGlobal;
    }

    public static int getTempVarNum() {
        return tempVarNum++;
    }

    public static int getBlockNum() {
        return blockNum++;
    }

    public static void addGlobalVariable(IrGlobalVariable globalVariable) {
        module.addGlobalValue(globalVariable);
    }
    //--------------------------------
    //          Function
    //--------------------------------
    public static void createFunc(String name, IrFunction.IrFunctionType functionType, ArrayList<IrVariable> parameters) {
        if (ErrorHandler.hasError()) return;

        inGlobal = false;
        curFunction = new IrFunction(name, functionType, parameters);
    }

    public static void finishFuncAndAddToModule() {
        if (ErrorHandler.hasError()) return;

        tempVarNum = 0;
        blockNum = 0;
        inGlobal = true;
        module.addGlobalValue(curFunction);
        curFunction = null;
    }

    //--------------------------------
    //          BasicBlock
    //--------------------------------
    public static IrBasicBlock createBasicBlock(String name) {
        if (ErrorHandler.hasError()) return null;

        curBasicBlock = new IrBasicBlock(name);
        return curBasicBlock;
    }

    public static void addInstr(IrInstruction instruction) {
        if (ErrorHandler.hasError()) return;

        curBasicBlock.addInstr(instruction);
        if (instruction.getInstructionType().equals(IrInstructionType.BranchInstr)) {
            lastBranchInstr = (IrBranchInstruction) instruction;
        }
    }

    public static void setBranchDestination(IrBasicBlock basicBlock) {
        lastBranchInstr.setDestination(basicBlock);
    }

    public static void finishBasicBlockAndAddToFunc() {
        if (ErrorHandler.hasError()) return;
        if (curBasicBlock.notEndWithTerminator()) throw new RuntimeException("WARNING: IrBasicBlock doesn't end with terminator");
        curFunction.addBasicBlock(curBasicBlock);
        curBasicBlock = null;
    }

    //--------------------------------
    public static void outputIR() {
        FileIO.write(FileIO.IOType.IR, declares + module.toString());
    }
}
