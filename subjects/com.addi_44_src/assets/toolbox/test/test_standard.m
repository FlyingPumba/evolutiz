function t=test_standard(x,y,z)
	//test of standard functions
	disp("No of arguments = " + nargin);

	disp(x + "+" + y + " = " + (x+y));

	disp(x + "-" + y + " = " + (x-y));

	disp(x + "*" + y + " = " + (x*y));

	disp(x + "/" + y + " = " + (x/y));

	disp(x + "^" + y + " = " + (x^y));

	newline();

	disp(x + "+" + y + " * " + z + " = " + (x + y * z));

	disp(y + "*" + z + " + " + x + " = " + (y * z + x));

	newline();

	disp(x + " + " + y + " - " + z + " = " + (x + y - z));

	disp(x + " - " + z + " + " + y + " = " + (x - z + y));

	newline();

	disp("(" + x + "+" + y + ") * " + z + " = " + ((x+y) * z));

	disp("(" + x + "+" + y + ") ^ " + z + " = " + ((x+y) ^ z));

	disp("(" + x + "+" + y + ") * (" + y + " + " + z + ") = " + ((x+y) * (y+z)));

	disp("((" + x + "+" + y + ") * " + z + ") ^ " + x + " = " + ((x+y)*z)^x);

    t=1

/*
@GROUP
test
@SYNTAX
TEST_STANDARD(val1, va2, val3)
@DOC
A set of routines for testing standard operations
@NOTES
@EXAMPLES
TEST_STANDARD(1, 2, 3)
@SEE
*/

