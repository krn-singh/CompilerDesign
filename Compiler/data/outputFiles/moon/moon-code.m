f_highestMarks				sw -20(r14), r15
				lw r7, -4(r14)		% loading variable value in register
				lw r8, -8(r14)		% loading variable value in register
				cgt r6, r7, r8
				bz r6, else_block1		% jump to else block
				lw r6, -4(r14)		% loading variable value in register
				sw -12(r14), r6		% storing expression value in the moon memory
				j end_if1			% end if-else block
else_block1				lw r6, -8(r14)		% loading variable value in register
				sw -12(r14), r6		% storing expression value in the moon memory
				end_if1 nop
				lw r6, -12(r14)		% loading variable value in register
				sw temp_var(r0), r6	% store the return value in a temporary variable
				lw r15, -20(r14)
				jr r15			% jump back to the calling function
				entry
				addi r14, r0, topaddr	% stack pointer
				addi r6, r0, 80		% processing value: 80
				sw -12(r14), r6		% storing expression value in the moon memory
				addi r6, r0, 120		% processing value: 120
				sw -16(r14), r6		% storing expression value in the moon memory
				lw r6, -12(r14)		% loading variable value in register
				sw -28(r14), r6		% passing argument to the function parameter
				lw r6, -16(r14)		% loading variable value in register
				sw -32(r14), r6		% passing argument to the function parameter
				addi r14, r14, -24	% updating the stack pointer for the function call
				jl r15, f_highestMarks		% function call
				subi r14, r14, -24	% updating the stack pointer for the function call
				lw r6, temp_var(r0)	% value returned by function
				sw -20(r14), r6		% storing the returned value
				lw r6, -20(r14)		% loading variable value in register
				sw -4(r14), r6		% storing expression value in the moon memory
				lw r7, -4(r14)		% loading variable value in register
				lw r8, -12(r14)		% loading variable value in register
				ceq r6, r7, r8
				bz r6, else_block2		% jump to else block
				addi r6, r0, 1		% processing value: 1
				sw -8(r14), r6		% storing expression value in the moon memory
				j end_if2			% end if-else block
else_block2				addi r6, r0, 2		% processing value: 2
				sw -8(r14), r6		% storing expression value in the moon memory
				end_if2 nop
				lw r1, -8(r14)		% loading variable value in register
				jl r15, putint				% output the variable value
				hlt
				
temp_var				res	4
				
% A Simple MOON Library (new version)
% Author: Peter Grogono
% Last modified: 8 March 1995

				 align

% Write an integer to the output file.
% Entry:  r1 contains the integer.
% Uses: r1, r2, r3, r4, r5.
% Link: r15.

putint   			 add    r2,r0,r0         % c := 0 (character)
				 add    r3,r0,r0         % s := 0 (sign)
				 addi   r4,r0,endbuf     % p is the buffer pointer
				 cge    r5,r1,r0
				 bnz    r5,putint1       % branch if n >= 0
				 addi   r3,r0,1          % s := 1
				 sub    r1,r0,r1         % n := -n
putint1  		 	 modi   r2,r1,10         % c := n mod 10
				 addi   r2,r2,48         % c := c + '0'
				 subi   r4,r4,1          % p := p - 1
				 sb     0(r4),r2         % buf[p] := c
				 divi   r1,r1,10         % n := n div 10
				 bnz    r1,putint1       % do next digit
				 bz     r3,putint2       % branch if n >= 0
				 addi   r2,r0,45         % c := '-'
				 subi   r4,r4,1          % p := p - 1
				 sb     0(r4),r2         % buf[p] := c
putint2  			 lb     r2,0(r4)         % c := buf[p]
				 putc   r2               % write c
				 addi   r4,r4,1          % p := p + 1
				 cgei   r5,r4,endbuf
				 bz     r5,putint2       % branch if more digits
				 jr     r15              % return

				 res    20               % digit buffer
endbuf

% Read an integer.
% Exit: R1 contains value of integer read.
% Uses: r1, r2, r3, r4.
% Link: r15.

getint   		 	 add    r1,r0,r0         % n := 0 (result)
				 add    r2,r0,r0         % c := 0 (character)
				 add    r3,r0,r0         % s := 0 (sign)
getint1  		 	 getc   r2               % read c
				 ceqi   r4,r2,32
				 bnz    r4,getint1       % skip blanks
				 ceqi   r4,r2,43
				 bnz    r4,getint2       % branch if c is '+'
				 ceqi   r4,r2,45
				 bz     r4,getint3       % branch if c is not '-'
				 addi   r3,r0,1          % s := 1 (number is negative)
getint2  		 	 getc   r2               % read c
getint3  		 	 ceqi   r4,r2,10
				 bnz    r4,getint5       % branch if c is \n
				 cgei   r4,r2,48
				 bz     r4,getint4       % c < 0
				 clei   r4,r2,57
				 bz     r4,getint4       % c > 9
				 muli   r1,r1,10         % n := 10 * n
				 add    r1,r1,r2         % n := n + c
				 subi   r1,r1,48         % n := n - '0'
				 j      getint2
getint4  		 	 addi   r2,r0,63         % c := '?'
				 putc   r2               % write c
				 j      getint           % Try again
getint5  		 	 bz     r3,getint6       % branch if s = 0 (number is positive)
				 sub    r1,r0,r1         % n := -n
getint6  		 	 jr     r15              % return