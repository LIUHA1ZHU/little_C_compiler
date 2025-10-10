package frontend;

import error.Error;
import error.ErrorHandler;
import error.ErrorType;
import token.Token;
import token.TokenType;
import utils.FileIO;

import java.util.ArrayList;
import java.util.List;

public class Lexer {
    private final String inputText;
    private final int maxLength;
    private int pos;
    private int lineNum;
    private Token curToken;

    private final ArrayList<Token> tokenList = new ArrayList<>();

    public Lexer(String inputText) {
        this.inputText = inputText;
        maxLength = inputText.length();
        pos = 0;
        lineNum = 1;
        curToken = null;
    }

    public void lexicalAnalyse() {
        do {
            curToken = next();
            tokenList.add(curToken);
        } while (curToken != null);

    }

    private Token next() {
        do {
            skipWhite();
        } while (skipComment());

        if (pos >= maxLength) {
            return null;
        }

        if (isIdentifierNonDigit(inputText.charAt(pos))) { // identifier or reserved
            matchIdentOrReserved();
        } else if (Character.isDigit(inputText.charAt(pos))) { // intConst
            matchIntConst();
        } else if (inputText.charAt(pos) == '\"') { // stringConst
            matchStringConst();
        } else { // delimiter & operator
            TokenType tokenType;
            int tokenLength = 0;

            // errorA
            if (inputText.charAt(pos) == '&' || inputText.charAt(pos) == '|') {
                boolean isSingleAtEnd = (pos + 1 == maxLength) ||
                        (pos + 1 < maxLength && inputText.charAt(pos + 1) != ((inputText.charAt(pos) == '&') ? '&' : '|'));

                if (isSingleAtEnd) {
                    String operator = (inputText.charAt(pos) == '&') ? "&&" : "||";
                    TokenType type = (inputText.charAt(pos) == '&') ? TokenType.AND : TokenType.OR;

                    curToken = new Token(operator, type, lineNum);
                    Error error = new Error(ErrorType.a, lineNum);
                    ErrorHandler.addError(error);
                    pos++;
                    return curToken;
                }
            }

            if (pos + 2 <= maxLength && TokenType.isPossibleFirstChar(inputText.charAt(pos))) { // possible doubleChar
                tokenType = TokenType.getTokenType(inputText.substring(pos, pos + 2));
                if (tokenType != null) { // doubleChar
                    pos += 2;
                    tokenLength = 2;
                } else { // singleChar
                    tokenType = TokenType.getTokenType(inputText.substring(pos, pos + 1));
                    pos++;
                    tokenLength = 1;
                }
            } else if (pos < maxLength){ // singleChar
                tokenType = TokenType.getTokenType(inputText.substring(pos, pos + 1));
                pos++;
                tokenLength = 1;
            } else {
                System.out.println("WARNING: reaching max length " + lineNum);
                return null;
            }

            if (tokenType != null) {
                curToken = new Token(inputText.substring(pos - tokenLength, pos), tokenType, lineNum);
            } else {
                System.out.println("WARNING: not supposed to be here! unknown token " + lineNum);
            }
        }
        return curToken;
    }

    private void matchIdentOrReserved() {
        int endPos = pos + 1;
        while (endPos < maxLength && (isIdentifierNonDigit(inputText.charAt(endPos)) || Character.isDigit(inputText.charAt(endPos)))) {
            endPos++;
        }
        String content = inputText.substring(pos, endPos);
        pos = endPos;

        // judge TokenType and create token
        TokenType tokenType = TokenType.getTokenType(content);
        if (tokenType != null) {
            curToken = new Token(content, tokenType, lineNum);
        } else {
            // must be identifier
            curToken = new Token(content, TokenType.IDENFR, lineNum);
        }
    }

    private void matchIntConst() {
        int endPos = pos + 1;
        while (endPos < maxLength && Character.isDigit(inputText.charAt(endPos))) {
            endPos++;
        }
        String content = inputText.substring(pos, endPos);
        pos = endPos;

        curToken = new Token(content, TokenType.INTCON, lineNum);
    }

    private void matchStringConst() {
        int endPos = pos + 1;
        while (endPos < maxLength && inputText.charAt(endPos) != '\"') {
            endPos++;
        }
        endPos++;
        String content = inputText.substring(pos, endPos);
        pos = endPos;

        curToken = new Token(content, TokenType.STRCON, lineNum);
    }

    /**
     * skip single-line and multi-line comments
     * @return whether to skip whiteSpace
     * return true if charAt pos is whiteSpace or comment
     * return false if reaching the end
     * return false if charAt pos is not whiteSpace or comment
     */
    private boolean skipComment() {
        // must not be whiteSpace
        if (pos >= maxLength || inputText.charAt(pos) != '/') {
            return false;
        }
        pos++;
        if (inputText.charAt(pos) == '/') {
            // single line
            while (pos < maxLength && inputText.charAt(pos) != '\n') pos++;
            pos++;
            lineNum++;
            return true;
        } else if (inputText.charAt(pos) == '*') {
            // multi line
            pos++;
            while (pos < maxLength) {
                if (inputText.charAt(pos) == '*' && inputText.charAt(pos + 1) == '/') {
                    pos += 2;
                    return true;
                }
                if (inputText.charAt(pos) == '\n') {
                    lineNum++;
                }
                pos++;
            }
        } else {
            pos--;
            return false;
        }
        System.out.println("WARNING: not supposed to be here while skipping comment! " + lineNum);
        return pos < maxLength;
    }

    private void skipWhite() {
        while (pos < maxLength && Character.isWhitespace(inputText.charAt(pos))) {
            if (inputText.charAt(pos) == '\n') {
                lineNum++;
            }
            pos++;
        }
    }

    private boolean isIdentifierNonDigit(char c) {
        return Character.isLetter(c) || c == '_';
    }

    public ArrayList<Token> getTokenList() {
        return tokenList;
    }

    // including error
    public void outputTokenList() {
        StringBuilder sb = new StringBuilder();
        for (Token token : tokenList) {
            if (token != null) sb.append(token);
        }
        FileIO.write(FileIO.IOType.LEXER, sb.toString());
    }
}
