function t=test_complex(x,y,a,b)
	//test complex number operations
	temp1 = x + y * img;
	temp2 = a + b * img;

	disp("No of arguments = " + nargin);

	disp(temp1 + " + " + temp2 + " = " + (temp1 + temp2));

	disp(temp1 + " - " + temp2 + " = " + (temp1 - temp2));

	disp(temp1 + " * " + temp2 + " = " + (temp1 * temp2));

	disp(temp1 + " / " + temp2 + " = " + (temp1 / temp2));

	disp(temp1 + " ^ " + temp2 + " = " + (temp1 ^ temp2));

	disp("");

	disp("sin(" + temp1 + ") = " + sin(temp1));

	disp("cos(" + temp1 + ") = " + cos(temp1));

	disp("tan(" + temp1 + ") = " + tan(temp1));

	disp("conj(" + temp1 + ") = " + conj(temp1));

	disp("real(" + temp1 + ") = " + real(temp1));

	disp("imag(" + temp1 + ") = " + imag(temp1));

	disp("e^(" + temp1 + ") = " + exp(temp1));

	disp("ln(" + temp1 + ") = " + ln(temp1));

	disp("");

	disp("sin(" + temp1 + "+" + temp2 + ") = " + sin(temp1+temp2));

	disp("cos(" + temp1 + "+" + temp2 + ") = " + cos(temp1+temp2));

	#disp("tan(" + temp1 + "+" + temp2 + ") = " + tan(temp1+temp2));

    t=1;
    
/*
@GROUP
test
@SYNTAX
TEST_COMPLEX(real1, im1, re2, im2)
@DOC
A set of routines for testing complex numebrs
@NOTES
@EXAMPLES
TEST_COMPLEX(1, 1, 2, 2)
@SEE
*/

