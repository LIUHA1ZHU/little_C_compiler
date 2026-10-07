import backend.BackEnd;
import backend.PeepHole;
import backend.mips.MipsBuilder;
import error.ErrorHandler;
import frontend.Lexer;
import frontend.Parser;
import midEnd.MidEnd;
import midEnd.ir.IrBuilder;
import midEnd.symbol.SymbolManager;
import optimizers.allocators.GraphColoringAllocator;
import optimizers.analysers.CFGAnalyser;
import optimizers.analysers.DominanceAnalyser;
import optimizers.analysers.LivenessAnalyser;
import optimizers.modifiers.*;
import utils.Config;
import utils.FileIO;

public class Compiler {
    private String source = "";

    public void compile() {
        //frontEnd
        source = FileIO.read();
        Lexer lexer = new Lexer(source);
        lexer.lexicalAnalyse();
        if (Config.LexerOutput) {
            lexer.outputTokenList();
        }

        Parser parser = new Parser(lexer.getTokenList());
        parser.parse();
        if (Config.ParserOutput) {
            parser.outputAST();
        }

        // midEnd
        SymbolManager.init();
        MidEnd midEnd = new MidEnd(parser.getRootNode());
        midEnd.visit();
        if (Config.SymbolOutput) {
            SymbolManager.outputSymbol();
        }

        if (Config.ErrorOutput) {
            ErrorHandler.outputError();
        }

        if (!ErrorHandler.hasError()) {
            // midEnd optimization
            if (Config.optimize) {
                CFGAnalyser.run(IrBuilder.getModule());
                BasicBlockMerger.run(IrBuilder.getModule());
                DominanceAnalyser.run(IrBuilder.getModule());

                Mem2Reg.run(IrBuilder.getModule());
                DeadCodeElimination.run(IrBuilder.getModule());
                DeadStoreElimination.run(IrBuilder.getModule());
                SCCP.run(IrBuilder.getModule());

                PartialRedundancyElimination.run(IrBuilder.getModule());
                LoopInvariantCodeMotion.run(IrBuilder.getModule());
                MulDivOptimization.run(IrBuilder.getModule());

                LivenessAnalyser.run(IrBuilder.getModule());
                GraphColoringAllocator.run(IrBuilder.getModule());
            }

            if (Config.IROutput) {
                IrBuilder.outputIR();
            }

            if (Config.optimize) {
                RemovePhi.run(IrBuilder.getModule());
            }

            // backEnd
            BackEnd backEnd = new BackEnd(IrBuilder.getModule());
            backEnd.buildMips();

            // backEnd optimization
            if (Config.optimize) {
                PeepHole peepHole = new PeepHole(backEnd.getMipsModule());
                peepHole.Peep();
            }

            if (Config.MipsOutput) {
                MipsBuilder.outputMips();
            }
        }
    }

    public static void main(String[] args) {
        Compiler compiler = new Compiler();
        compiler.compile();
    }
}
