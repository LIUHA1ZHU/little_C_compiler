package error;

import utils.FileIO;

import java.util.ArrayList;

public class ErrorHandler {
    private static final ArrayList<Error> errorList = new ArrayList<>();

    private ErrorHandler() {}

    public static void addError(Error error) {
        errorList.add(error);
    }

    public static void outputError() {
        errorList.sort((o1, o2) -> {
            if(o1.getLineNum() ==  o2.getLineNum()){
                return o1.getErrorType().toString().compareTo(o2.getErrorType().toString());
            }
            return o1.getLineNum() - o2.getLineNum();
        });
        StringBuilder sb = new StringBuilder();
        for (Error error : errorList) {
            sb.append(error);
        }
        FileIO.write(FileIO.IOType.ERROR, sb.toString());
    }
}
