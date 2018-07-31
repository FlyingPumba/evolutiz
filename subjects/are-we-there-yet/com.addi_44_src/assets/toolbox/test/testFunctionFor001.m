function t=test_for(z)
	y=0;
	for(x=0;x<=z;x++)
		y=y+x;
		disp("x=" + x);
		disp("y=" + y);
		newline();
	end;
	t=1;
    
    
/*
@GROUP
test
@SYNTAX
testFunctionFor001(limit)
@DOC
A function to test the for command. It adds all integers up to limit.
@NOTES
@EXAMPLES
TEST_COMPLEX(4)
x=0
y=0

x=1
y=1

x=2
y=3

x=3
y=6

x=4
y=10
@SEE
*/
    
