// sys init 
@256
D=A
@SP
M=D
// call Sys.init 0
@RETURN_ADDR_0
D=A
@SP
A=M
M=D
@SP
M=M+1
@LCL
D=M
@SP
A=M
M=D
@SP
M=M+1
@ARG
D=M
@SP
A=M
M=D
@SP
M=M+1
@THIS
D=M
@SP
A=M
M=D
@SP
M=M+1
@THAT
D=M
@SP
A=M
M=D
@SP
M=M+1
@SP
D=M
@0
D=D-A
@5
D=D-A
@ARG
M=D
@SP
D=M
@LCL
M=D
@Sys.init
0,JMP
(RETURN_ADDR_0)
// function Class1.set
(Class1.set)
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
// pop static 0
@SP
AM=M-1
D=M
@Class1.0
M=D
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
// pop static 1
@SP
AM=M-1
D=M
@Class1.1
M=D
// push constant 0
@0
D=A
@SP
A=M
M=D
@SP
M=M+1
//  return
//  FRAME = LCL
@LCL
D=M
@FRAME
M=D
//  RET = *(FRAME - 5)
@5
A=D-A
D=M
@RET
M=D
//  *ARG = pop()
@SP
A=M-1
D=M
@ARG
A=M
M=D
//  SP = ARG + 1
@ARG
D=M+1
@SP
M=D
//  THAT = *(FRAME - 1)
@1
D=A
@FRAME
A=M-D
D=M
@THAT
M=D
//  THAT = *(FRAME - 2)
@2
D=A
@FRAME
A=M-D
D=M
@THIS
M=D
//  THAT = *(FRAME - 3)
@3
D=A
@FRAME
A=M-D
D=M
@ARG
M=D
//  THAT = *(FRAME - 4)
@4
D=A
@FRAME
A=M-D
D=M
@LCL
M=D
@RET
A=M
0,JMP
// function Class1.get
(Class1.get)
// push static 0
@Class1.0
D=M
@SP
A=M
M=D
@SP
M=M+1
// push static 1
@Class1.1
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
//  return
//  FRAME = LCL
@LCL
D=M
@FRAME
M=D
//  RET = *(FRAME - 5)
@5
A=D-A
D=M
@RET
M=D
//  *ARG = pop()
@SP
A=M-1
D=M
@ARG
A=M
M=D
//  SP = ARG + 1
@ARG
D=M+1
@SP
M=D
//  THAT = *(FRAME - 1)
@1
D=A
@FRAME
A=M-D
D=M
@THAT
M=D
//  THAT = *(FRAME - 2)
@2
D=A
@FRAME
A=M-D
D=M
@THIS
M=D
//  THAT = *(FRAME - 3)
@3
D=A
@FRAME
A=M-D
D=M
@ARG
M=D
//  THAT = *(FRAME - 4)
@4
D=A
@FRAME
A=M-D
D=M
@LCL
M=D
@RET
A=M
0,JMP
// function Sys.init
(Sys.init)
// push constant 6
@6
D=A
@SP
A=M
M=D
@SP
M=M+1
// push constant 8
@8
D=A
@SP
A=M
M=D
@SP
M=M+1
// call Class1.set 2
@RETURN_ADDR_1
D=A
@SP
A=M
M=D
@SP
M=M+1
@LCL
D=M
@SP
A=M
M=D
@SP
M=M+1
@ARG
D=M
@SP
A=M
M=D
@SP
M=M+1
@THIS
D=M
@SP
A=M
M=D
@SP
M=M+1
@THAT
D=M
@SP
A=M
M=D
@SP
M=M+1
@SP
D=M
@2
D=D-A
@5
D=D-A
@ARG
M=D
@SP
D=M
@LCL
M=D
@Class1.set
0,JMP
(RETURN_ADDR_1)
// pop temp 0
@5
D=A
@0
D=D+A
@R13
M=D
@SP
AM=M-1
D=M
@R13
A=M
M=D
// push constant 23
@23
D=A
@SP
A=M
M=D
@SP
M=M+1
// push constant 15
@15
D=A
@SP
A=M
M=D
@SP
M=M+1
// call Class2.set 2
@RETURN_ADDR_2
D=A
@SP
A=M
M=D
@SP
M=M+1
@LCL
D=M
@SP
A=M
M=D
@SP
M=M+1
@ARG
D=M
@SP
A=M
M=D
@SP
M=M+1
@THIS
D=M
@SP
A=M
M=D
@SP
M=M+1
@THAT
D=M
@SP
A=M
M=D
@SP
M=M+1
@SP
D=M
@2
D=D-A
@5
D=D-A
@ARG
M=D
@SP
D=M
@LCL
M=D
@Class2.set
0,JMP
(RETURN_ADDR_2)
// pop temp 0
@5
D=A
@0
D=D+A
@R13
M=D
@SP
AM=M-1
D=M
@R13
A=M
M=D
// call Class1.get 0
@RETURN_ADDR_3
D=A
@SP
A=M
M=D
@SP
M=M+1
@LCL
D=M
@SP
A=M
M=D
@SP
M=M+1
@ARG
D=M
@SP
A=M
M=D
@SP
M=M+1
@THIS
D=M
@SP
A=M
M=D
@SP
M=M+1
@THAT
D=M
@SP
A=M
M=D
@SP
M=M+1
@SP
D=M
@0
D=D-A
@5
D=D-A
@ARG
M=D
@SP
D=M
@LCL
M=D
@Class1.get
0,JMP
(RETURN_ADDR_3)
// call Class2.get 0
@RETURN_ADDR_4
D=A
@SP
A=M
M=D
@SP
M=M+1
@LCL
D=M
@SP
A=M
M=D
@SP
M=M+1
@ARG
D=M
@SP
A=M
M=D
@SP
M=M+1
@THIS
D=M
@SP
A=M
M=D
@SP
M=M+1
@THAT
D=M
@SP
A=M
M=D
@SP
M=M+1
@SP
D=M
@0
D=D-A
@5
D=D-A
@ARG
M=D
@SP
D=M
@LCL
M=D
@Class2.get
0,JMP
(RETURN_ADDR_4)
// label WHILE
(Sys.init$WHILE)
// goto WHILE
@Sys.init$WHILE
0,JMP
// function Class2.set
(Class2.set)
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
// pop static 0
@SP
AM=M-1
D=M
@Class2.0
M=D
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
// pop static 1
@SP
AM=M-1
D=M
@Class2.1
M=D
// push constant 0
@0
D=A
@SP
A=M
M=D
@SP
M=M+1
//  return
//  FRAME = LCL
@LCL
D=M
@FRAME
M=D
//  RET = *(FRAME - 5)
@5
A=D-A
D=M
@RET
M=D
//  *ARG = pop()
@SP
A=M-1
D=M
@ARG
A=M
M=D
//  SP = ARG + 1
@ARG
D=M+1
@SP
M=D
//  THAT = *(FRAME - 1)
@1
D=A
@FRAME
A=M-D
D=M
@THAT
M=D
//  THAT = *(FRAME - 2)
@2
D=A
@FRAME
A=M-D
D=M
@THIS
M=D
//  THAT = *(FRAME - 3)
@3
D=A
@FRAME
A=M-D
D=M
@ARG
M=D
//  THAT = *(FRAME - 4)
@4
D=A
@FRAME
A=M-D
D=M
@LCL
M=D
@RET
A=M
0,JMP
// function Class2.get
(Class2.get)
// push static 0
@Class2.0
D=M
@SP
A=M
M=D
@SP
M=M+1
// push static 1
@Class2.1
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
//  return
//  FRAME = LCL
@LCL
D=M
@FRAME
M=D
//  RET = *(FRAME - 5)
@5
A=D-A
D=M
@RET
M=D
//  *ARG = pop()
@SP
A=M-1
D=M
@ARG
A=M
M=D
//  SP = ARG + 1
@ARG
D=M+1
@SP
M=D
//  THAT = *(FRAME - 1)
@1
D=A
@FRAME
A=M-D
D=M
@THAT
M=D
//  THAT = *(FRAME - 2)
@2
D=A
@FRAME
A=M-D
D=M
@THIS
M=D
//  THAT = *(FRAME - 3)
@3
D=A
@FRAME
A=M-D
D=M
@ARG
M=D
//  THAT = *(FRAME - 4)
@4
D=A
@FRAME
A=M-D
D=M
@LCL
M=D
@RET
A=M
0,JMP
