function t=test_matrixfor()	
	y=0;
	for x=[1,2,3,4],
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
TEST_MATRIXFOR()
@DOC
A function to test the for command using the matrix syntax.
@NOTES
@EXAMPLES
TEST_MATRIXFOR()
@SEE
*/
    
