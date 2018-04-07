f_add				lw r2, -4(r14)		% loading variable value in register
				lw r3, -8(r14)		% loading variable value in register
				add r1, r2, r3
				sw -12(r14), r1		% storing expression value in the moon memory
				lw r1, -12(r14)		% loading variable value in register
				sw temp_var(r0), r1	% store the return value in a temporary variable
				jr r15			% jump back to the calling function
				entry
				addi r14, r0, topaddr	% stack pointer
				addi r1, r0, 62		% processing value: 62
				sw -4(r14), r1		% storing expression value in the moon memory
				addi r1, r0, 7		% processing value: 7
				sw -8(r14), r1		% storing expression value in the moon memory
				lw r2, -4(r14)		% loading variable value in register
				sw -20(r14), r2		% passing argument to the function parameter
				lw r2, -8(r14)		% loading variable value in register
				sw -24(r14), r2		% passing argument to the function parameter
				addi r14, r14, -16	% updating the stack pointer for the function call
				jl r15, f_add		% function call
				subi r14, r14, -16	% updating the stack pointer for the function call
				lw r2, temp_var(r0)	% value returned by function
				sw -16(r14), r2		% storing the returned value
				lw r2, -16(r14)		% loading variable value in register
				addi r3, r0, 6		% processing value: 6
				add r1, r2, r3
				sw -12(r14), r1		% storing expression value in the moon memory
				lw r1, -12(r14)		% loading variable value in register
				putc r1			% output the variable value
				hlt
				
temp_var				res	4