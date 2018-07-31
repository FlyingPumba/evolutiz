function t=test_scriptcall(x)

	disp("x = " + x);

	disp("running test_script");

	//modifies x and creates variable y
	test_script;

	disp("x = " + x);

	//should be 30
	t=x+y;

