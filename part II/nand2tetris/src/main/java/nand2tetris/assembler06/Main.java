package nand2tetris.assembler06;

import java.io.FileWriter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 结果验证有两种方案：
 *  1. 运行 Assembler.sh, 加载自己编译的文件，同时选择源文件，可以进行比较
 *  2. 运行 CUPSimulator.sh 直接执行编译的 Hack 文件看是否可以正确运行
 */
public class Main {
    public static void main(String[] args) throws Exception {
        SymbolTable symbolTable = new SymbolTable();
        System.out.println("Start first time parsing...");
        String filePath = "/06/pong/Pong.asm";
        String outPath = filePath.split("\\.")[0] + ".hack";
        Parser parser = new Parser(Main.class.getResource(filePath).getPath());
        int nextExeLineNum = 0;
        while (parser.hasMoreCommands()) {
            parser.advance();

            if (parser.commandType().equals(Parser.CmdType.L_COMMAND) && !symbolTable.contains(parser.symbol())) {
                // 统计 label 信息，记录到符号表中，规则如下。当遇到 (xxx) 这种标签时，用下一行的行号表示
                symbolTable.addEntry(parser.symbol(), nextExeLineNum);
                continue;
            }
            nextExeLineNum++;
        }
        System.out.println("End first time parsing...");

        System.out.println("Start second time parsing...");
        String writtenLine;
        FileWriter myWriter = new FileWriter(Main.class.getResource("/").getPath() + outPath);

        parser.lineNum = 0;
        int ramAddress = 16;
        int counter = 0;
        while (parser.hasMoreCommands()) {
            parser.advance();

            if (parser.commandType().equals(Parser.CmdType.C_COMMAND)) {
                System.out.println("Line: " + counter + ", C command: " + parser.currentCmd);

                String dest = Code.dest(parser.dest());
                String comp = Code.comp(parser.comp());
                String jump = Code.jump(parser.jump());
                writtenLine = Parser.OP_CODE_C + comp + dest + jump;
                myWriter.write(writtenLine + "\n");
                System.out.println("Parsed machine code: [" + writtenLine + "]");

            } else if (parser.commandType().equals(Parser.CmdType.L_COMMAND)) {
                System.out.println("Line: " + counter + ", L command: " + parser.currentCmd + ", skip transfer");
            } else {
                System.out.println("Line: " + counter + ", A command: " + parser.currentCmd);
                String symbol = parser.symbol();

                int symbolValue;

                if (isInteger(symbol)) {
                    // if it's an integer, like @100, assign to symbolValue directly
                    symbolValue = Integer.parseInt(symbol);
                } else if (symbolTable.contains(symbol)) {
                    // check if symbol is predefined or label, get stored value
                    symbolValue = symbolTable.getAddress(symbol);
                } else {
                    // store to table and give a value to it
                    if (!symbolTable.contains(symbol)) {
                        symbolTable.addEntry(symbol, ramAddress);
                        ramAddress++;
                    }
                    symbolValue = symbolTable.getAddress(symbol);
                }

                // 1, 0000, 0000, 0000, 00000
                String binaryStr = Integer.toBinaryString(0x10000 | symbolValue).substring(2);

                writtenLine = Parser.OP_CODE_A + binaryStr;
                myWriter.write(writtenLine + "\n");
                System.out.println("Parsed machine code: [" + writtenLine + "]");
            }
        }

        myWriter.close();
        System.out.println("Successfully wrote to the file.");
        System.out.println("End parsing...");
    }

    private static boolean isInteger(String val){
        try{
            String regex="^[0-9]\\d*$";
            Pattern p=Pattern.compile(regex);
            Matcher m=p.matcher(val);
            if(m.find()){
                return true;
            }
        }catch(Exception e){
            e.printStackTrace();
        }
        return false;
    }
}
