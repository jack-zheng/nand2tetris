package nand2tetris.vmtranslator07;

public class Main {
    public static void main(String[] args) throws Exception {
        String filePath = "/07/StackArithmetic/StackTest/StackTest.vm";
        String outPath = filePath.split("\\.")[0] + ".asm";


        System.out.println(filePath);
        Parser parser = new Parser(filePath);
        CodeWriter writer = new CodeWriter(outPath);
        writer.setFileName("Sys");

        while (parser.hasMoreCommands()) {
            parser.advance();
            VmCmdType type = parser.commandType();
            switch (type) {
                case C_ARITHMETIC:
                    writer.writeArithmethic(parser.currentCmd);
                    break;
                case C_PUSH:
                case C_POP:
                    writer.writePushPop(parser.commandType(), parser.arg1(), parser.arg2());
                    break;
                case C_LABEL:
                    writer.writeLabel(parser.arg1());
                    break;
                case C_IF:
                    writer.writeIf(parser.arg1());
                    break;
                case C_GOTO:
                    writer.writeGoto(parser.arg1());
                    break;
                case C_FUNCTION:
                    writer.writeFunction(parser.arg1(), parser.arg2());
                    break;
                case C_RETURN:
                    writer.writeReturn();
                    break;
                case C_CALL:
                    writer.writeCall(parser.arg1(), parser.arg2());
                    break;
                default:
                    throw new RuntimeException("Unsupported cmd type: " + type);
            }
        }
        writer.close();
    }
}
