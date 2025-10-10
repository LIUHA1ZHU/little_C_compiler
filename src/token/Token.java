package token;

public class Token {
    private String content;
    private TokenType tokenType;
    private int lineNum;

    public Token(String content, TokenType tokenType, int lineNum) {
        this.content = content;
        this.tokenType = tokenType;
        this.lineNum = lineNum;
    }

    public TokenType getTokenType() {
        return tokenType;
    }

    public int getLineNum() {
        return lineNum;
    }

    @Override
    public String toString() {
        return tokenType + " " + content + "\n";
    }
}
