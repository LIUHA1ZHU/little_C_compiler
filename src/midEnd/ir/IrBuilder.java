package midEnd.ir;

import error.ErrorHandler;
import midEnd.ir.values.*;
import midEnd.ir.values.instructions.IrBranchInstruction;
import midEnd.ir.values.instructions.IrInstructionType;
import midEnd.ir.values.instructions.IrReturnInstruction;
import utils.FileIO;

import java.util.ArrayList;
import java.util.Stack;

public class IrBuilder {
    public static final String LocalPrefix = "%";
    public static final String GlobalPrefix = "@";
    public static final String declares =
            "declare i32 @getint()          \n" +
            "declare void @putint(i32)      \n" +
            "declare void @putch(i32)       \n" +
            "declare void @putstr(i8*)      \n\n";

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
    private static int stringConstantNum = 0;

    // stack of arrayList of branchInstr to set destination
    private static Stack<ArrayList<IrBranchInstruction>> forStepStack = new Stack<>();
    private static Stack<ArrayList<IrBranchInstruction>> forEndStack = new Stack<>();

    public static boolean isInGlobal() {
        return inGlobal;
    }

    public static String getTempVarNum() {
        return "t." + tempVarNum++;
    }

    public static int getBlockNum() {
        return blockNum++;
    }

    public static int getStringConstantNum() {
        return stringConstantNum++;
    }

    public static void addGlobalVariable(IrGlobalVariable globalVariable) {
        if (ErrorHandler.hasError()) return;
        module.addGlobalValue(globalVariable);
    }

    public static void addStringConstant(IrStringConstant stringConstant) {
        if (ErrorHandler.hasError()) return;
        module.addGlobalValue(stringConstant);
    }
    //--------------------------------
    //          Function
    //--------------------------------
    public static IrValue createFunc(String name, IrFunction.IrFunctionType functionType) {

        inGlobal = false;
        curFunction = new IrFunction(name, functionType);
        return curFunction;
    }

    public static void setParamIR(ArrayList<IrVariable> parameters) {
        curFunction.setParameters(parameters);
    }

    public static void finishFuncAndAddToModule() {

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
        if (ErrorHandler.hasError()) return;
        lastBranchInstr.setTrueDestination(basicBlock);
    }

    public static void finishBasicBlockAndAddToFunc() {
        if (ErrorHandler.hasError()) return;
        if (curBasicBlock.notEndWithTerminator() && curFunction.getIrFunctionType().equals(IrFunction.IrFunctionType.voidFunc)) {
            new IrReturnInstruction("void", IrInstructionType.ReturnVoidInstr);
        }
        //if (curBasicBlock.notEndWithTerminator() && !ErrorHandler.hasError()) throw new RuntimeException("WARNING: IrBasicBlock doesn't end with terminator");
        curFunction.addBasicBlock(curBasicBlock);
        curBasicBlock = null;
    }

    public static IrBasicBlock getCurBasicBlock() {
        return curBasicBlock;
    }
//--------------------------------


    public static Stack<ArrayList<IrBranchInstruction>> getForStepStack() {
        return forStepStack;
    }

    public static Stack<ArrayList<IrBranchInstruction>> getForEndStack() {
        return forEndStack;
    }

    //--------------------------------
    public static void outputIR() {
        if (ErrorHandler.hasError()) return;
        FileIO.write(FileIO.IOType.IR, declares + module.toString());
    }
}
