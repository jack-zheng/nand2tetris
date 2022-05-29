package nand2tetris.vmtranslator08;

import java.io.File;
import java.io.FileFilter;
import java.util.Objects;

public class Main {
    public static void main(String[] args) throws Exception {
        String filePath = "/08/FunctionCalls/StaticsTest";
        File source = new File(Main.class.getResource("/").getPath() + filePath);

        if (source.isDirectory()) {
            String outPath = source.getPath() + "/" + source.getName() + ".asm";
            CodeWriter writer = new CodeWriter(new File(outPath));
            // 处理文件夹时添加初始化程序
            writer.writeInit();
            handleFolder(source, writer);
            writer.close();
        } else {
            String outPath = source.getPath().split("\\.")[0] + ".asm";
            CodeWriter writer = new CodeWriter(new File(outPath));
            handleFile(source, writer);
            writer.close();
        }
    }

    private static void handleFile(File source, CodeWriter writer) throws Exception {
        writer.setFileName(source.getName().split("\\.")[0]);

        Parser parser = new Parser(source);
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
    }


    private static void handleFolder(File source, CodeWriter writer) throws Exception {
        FileFilter filter = pathname -> pathname.getName().lastIndexOf(".vm") > 0;
        for (File vmFile : Objects.requireNonNull(source.listFiles(filter))) {
            handleFile(vmFile, writer);
        }
    }
}
