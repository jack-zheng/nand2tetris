package nand2tetris.assembler06;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Parser {
    public int lineNum = 0;
    public String currentCmd = "";

    public static final String OP_CODE_C = "111";
    public static final String OP_CODE_A = "0";

    private Scanner myReader;

    private final List<String> contents = new ArrayList<>();

    public enum CmdType {
        A_COMMAND, C_COMMAND, L_COMMAND;
    }

    public Parser() {
    }

    public Parser(String fileName) {
        System.out.println("load file: " + fileName);

        File myObj = new File(fileName);
        try {
            myReader = new Scanner(myObj);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // 去除多余内容，包括每行的首尾空格，行注释，行内注释
        while (myReader.hasNextLine()) {
            String line = myReader.nextLine();
            // remove redundant space
            line = line.trim();
            // if it's empty line or comment line, skip it
            if (line.isEmpty() || line.startsWith("//")) {
                continue;
            }
            // remove inline comment
            if (line.contains("//")) {
                line = line.split("//")[0];
                line = line.trim();
            }

            // after format, save it to contents
            contents.add(line);
        }

        myReader.close();
    }

    public boolean hasMoreCommands() {
        return lineNum < contents.size();
    }

    public void advance() {
        currentCmd = contents.get(lineNum);
        lineNum++;
    }

    public CmdType commandType() {
        if (currentCmd.startsWith("@")) {
            return CmdType.A_COMMAND;
        } else if (currentCmd.startsWith("(") && currentCmd.endsWith(")")) {
            return CmdType.L_COMMAND;
        } else {
            return CmdType.C_COMMAND;
        }
    }

    public String symbol() {
        if (currentCmd.startsWith("@")) {
            // for @Xxx case
            return currentCmd.substring(1);
        } else {
            // for (Xxx) case
            return currentCmd.substring(1, currentCmd.length() - 1);
        }
    }

    public String dest() {
        // D=M+1;JGT
        // 0;JMP
        // D=M+1
        // M+1
        if (!currentCmd.contains("=")) {
            return null;
        }

        String ret = currentCmd.split("=")[0];
        if (ret.isEmpty()) {
            return null;
        } else {
            return ret;
        }
    }

    public String comp() {
        // D=M+1;JGT
        // 0;JMP
        // D=M+1

        String ret = currentCmd.split(";")[0];
        if (ret.contains("=")) {
            return ret.split("=")[1];
        } else {
            return ret;
        }
    }

    public String jump() {
        // D=M+1;JGT
        // 0;JMP
        // D=M+1
        String[] ret = currentCmd.split(";");
        if (ret.length == 1) {
            return null;
        } else {
            return ret[1];
        }
    }

    /**
     * Testing
     *
     * @param args
     */
    public static void main(String[] args) {
        Parser parser = new Parser();

        parser.currentCmd = "D=A";
        Parser.printCInfo(parser);

        parser.currentCmd = "M=M+1;JEQ";
        Parser.printCInfo(parser);

        parser.currentCmd = "AMD=M+1;JEQ";
        Parser.printCInfo(parser);

        parser.currentCmd = "0;JMP";
        Parser.printCInfo(parser);

        parser.currentCmd = "@i";
        Parser.printAInfo(parser);

        parser.currentCmd = "@sum";
        Parser.printAInfo(parser);

        parser.currentCmd = "(END)";
        Parser.printAInfo(parser);


    }

    private static void printCInfo(Parser parser) {
        System.out.println("parsed cmd: " + parser.currentCmd);
        System.out.println("cmd type: " + parser.commandType());
        System.out.println("cmd dest: " + parser.dest());
        System.out.println("cmd comp: " + parser.comp());
        System.out.println("cmd jump: " + parser.jump());
        System.out.println("----------------- * -----------------");
    }

    private static void printAInfo(Parser parser) {
        System.out.println("parsed cmd: " + parser.currentCmd);
        System.out.println("cmd type: " + parser.commandType());
        System.out.println("cmd symbol: " + parser.symbol());
        System.out.println("----------------- * -----------------");
    }
}
