package utils;

public class Config {
    private static final String workingDir = System.getProperty("user.dir");

    private static final boolean atLocal = false;

    public static final boolean LexerOutput = true;

    public static final boolean ErrorOutput = true;

    public static final boolean ParserOutput = true;

    public static final boolean SymbolOutput = true;

    public static final boolean IROutput = true;

    public static final String LocalInputFilePath = workingDir + "/test_cases/testfile_ir.txt";

    public static final String OnlineInputFilePath = "testfile.txt";

    public static final String LocalLexerOutputPath = workingDir + "/output/lexer.txt";

    public static final String OnlineLexerOutputPath = "lexer.txt";

    public static final String LocalErrorPath = workingDir + "/output/error.txt";

    public static final String OnlineErrorPath = "error.txt";

    public static final String LocalParserOutputPath = workingDir + "/output/parser.txt";

    public static final String OnlineParserOutputPath = "parser.txt";

    public static final String LocalSymbolOutputPath =  workingDir + "/output/symbol.txt";

    public static final String OnlineSymbolOutputPath = "symbol.txt";

    public static final String LocalIROutputPath = workingDir + "/output/llvm_ir.txt";

    public static final String OnlineIROutputPath = "llvm_ir.txt";


    // all set
    public static final String LexerOutputPath = atLocal ? LocalLexerOutputPath : OnlineLexerOutputPath;

    public static final String ErrorPath = atLocal ? LocalErrorPath : OnlineErrorPath;

    public static final String InputFilePath = atLocal ? LocalInputFilePath : OnlineInputFilePath;

    public static final String ParserOutputPath = atLocal ? LocalParserOutputPath : OnlineParserOutputPath;

    public static final String SymbolOutputPath = atLocal ? LocalSymbolOutputPath : OnlineSymbolOutputPath;

    public static final String IROutputPath = atLocal ? LocalIROutputPath : OnlineIROutputPath;
}
