package nand2tetris.assembler06;

import java.util.HashMap;
import java.util.Map;

/**
 * 助记符翻译成二进制码 e.g. ADM -> 111
 */
public class Code {
    private static final Map<String, String> destMap = new HashMap<>();
    private static final Map<String, String> compMap = new HashMap<>();
    private static final Map<String, String> jumpMap = new HashMap<>();

    static {
        destMap.put(null, "000");
        destMap.put("M", "001");
        destMap.put("D", "010");
        destMap.put("MD", "011");
        destMap.put("A", "100");
        destMap.put("AM", "101");
        destMap.put("AD", "110");
        destMap.put("AMD", "111");

        compMap.put(null,  "0000000");
        compMap.put("0",   "0101010");
        compMap.put("1",   "0111111");
        compMap.put("-1",  "0111010");
        compMap.put("D",   "0001100");
        compMap.put("A",   "0110000");
        compMap.put("!D",  "0001101");
        compMap.put("!A",  "0110001");
        compMap.put("-D",  "0001111");
        compMap.put("-A",  "0110011");
        compMap.put("D+1", "0011111");
        compMap.put("A+1", "0110111");
        compMap.put("D-1", "0001110");
        compMap.put("A-1", "0110010");
        compMap.put("D+A", "0000010");
        compMap.put("D-A", "0010011");
        compMap.put("A-D", "0000111");
        compMap.put("D&A", "0000000");
        compMap.put("D|A", "0010101");
        // M related
        compMap.put("M",   "1110000");
        compMap.put("!M",  "1110001");
        compMap.put("-M",  "1110011");
        compMap.put("M+1", "1110111");
        compMap.put("M-1", "1110010");
        compMap.put("D+M", "1000010");
        compMap.put("D-M", "1010011");
        compMap.put("M-D", "1000111");
        compMap.put("D&M", "1000000");
        compMap.put("D|M", "1010101");

        jumpMap.put(null, "000");
        jumpMap.put("JGT", "001");
        jumpMap.put("JEQ", "010");
        jumpMap.put("JGE", "011");
        jumpMap.put("JLT", "100");
        jumpMap.put("JNE", "101");
        jumpMap.put("JLE", "110");
        jumpMap.put("JMP", "111");
    }
    public static String dest(String target) {
        return destMap.get(target);
    }

    public static String comp(String target) {
        return compMap.get(target);
    }

    public static String jump(String target) {
        return jumpMap.get(target);
    }

    public static void main(String[] args) {
        System.out.println(Code.dest("AMD"));
        System.out.println(Code.dest("ADM"));
    }
}
