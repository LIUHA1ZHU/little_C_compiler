package token;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public enum TokenType {

    IDENFR("*Ident"),

    INTCON("*IntConst"),
    STRCON("*StringConst"),

    // reserved
    CONSTTK("const"),
    INTTK("int"),
    STATICTK("static"),
    BREAKTK("break"),
    CONTINUETK("continue"),
    IFTK("if"),
    MAINTK("main"),
    ELSETK("else"),
    FORTK("for"),
    RETURNTK("return"),
    VOIDTK("void"),
    PRINTFTK("printf"),

    // operators
    NOT("!"),
    AND("&&"),
    OR("||"),
    PLUS("+"),
    MINU("-"),
    MULT("*"),
    DIV("/"),
    MOD("%"),
    LSS("<"),
    LEQ("<="),
    GRE(">"),
    GEQ(">="),
    EQL("=="),
    NEQ("!="),
    ASSIGN("="),

    // delimiters
    SEMICN(";"),
    COMMA(","),
    LPARENT("("),
    RPARENT(")"),
    LBRACK("["),
    RBRACK("]"),
    LBRACE("{"),
    RBRACE("}"),

    ERRORTK("*errorA");

    private final String value;

    TokenType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    private static final HashMap<String, TokenType> tokenTypeMap = new HashMap<>();

    static {
        for (TokenType tokenType : TokenType.values()) {
            tokenTypeMap.put(tokenType.getValue(), tokenType);
        }
    }

    public static final  List<Character> singleCharList = Arrays.asList(
            '!', '+', '-', '*', '/', '%', '<', '>', ';', ',', '(', ')', '[', ']', '{', '}', '='
    );

    public static boolean isSingleChar(char c) {
        return singleCharList.contains(c);
    }

    // && || <= >= == !=
    public static final List<Character> possibleFirstCharList = Arrays.asList(
            '&', '|', '<', '>', '=', '!'
    );

    public static boolean isPossibleFirstChar(char c) {
        return possibleFirstCharList.contains(c);
    }

    public static TokenType getTokenType(String value) {
        return tokenTypeMap.get(value);
    }
}
