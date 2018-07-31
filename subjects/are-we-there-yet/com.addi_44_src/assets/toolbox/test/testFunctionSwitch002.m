function t=test_switch(x)
	switch(x)
		case(1):
			disp("your choice is x = 1");		
		case(2):
			disp("your choice is x = 2");
		case(3):
			disp("your choice is x = 3");
		otherwise:
			disp("invalid x");
			disp(x);
	end;
	t=1;


/*
@GROUP
test
@SYNTAX
TEST_SWITCH(value)
@DOC
A function to test the switch command.
@NOTES
@EXAMPLES
TEST_SWITCH(2)
your choice is x = 2

TEST_SWITCH(5)
invalid x
5
@SEE
*/
        
