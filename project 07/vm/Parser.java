package com.jk.vm;

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

    public Parser(String fileName) {
        System.out.println("load file: " + fileName);
        File myObj = new File(Objects.requireNonNull(Parser.class.getResource(fileName)).getFile());
        try {
            myReader = new Scanner(myObj);
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

    public VMCMDTYPE commandType() {
        if (currentCmd.startsWith("push"))
            return VMCMDTYPE.C_PUSH;
        else if (currentCmd.startsWith("pop"))
            return VMCMDTYPE.C_POP;
        else
            return VMCMDTYPE.C_ARITHMETIC;
    }

    public String arg1() {
        VMCMDTYPE type = commandType();

        if (type.equals(VMCMDTYPE.C_RETURN))
            throw new RuntimeException("arg1() is not available in return command");

        if (type.equals(VMCMDTYPE.C_ARITHMETIC))
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
        System.out.println(parser.commandType());
        System.out.println(parser.arg1());
        //System.out.println(parser.arg2());
    }

    private static void printPushPop(Parser parser) {
        System.out.println(parser.commandType());
        System.out.println(parser.arg1());
        System.out.println(parser.arg2());
    }
}
