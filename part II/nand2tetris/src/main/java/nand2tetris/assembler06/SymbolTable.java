package nand2tetris.assembler06;

import java.util.HashMap;
import java.util.Map;

/**
 * 符号表，存储 变量-地址 对
 */
public class SymbolTable {

    private Map<String, Integer> map = new HashMap<>();

    public SymbolTable() {
        // 初始化内置的 23 个符号变量
        map.put("SP", 0);
        map.put("LCL", 1);
        map.put("ARG", 2);
        map.put("THIS", 3);
        map.put("THAT", 4);

        map.put("R0", 0);
        map.put("R1", 1);
        map.put("R2", 2);
        map.put("R3", 3);
        map.put("R4", 4);
        map.put("R5", 5);
        map.put("R6", 6);
        map.put("R7", 6);
        map.put("R8", 7);
        map.put("R9", 8);
        map.put("R10", 10);
        map.put("R11", 11);
        map.put("R12", 12);
        map.put("R13", 13);
        map.put("R14", 14);
        map.put("R15", 15);

        map.put("SCREEN", 16384);
        map.put("KBD", 24576);
    }

    public boolean contains(String key) {
        return map.containsKey(key);
    }

    public int getAddress(String key) {
        return map.get(key);
    }

    public void addEntry(String key, int value) {
        map.put(key, value);
    }
}
