// This file is part of www.nand2tetris.org
// and the book "The Elements of Computing Systems"
// by Nisan and Schocken, MIT Press.
// File name: projects/04/Fill.asm

// Runs an infinite loop that listens to the keyboard input.
// When a key is pressed (any key), the program blackens the screen,
// i.e. writes "black" in every pixel;
// the screen should remain fully black as long as the key is pressed. 
// When no key is pressed, the program clears the screen, i.e. writes
// "white" in every pixel;
// the screen should remain fully clear as long as no key is pressed.

// LOOP
// get KBD input
// if KBD > 0:
//  screen = black
// else:
//  screen = white
// JMP to LOOP

(LOOP)
// init start location
@SCREEN
D=A
@R0
M=D

// get KBD
@KBD
D=M // try comp with M directly

// key press, goto black
@BLACK
D;JGT
// no key press, goto white
@WHITE
D;JEQ

// back to LOOP
@LOOP
0;JMP

// turn screen to black
(BLACK)
@R1
M=-1
@FILL
0;JMP

// screen white
(WHITE)
@R1
M=0
@FILL
0;JMP

// change color till addr to KBD
(FILL)
@KBD
D=A
@R0
A=M
D=D-A
// if equals, end
@LOOP
D;JEQ

// get color
@R1
D=M
// change color
@R0
A=M
M=D
//R0++
@R0
M=M+1
@FILL
0;JMP
