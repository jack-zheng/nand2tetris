package nand2tetris.compiler11;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class VMWriter {
    private FileWriter myWriter = null;

    public VMWriter(File output) {
        try {
            myWriter = new FileWriter(output);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void writePush(Segment segment, int index) {
        try {
            myWriter.write("push " + segment.getVal() + " " + index + "\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void writePop(Segment segment, int index) {
        try {
            myWriter.write("pop " + segment.getVal() + " " + index + "\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void writeArithmetic(Command cmd) {
        switch (cmd) {
            case MUT:
                writeCall("Math.multiply", 2);
                break;
            case DIV:
                writeCall("Math.divide", 2);
                break;
            default:
                try {
                    myWriter.write(cmd.getVal() + "\n");
                } catch (IOException e) {
                    e.printStackTrace();
                }
        }
    }

    public void writeLabel(String label) {
        try {
            myWriter.write("label " + label + "\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void writeGoto(String label) {
        try {
            myWriter.write("goto " + label + "\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void writeIf(String label) {
        try {
            myWriter.write("if-goto " + label + "\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void writeCall(String name, int nArgs) {
        try {
            myWriter.write("call " + name + " " + nArgs + "\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void writeFunction(String name, int nArgs) {
        try {
            myWriter.write("function " + name + " " + nArgs + "\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void writeReturn() {
        try {
            myWriter.write("return\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void close() {
        try {
            myWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    enum Segment {
        CONST("constant"), ARG("argument"),
        LOCAL("local"), STATIC("static"),
        THIS("this"), THAT("that"),
        POINTER("pointer"), TEMP("temp");

        private final String val;
        Segment(String val) {
            this.val = val;
        }

        public String getVal() {
            return val;
        }
    }

    enum Command {
        ADD("add"), SUB("sub"), NEG("neg"), EQ("eq"),
        GT("gt"), LT("lt"), AND("and"), OR("or"),
        NOT("not"), MUT("mut"), DIV("div");

        private String val;
        Command(String val) {
            this.val = val;
        }

        public String getVal() {
            return val;
        }
    }
}
