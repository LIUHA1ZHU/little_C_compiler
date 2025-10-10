package utils;

import java.io.*;

public class FileIO {
    public enum IOType {
        LEXER, LEXERERROR, PARSER, ERROR
    }

    public static String read() {
        String filePath = Config.InputFilePath;
        File inputFile = new File(filePath);
        String inputText = "";

        try {
            FileReader fileToRead = new FileReader(inputFile);
            StringBuilder stringBuilder = new StringBuilder();
            char[] buffer = new char[30];
            int size;
            while ((size = fileToRead.read(buffer)) != -1) {
                stringBuilder.append(buffer, 0, size);
            }
            inputText = stringBuilder.toString();
        } catch (Exception e) {
            System.out.println("Reading Failed! " + e);
        }
        return inputText;
    }

    /**
     * one time writes all
     * @param ioType determines which file to write
     * @param content to write
     */
    public static void write(IOType ioType, String content) {
        String filePath = "";
        switch (ioType) {
            case LEXER -> filePath = Config.LexerOutputPath;
            case PARSER -> filePath = Config.ParserOutputPath;
            case ERROR -> filePath = Config.ErrorPath;
        }
        File file = new File(filePath);
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                System.out.println("Failed to create file: " + e);
            }
        }
        try {
            fileWrite(filePath, content);
        } catch (IOException e) {
            System.out.println("Writing Failed! " + e);
        }
    }

    private static void fileWrite(String filePath, String content) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(
                new FileWriter(filePath))) {
            writer.write(content);
        }
    }
}
