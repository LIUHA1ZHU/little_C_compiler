import error.ErrorHandler;
import frontend.Lexer;
import frontend.Parser;
import midEnd.MidEnd;
import midEnd.symbol.SymbolManager;
import utils.Config;
import utils.FileIO;

public class Compiler {
    private String source = "";

    public void compile() {
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

        SymbolManager.init();
        MidEnd midEnd = new MidEnd(parser.getRootNode());
        midEnd.visit();
        if (Config.SymbolOutput) {
            SymbolManager.outputSymbol();
        }


        if (Config.ErrorOutput) {
            ErrorHandler.outputError();
        }
    }

    public static void main(String[] args) {
        Compiler compiler = new Compiler();
        compiler.compile();
    }
}
