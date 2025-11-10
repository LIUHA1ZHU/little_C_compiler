package error;

public class Error {
    private ErrorType errorType;
    private int lineNum;

    public Error(ErrorType errorType, int lineNum) {
        this.errorType = errorType;
        this.lineNum = lineNum;
    }

    public ErrorType getErrorType() {
        return errorType;
    }

    public int getLineNum() {
        return lineNum;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Error)) return false;
        return ((Error) o).getLineNum() == this.lineNum && ((Error) o).getErrorType().equals(this.getErrorType());
    }

    @Override
    public String toString() {
        return lineNum + " " + errorType + "\n";
    }
}
