function testGlobal001(val)

whos()
global helloGlobal
helloGlobal = val;
whos()