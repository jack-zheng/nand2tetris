package com.jk;

import java.io.FileWriter;

public class MainL {
    public static void main(String[] args) throws Exception {
        System.out.println("Start parsing...");

        String writtenLine;
        FileWriter myWriter = new FileWriter(MainL.class.getResource(".").getPath() + "RectL.hack");

        Parser parser = new Parser(MainL.class.getResource("RectL.asm").getPath());
        while (parser.hasMoreCommands()) {
            parser.advance();
            if (parser.currentCmd.isEmpty() || parser.currentCmd.startsWith("//")) {
                //System.out.printf("It's a comment line: [%s], skip it.%n", parser.currentCmd);
                continue;
            }

            if (parser.commandType().equals(CMDTYPE.C_COMMAND)) {
                System.out.println("C command: " + parser.currentCmd);

                String dest = Code.dest(parser.dest());
                String comp = Code.comp(parser.comp());
                String jump = Code.jump(parser.jump());
                writtenLine = Parser.OP_CODE_C + comp + dest + jump;

            } else {
                System.out.println("A command: " + parser.currentCmd);
                String symbol = parser.symbol();

                int sym = Integer.valueOf(symbol);
                // 1, 0000, 0000, 0000, 00000
                String binaryStr = Integer.toBinaryString(0x10000 | sym).substring(2);

                writtenLine = Parser.OP_CODE_A + binaryStr;
            }
            myWriter.write(writtenLine + "\n");
            System.out.println("Parsed machine code: [" + writtenLine + "]");
        }

        myWriter.close();
        System.out.println("Successfully wrote to the file.");
        System.out.println("End parsing...");
    }
}
