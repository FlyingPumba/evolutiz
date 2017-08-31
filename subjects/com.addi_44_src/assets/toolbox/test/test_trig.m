function t=test_trig(x)
	//test trigonometric functions
	disp("No of arguments = " + nargin);

	disp("sin(" + x + ")  = " + sin(x));

	disp("cos(" + x + ")  = " + cos(x));

	disp("tan(" + x + ")  = " + tan(x));

	disp("");

	disp("arc sin(sin(" + x + "))  = " + asin(sin(x)));

	disp("arc cos(cos(" + x + "))  = " + acos(cos(x)));

	disp("arc tan(tan(" + x + "))  = " + atan(tan(x)));

	//disp("sin(" + x + ")  / cos(" + x + ")  = " + (sin(x)/cos(x)));

    	t=1;

/*
@GROUP
test
@SYNTAX
TEST_TRIG(value)
@DOC
A function to test the trigonometric functions.
@NOTES
@EXAMPLES
TEST_TRIG(2)
@SEE
*/
    
