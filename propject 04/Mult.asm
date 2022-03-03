// This file is part of www.nand2tetris.org
// and the book "The Elements of Computing Systems"
// by Nisan and Schocken, MIT Press.
// File name: projects/04/Mult.asm

// Multiplies R0 and R1 and stores the result in R2.
// (R0, R1, R2 refer to RAM[0], RAM[1], and RAM[2], respectively.)
//
// This program only needs to handle arguments that satisfy
// R0 >= 0, R1 >= 0, and R0*R1 < 32768.

// 4x3 = 4+4+4
// sum=0
// for(i=0;i<n;i++) {
//     sum = sum + R0;
// }
// R2 = sum;

// sum=0
@sum
M=0

// i=0
@i
M=0

(LOOP)
@R1
D=M
// n-i
@i
D=D-M
// if n-i=0 end
@SETR2
D;JEQ
// else sum=sum+R0, i++, continue
@R0
D=M
@sum
M=D+M

//i++
@i
M=M+1
@LOOP
0;JMP

// set R2 = sum
(SETR2)
@sum
D=M
@R2
M=D

(END)
@END
0;JMP