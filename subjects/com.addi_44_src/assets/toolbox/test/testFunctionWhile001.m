function t=test_while(z)
	y=0;
    t=0;
	while(y<z)
    {
		y=y+1;
		t=t+y;
        disp(["y=" num2str(y) " t= " num2str(t)]);
		newline();
	}
    end;
	
    
/*
@GROUP
test
@SYNTAX
TEST_FOR(limit)
@DOC
A function to test the while command. It adds all integers up to limit.
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
    
