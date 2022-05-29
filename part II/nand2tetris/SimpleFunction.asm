// function SimpleFunction.test
(SimpleFunction.test)
// push constant 0
@0
D=A
@SP
A=M
M=D
@SP
M=M+1
// push constant 0
@0
D=A
@SP
A=M
M=D
@SP
M=M+1
// push local 0
@0
D=A
@LCL
A=D+M
D=M
@SP
A=M
M=D
@SP
M=M+1
// push local 1
@1
D=A
@LCL
A=D+M
D=M
@SP
A=M
M=D
@SP
M=M+1
// add
@SP
AM=M-1
D=M
A=A-1
M=D+M
// not
@SP
A=M-1
M=!M
// push argument 0
@0
D=A
@ARG
A=D+M
D=M
@SP
A=M
M=D
@SP
M=M+1
// add
@SP
AM=M-1
D=M
A=A-1
M=D+M
// push argument 1
@1
D=A
@ARG
A=D+M
D=M
@SP
A=M
M=D
@SP
M=M+1
// sub
@SP
AM=M-1
D=M
A=A-1
M=M-D
// return
// FRAME = LCL
@LCL
D=M
@FRAME
M=D
// RET = *(FRAME - 5)
@5
A=D-A
D=M
@RET
M=D
// *ARG = pop()
@SP
A=M-1
D=M
@ARG
A=M
M=D
// SP = ARG + 1
@ARG
D=M+1
@SP
M=D
// THAT = *(FRAME - 1)
@1
D=A
@FRAME
A=M-D
D=M
@THAT
M=D
// THAT = *(FRAME - 2)
@2
D=A
@FRAME
A=M-D
D=M
@THIS
M=D
// THAT = *(FRAME - 3)
@3
D=A
@FRAME
A=M-D
D=M
@ARG
M=D
// THAT = *(FRAME - 4)
@4
D=A
@FRAME
A=M-D
D=M
@LCL
M=D
@RET
A=M
0;JMP
