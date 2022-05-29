package nand2tetris.compiler11;

import nand2tetris.vmtranslator08.Main;

import java.io.File;
import java.io.FileFilter;
import java.util.Objects;

public class JackCompiler {
    public static void main(String[] args) throws Exception {
        String filePath = "/12/Pong_9";
        File source = new File(Main.class.getResource("/").getPath() + filePath);

        FileFilter filter = pathname -> pathname.getName().lastIndexOf(".jack") > 0;

        for (File var : Objects.requireNonNull(source.listFiles(filter))) {
            parseFile(var);
        }
    }

    public static void parseFile(File file) {
        String jackFile = file.getPath();
        new CompilationEngine(new File(jackFile));
    }
}
