package nand2tetris.compiler11;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class SymbolTable {
    private Map<String, SymbolEntry> classTable;
    private Map<String, SymbolEntry> subroutineTable;

    public SymbolTable() {
        classTable = new HashMap<>();
        subroutineTable = new HashMap<>();
    }

    public void startSubroutine() {
        subroutineTable.clear();
    }

    /**
     * e.g. static boolean test;
     *
     * @param name test
     * @param type boolean
     * @param kind static
     */
    public void define(String name, String type, Kind kind) {

        SymbolEntry entry = new SymbolEntry(type, kind, nextIndex(kind));

        switch (kind) {
            case ARG:
            case VAR:
                // check if existing first
                if (subroutineTable.containsKey(name)) {
                    throw new IllegalArgumentException("Name: " + name + " already existed in subroutine table");
                }
                // put to subroutine table
                subroutineTable.put(name, entry);
                break;
            default:
                // check if existing first
                // put to class table
                if (classTable.containsKey(name)) {
                    throw new IllegalArgumentException("Name: " + name + " already existed in class table");
                }
                classTable.put(name, entry);
        }
    }

    enum Kind {
        STATIC, FIELD, ARG, VAR;
    }

    /**
     * 返回 segment 类型的 count， 可以在声明 function 的时候使用，
     * 那是我们需要写类似 function Main.main 0 的声明，0 即是这个方法的返回值
     *
     * @param kind
     * @return
     */
    public int varCount(Kind kind) {
        int count = 0;
        Set<Map.Entry<String, SymbolEntry>> entrySet;
        if (kind.equals(Kind.VAR) || kind.equals(Kind.ARG)) {
            entrySet = subroutineTable.entrySet();
        } else {
            entrySet = classTable.entrySet();
        }

        for (Map.Entry<String, SymbolEntry> entry : entrySet) {
            if (entry.getValue().getKind().equals(kind)) {
                count++;
            }
        }
        return count;
    }


    /**
     * symbolTable 中添加新元素的时候用，添加时需要指定下标，
     * 比如当前时 local 1，那么下一个就是 local 2
     * @param kind
     * @return
     */
    public int nextIndex(Kind kind) {
        // 根据 kind 类型确定要操作的 table 是哪个
        Map<String, SymbolEntry> map = kind.equals(Kind.ARG) || kind.equals(Kind.VAR) ? subroutineTable : classTable;

        // 计算 kind 的 index
        int count = 0;
        for (Map.Entry<String, SymbolEntry> entry : map.entrySet()) {
            if (entry.getValue().kind.equals(kind)) {
                count++;
            }
        }
        return count;
    }

    /**
     * 返回当前作用域内的标识符的种类，如果未知，返回 none
     *
     * @param name
     * @return
     */
    public Kind kindOf(String name) {
        // 先从 subroutine 中找，再从 class 中找
        SymbolEntry entry = null;
        entry = subroutineTable.get(name);
        if (entry == null) {
            entry = classTable.get(name);
        }
        return entry == null ? null : entry.getKind();
    }

    /**
     * 返回当前作用域内标识符的类型
     *
     * @param name
     * @return
     */
    public String typeOf(String name) {
        // 先从 subroutine 中找，再从 class 中找
        SymbolEntry entry = null;
        entry = subroutineTable.get(name);
        if (entry == null) {
            entry = classTable.get(name);
        }
        return entry == null ? null : entry.getType();
    }

    public int indexOf(String name) {
        // 先从 subroutine 中找，再从 class 中找
        SymbolEntry entry = null;
        entry = subroutineTable.get(name);
        if (entry == null) {
            entry = classTable.get(name);
        }
        return entry == null ? -1 : entry.getIndex();
    }


    class SymbolEntry {
        private String type;
        private Kind kind;
        private int index;

        public SymbolEntry(String type, Kind kind, int index) {
            this.type = type;
            this.kind = kind;
            this.index = index;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public int getIndex() {
            return index;
        }

        public void setIndex(int index) {
            this.index = index;
        }

        public Kind getKind() {
            return kind;
        }

        public void setKind(Kind kind) {
            this.kind = kind;
        }
    }

    public static void main(String[] args) {
        SymbolTable table = new SymbolTable();
        System.out.println(table.nextIndex(Kind.VAR));
        System.out.println(table.nextIndex(Kind.STATIC));

        // var boolean isGood;
        System.out.println("Define a new VAR...");
        table.define("isGood", "boolean", Kind.VAR);
        System.out.println(table.nextIndex(Kind.VAR));
        System.out.println(table.nextIndex(Kind.STATIC));

        // static int count;
        System.out.println("Define a new STATIC...");
        table.define("count", "int", Kind.STATIC);
        System.out.println(table.nextIndex(Kind.VAR));
        System.out.println(table.nextIndex(Kind.STATIC));

        // when start a new subroutine, subroutine table will be cleaned first
        System.out.println("Start new subroutine...");
        table.startSubroutine();
        System.out.println(table.nextIndex(Kind.VAR));

        // 在 class 和 subroutine 总定义同名变量，优先取 subroutine
        System.out.println("Test kindOf/typeOf...");
        table.define("scope01", "int", Kind.STATIC);
        table.define("scope01", "String", Kind.VAR);
        // expected value: VAR
        System.out.println(table.kindOf("scope01") + "|" + table.typeOf("scope01"));

        // 如果没定义过，返回 null
        System.out.println(table.kindOf("undefined"));

        // varCount
        System.out.println("Test varCount...");
        // expected 2
        System.out.println(table.nextIndex(Kind.STATIC));
        // expected 1
        System.out.println(table.nextIndex(Kind.VAR));

        // duplicate test
        System.out.println("Duplicate test...");
        //table.define("scope01", "Int", Kind.VAR);
    }
}
