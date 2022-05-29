package nand2tetris.vmtranslator08;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Scanner;

public class Parser {
    public String currentCmd = "";
    public int lineNum = 0;
    private Scanner myReader;

    private List<String> contents = new ArrayList<>();

    public Parser() {
    }

    public Parser(File source) {
        try {
            myReader = new Scanner(source);
        } catch (Exception e) {
            e.printStackTrace();
        }

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

        System.out.println("File contents: ");
        contents.forEach(System.out::println);
    }

    public boolean hasMoreCommands() {
        return lineNum < contents.size();
    }

    public void advance() {
        currentCmd = contents.get(lineNum);
        lineNum++;
    }

    public VmCmdType commandType() {
        if (currentCmd.startsWith("push"))
            return VmCmdType.C_PUSH;
        else if (currentCmd.startsWith("pop"))
            return VmCmdType.C_POP;
        else if (currentCmd.startsWith("label"))
            return VmCmdType.C_LABEL;
        else if (currentCmd.startsWith("if-goto"))
            return VmCmdType.C_IF;
        else if (currentCmd.startsWith("goto"))
            return VmCmdType.C_GOTO;
        else if(currentCmd.startsWith("return"))
            return VmCmdType.C_RETURN;
        else if(currentCmd.startsWith("function"))
            return VmCmdType.C_FUNCTION;
        else if(currentCmd.startsWith("call"))
            return VmCmdType.C_CALL;
        else
            return VmCmdType.C_ARITHMETIC;
    }

    public String arg1() {
        VmCmdType type = commandType();

        if (type.equals(VmCmdType.C_RETURN))
            throw new RuntimeException("arg1() is not available in return command");

        if (type.equals(VmCmdType.C_ARITHMETIC))
            return currentCmd;

        String[] arr = currentCmd.split(" ");
        return arr[1];
    }

    public int arg2() {
        switch (commandType()) {
            case C_PUSH:
            case C_POP:
            case C_FUNCTION:
            case C_CALL:
                String[] arr = currentCmd.split(" ");
                return Integer.parseInt(arr[2]);
            default:
                throw new RuntimeException("unsupported...");

        }
    }

    public static void main(String[] args) {
        Parser parser = new Parser();

        parser.currentCmd = "push constant 10";
        printPushPop(parser);

        parser.currentCmd = "pop this 6";
        printPushPop(parser);

        parser.currentCmd = "add";
        printSingleArgCmd(parser);
        //System.out.println(parser.arg2());

        parser.currentCmd = "label LOOP_START";
        printSingleArgCmd(parser);
        //System.out.println(parser.arg2());
        parser.currentCmd = "if-goto LOOP_START";
        printSingleArgCmd(parser);
        //System.out.println(parser.arg2());
        parser.currentCmd = "goto LOOP_START";
        printSingleArgCmd(parser);
        //System.out.println(parser.arg2());
    }

    private static void printSingleArgCmd(Parser parser) {
        System.out.println(parser.commandType());
        System.out.println(parser.arg1());
    }

    private static void printPushPop(Parser parser) {
        System.out.println(parser.commandType());
        System.out.println(parser.arg1());
        System.out.println(parser.arg2());
    }
}
