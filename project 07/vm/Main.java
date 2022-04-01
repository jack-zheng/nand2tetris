package com.jk.vm;

public class Main {
    public static void main(String[] args) throws Exception {
        Parser parser = new Parser("StackTest.vm");

        CodeWriter writer = new CodeWriter("StackTest.asm");
        writer.setFileName("StackTest");

        while (parser.hasMoreCommands()) {
            parser.advance();
            VMCMDTYPE type = parser.commandType();
            switch (type) {
                case C_ARITHMETIC:
                    writer.writeArithmethic(parser.currentCmd);
                    break;
                case C_PUSH:
                case C_POP:
                    writer.writePushPop(parser.commandType(), parser.arg1(), parser.arg2());
                    break;
                default:
                    throw new RuntimeException("Unsupported cmd type: " + type);
            }
        }
        writer.close();
    }
}
