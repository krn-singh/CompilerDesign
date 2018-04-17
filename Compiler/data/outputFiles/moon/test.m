
entry
addi r14,r0,topaddr		% stack pointer
addi r1,r0,2	% processing value: 2
sw -4(r14),r1	% storing expression value in the moon memory
addi r1,r0,34	% processing value: 34
sw -8(r14),r1	% storing expression value in the moon memory
addi r2,r0,34	% processing value: 34
addi r3,r0,34	% processing value: 34
add r1,r2,r3
sw -12(r14),r1	% storing expression value in the moon memory
lw r1,-12(r14)	% loading variable value in register
putc r1	% output the variable value
hlt
