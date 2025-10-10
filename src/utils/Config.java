package utils;

public class Config {
    private static final String workingDir = System.getProperty("user.dir");

    private static final boolean atLocal = true;

    public static final boolean LexerOutput = false;

    public static final boolean ErrorOutput = true;

    public static final boolean ParserOutput = true;

    public static final String LocalInputFilePath = workingDir + "/test_cases/testfile_syn.txt";

    public static final String OnlineInputFilePath = "testfile.txt";

    public static final String LocalLexerOutputPath = workingDir + "/output/lexer.txt";

    public static final String OnlineLexerOutputPath = "lexer.txt";

    public static final String LocalErrorPath = workingDir + "/output/error.txt";

    public static final String OnlineErrorPath = "error.txt";

    public static final String LocalParserOutputPath = workingDir + "/output/parser.txt";

    public static final String OnlineParserOutputPath = "parser.txt";


    // all set
    public static final String LexerOutputPath = atLocal ? LocalLexerOutputPath : OnlineLexerOutputPath;

    public static final String ErrorPath = atLocal ? LocalErrorPath : OnlineErrorPath;

    public static final String InputFilePath = atLocal ? LocalInputFilePath : OnlineInputFilePath;

    public static final String ParserOutputPath = atLocal ? LocalParserOutputPath : OnlineParserOutputPath;
}
