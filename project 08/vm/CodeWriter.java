package com.jk.vm;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CodeWriter {
    private final FileWriter myWriter;
    private List<String> written = new ArrayList();
    private String fileName = "";
    private int counter = 0;
    private String functionName;
    private int returnCounter = 0;

    public CodeWriter(String output) throws Exception {
        myWriter = new FileWriter(CodeWriter.class.getResource(".").getPath() + output);

    }

    /**
     * 暂时推测是用来为 static segment 处理时提供当前文件名称的
     *
     * @param fileName
     */
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    /**
     * Hack 中有 9 中逻辑运算: add, sub, neg, eq, gt, lt, and, or, not;
     * 前八种有需要两个数才能运算，最后一种只需要一个
     * <p>
     * 实现虽然有点冗余，但是可以 work
     *
     * @param cmd
     */
    public void writeArithmethic(String cmd) throws IOException {
        List<String> ariths = new ArrayList<>();
        // log processed command
        ariths.add("// " + cmd);

        // push 1, push 2, add
        if (cmd.equals("not") || cmd.equals("neg")) {
            // 找到对应的一个数，取反
            // pseudo code: SP--, addr=SP, !(*addr)

            // R0 存储 SP 的值，拿到并 -1
            //  @R0
            //  AM=M-1 // R0-1 并设置地址线
            //  M=!M   // 下一个时钟周期时，M 的值就是 *addr，直接取反
            ariths.add("@SP");
            ariths.add("A=M-1");
            if (cmd.equals("not")) {
                ariths.add("M=!M");
            } else {
                ariths.add("M=-M");
            }

        } else {
            // 找到倒数第一/二个数，做对应的逻辑运算
            // 取数的操作是通用的
            ariths.add("@SP");
            ariths.add("AM=M-1");
            ariths.add("D=M");
            ariths.add("A=A-1");

            if (cmd.equals("add")) {
                ariths.add("M=D+M");
            } else if (cmd.equals("sub")) {
                ariths.add("M=M-D");
            } else if (cmd.equals("and")) {
                ariths.add("M=D&M");
            } else if (cmd.equals("or")) {
                ariths.add("M=D|M");
            } else if (cmd.equals("eq")) {
                ariths.add("D=D-M");
                ariths.add("@EQ" + counter);
                ariths.add("D,JEQ");
                ariths.add("@SP");
                ariths.add("A=M-1");
                ariths.add("M=0");
                ariths.add("@END" + counter);
                ariths.add("0,JMP");
                ariths.add("(EQ" + counter + ")");
                ariths.add("@SP");
                ariths.add("A=M-1");
                ariths.add("M=-1");
                ariths.add("(END" + counter + ")");
            } else if (cmd.equals("lt")) {
                // x < y, true, else false
                // D=y, M=x
                ariths.add("D=D-M");
                ariths.add("@GT" + counter);
                ariths.add("D,JGT");
                ariths.add("@SP");
                ariths.add("A=M-1");
                ariths.add("M=0");
                ariths.add("@END" + counter);
                ariths.add("0,JMP");
                ariths.add("(GT" + counter + ")");
                ariths.add("@SP");
                ariths.add("A=M-1");
                ariths.add("M=-1");
                ariths.add("(END" + counter + ")");
            } else if (cmd.equals("gt")) {
                // x > y, true, else false
                // D=y, M=x
                ariths.add("D=D-M");
                ariths.add("@LT" + counter);
                ariths.add("D,JLT");
                ariths.add("@SP");
                ariths.add("A=M-1");
                ariths.add("M=0");
                ariths.add("@END" + counter);
                ariths.add("0,JMP");
                ariths.add("(LT" + counter + ")");
                ariths.add("@SP");
                ariths.add("A=M-1");
                ariths.add("M=-1");
                ariths.add("(END" + counter + ")");
            }

            counter++;
        }
        written.addAll(ariths);

        for (String line : ariths) {
            myWriter.write(line + "\n");
        }
    }

    /**
     * @param type    only C_PUSH or C_POP is valid.
     * @param segment from the method format, I guess it's the arg1
     * @param val     from the method format, I guess it's the arg2
     */
    public void writePushPop(VMCMDTYPE type, String segment, int val) throws IOException {
        List<String> ariths = new ArrayList<>();

        switch (type) {

            case C_PUSH:
                // log processed command
                ariths.add("// push " + segment + " " + val);
                if (segment.equals("constant")) {
                    // @17      // D=17
                    // D=A
                    // @SP      // *SP=D
                    // A=M      // 将值设定为地址
                    // M=D
                    // @SP      // SP++
                    // M=M+1
                    ariths.add("@" + val);
                    ariths.add("D=A");
                    ariths.add("@SP");
                    ariths.add("A=M");
                    ariths.add("M=D");
                    ariths.add("@SP");
                    ariths.add("M=M+1");
                } else if (segment.equals("static")) {
                    // 这个要根据文件名做设置的，逻辑不太一样
                    ariths.add("@" + fileName + "." + val);
                    ariths.add("D=M");
                    ariths.add("@SP");
                    ariths.add("A=M");
                    ariths.add("M=D");
                    ariths.add("@SP");
                    ariths.add("M=M+1");
                } else if (segment.equals("pointer")) {
                    if (val == 0) {
                        ariths.add("@THIS");
                    } else if (val == 1) {
                        ariths.add("@THAT");
                    } else {
                        throw new RuntimeException("Unsupported pointer val " + val);
                    }

                    ariths.add("D=M");
                    ariths.add("@SP");
                    ariths.add("A=M");
                    ariths.add("M=D");
                    ariths.add("@SP");
                    ariths.add("M=M+1");
                } else if (segment.equals("temp")) {
                    ariths.add("@5");
                    ariths.add("D=A");
                    ariths.add("@" + val);
                    ariths.add("A=D+A");
                    ariths.add("D=M");
                    ariths.add("@SP");
                    ariths.add("A=M");
                    ariths.add("M=D");
                    ariths.add("@SP");
                    ariths.add("M=M+1");
                } else {
                    // push argument 1
                    ariths.add("@" + val);
                    ariths.add("D=A");

                    if (segment.equals("local")) {
                        ariths.add("@LCL");
                    } else if (segment.equals("argument")) {
                        ariths.add("@ARG");
                    } else if (segment.equals("this")) {
                        ariths.add("@THIS");
                    } else if (segment.equals("that")) {
                        ariths.add("@THAT");
                    }

                    ariths.add("A=D+M");    // 拿到偏移地址
                    ariths.add("D=M");
                    ariths.add("@SP");
                    ariths.add("A=M");
                    ariths.add("M=D");      // *SP = *addr
                    ariths.add("@SP");
                    ariths.add("M=M+1");    // SP++
                }

                break;
            case C_POP:
                // log processed command
                ariths.add("// pop " + segment + " " + val);
                if (segment.equals("constant")) {
                    throw new RuntimeException("constant not support pop");
                } else if (segment.equals("static")) {
                    // 这个要根据文件名做设置的，逻辑不太一样
                    ariths.add("@SP");
                    ariths.add("AM=M-1");
                    ariths.add("D=M");
                    ariths.add("@" + fileName + "." + val);
                    ariths.add("M=D");
                } else if (segment.equals("pointer")) {
                    ariths.add("@SP");
                    ariths.add("AM=M-1");
                    ariths.add("D=M");
                    if (val == 0) {
                        ariths.add("@THIS");
                    } else if (val == 1) {
                        ariths.add("@THAT");
                    } else {
                        throw new RuntimeException("Pointer not support val " + val);
                    }
                    ariths.add("M=D");

                } else if (segment.equals("temp")) {
                    // 过程中需要更多的寄存器存储中间变量，这里用了通用寄存器 R13
                    ariths.add("@5");
                    ariths.add("D=A");
                    ariths.add("@" + val);
                    ariths.add("D=D+A");
                    ariths.add("@R13");
                    ariths.add("M=D");
                    ariths.add("@SP");
                    ariths.add("AM=M-1");
                    ariths.add("D=M");
                    ariths.add("@R13");
                    ariths.add("A=M");
                    ariths.add("M=D");
                } else {
                    // 处理方式和 temp 雷同，不过，计算的是偏移量
                    ariths.add("@" + val);
                    ariths.add("D=A");

                    if (segment.equals("local")) {
                        ariths.add("@LCL");
                    } else if (segment.equals("argument")) {
                        ariths.add("@ARG");
                    } else if (segment.equals("this")) {
                        ariths.add("@THIS");
                    } else if (segment.equals("that")) {
                        ariths.add("@THAT");
                    }

                    ariths.add("D=D+M");    // 拿到偏移地址
                    ariths.add("@R13");
                    ariths.add("M=D");
                    ariths.add("@SP");
                    ariths.add("AM=M-1");
                    ariths.add("D=M");
                    ariths.add("@R13");
                    ariths.add("A=M");
                    ariths.add("M=D");
                }
                break;
            default:
                throw new RuntimeException("Other cmd type is not supported");
        }

        for (String line : ariths) {
            myWriter.write(line + "\n");
        }
    }

    public void close() throws Exception {
        System.out.println("Written Content:");
        written.forEach(System.out::println);
        myWriter.close();
    }

    /**
     * 1. 将 SP 置位 256
     * 2. call Sys.init 函数
     */
    public void writeInit() throws IOException {
        myWriter.write("// sys init \n");
        myWriter.write("@256\n");
        myWriter.write("D=A\n");
        myWriter.write("@SP\n");
        myWriter.write("M=D\n");
        writeCall("Sys.init", 0);
    }

    public void writeLabel(String label) throws IOException {
        // 为了确定 label 前缀，感觉这里应该有一个额外的变量 function 来记录当前函数名称
        // 实现方式应该和文件名这个变量的处理方式一样
        // sample: label LOOP_START
        myWriter.write("// label " + label + "\n");
        myWriter.write("(" + functionName + "$" + label + ")\n");
    }

    public void writeGoto(String label) throws IOException {
        myWriter.write("// goto " + label + "\n");
        myWriter.write("@" + functionName + "$" + label + "\n");
        myWriter.write("0,JMP" + "\n");
    }

    public void writeIf(String label) throws IOException {
        myWriter.write("// if-goto " + label + "\n");
        myWriter.write("@SP" + "\n");
        myWriter.write("AM=M-1" + "\n");
        myWriter.write("D=M" + "\n");
        myWriter.write("@" + functionName + "$" + label + "\n");
        myWriter.write("D,JNE" + "\n");
    }

    public void writeCall(String functionName, int numArgs) throws IOException {
        // push return-address
        myWriter.write("// call " + functionName + " " + numArgs + "\n");
        String returnAddr = "RETURN_ADDR_" + returnCounter++;
        myWriter.write("@" + returnAddr + "\n");
        myWriter.write("D=A\n");
        myWriter.write("@SP\n");
        myWriter.write("A=M\n");
        myWriter.write("M=D\n");
        myWriter.write("@SP\n");
        myWriter.write("M=M+1\n");
        // push lcl
        pushElement("@LCL");
        // push arg
        pushElement("@ARG");
        // push this
        pushElement("@THIS");
        // push that
        pushElement("@THAT");
        // arg = sp -n -5
        myWriter.write("@SP\n");
        myWriter.write("D=M\n");
        myWriter.write("@" + numArgs + "\n");
        myWriter.write("D=D-A\n");
        myWriter.write("@5\n");
        myWriter.write("D=D-A\n");
        myWriter.write("@ARG\n");
        myWriter.write("M=D\n");
        // lcl = sp
        myWriter.write("@SP\n");
        myWriter.write("D=M\n");
        myWriter.write("@LCL\n");
        myWriter.write("M=D\n");
        // goto f
        myWriter.write("@" + functionName + "\n");
        myWriter.write("0,JMP\n");
        // (return-address)
        myWriter.write("(" + returnAddr + ")\n");
    }

    private void pushElement(String element) throws IOException {
        myWriter.write(element + "\n");
        myWriter.write("D=M\n");
        myWriter.write("@SP\n");
        myWriter.write("A=M\n");
        myWriter.write("M=D\n");
        myWriter.write("@SP\n");
        myWriter.write("M=M+1\n");
    }

    public void writeReturn() throws IOException {
        myWriter.write("//  return\n");
        // FRAME = LCL
        myWriter.write("//  FRAME = LCL\n");
        myWriter.write("@LCL\n");
        myWriter.write("D=M\n");
        myWriter.write("@FRAME\n");
        myWriter.write("M=D\n");
        // RET = *(FRAME - 5)
        myWriter.write("//  RET = *(FRAME - 5)\n");
        myWriter.write("@5\n");
        myWriter.write("A=D-A\n");
        myWriter.write("D=M\n");
        myWriter.write("@RET\n");
        myWriter.write("M=D\n");
        // *ARG = pop(), 将计算赋值给 arg 0
        myWriter.write("//  *ARG = pop()\n");
        myWriter.write("@SP\n");
        myWriter.write("A=M-1\n");
        myWriter.write("D=M\n");
        myWriter.write("@ARG\n");
        myWriter.write("A=M\n");
        myWriter.write("M=D\n");
        // SP = ARG + 1
        myWriter.write("//  SP = ARG + 1\n");
        myWriter.write("@ARG\n");
        myWriter.write("D=M+1\n");
        myWriter.write("@SP\n");
        myWriter.write("M=D\n");
        // THAT = *(FRAME - 1)
        myWriter.write("//  THAT = *(FRAME - 1)\n");
        recoverSegment("@THAT", 1);
        // THIS = *(FRAME - 2)
        myWriter.write("//  THAT = *(FRAME - 2)\n");
        recoverSegment("@THIS", 2);
        // ARG = *(FRAME - 3)
        myWriter.write("//  THAT = *(FRAME - 3)\n");
        recoverSegment("@ARG", 3);
        // LCL = *(FRAME - 4)
        myWriter.write("//  THAT = *(FRAME - 4)\n");
        recoverSegment("@LCL", 4);
        // goto RET
        myWriter.write("@RET\n");
        myWriter.write("A=M\n");
        myWriter.write("0,JMP\n");
    }

    private void recoverSegment(String segment, int pos) throws IOException {
        myWriter.write("@" + pos + "\n");
        myWriter.write("D=A\n");
        myWriter.write("@FRAME\n");
        myWriter.write("A=M-D\n");
        myWriter.write("D=M\n");
        myWriter.write(segment + "\n");
        myWriter.write("M=D\n");
    }

    public void writeFunction(String functionName, int numLocals) throws IOException {
        myWriter.write("// function " + functionName + "\n");
        this.functionName = functionName;
        myWriter.write("(" + functionName + ")\n");
        for (int i = 0; i < numLocals; i++) {
            writePushPop(VMCMDTYPE.C_PUSH, "constant", 0);
        }
    }
}
