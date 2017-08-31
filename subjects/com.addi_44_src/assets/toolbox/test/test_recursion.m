function result=test_recursion(x)
	clear("result");
	if(x <= 0)
	{
		result = 0;
	}
	else
	{
		clear("result");
		printstacktrace("test"),
		result = x + test_recursion(x-1);
	};
