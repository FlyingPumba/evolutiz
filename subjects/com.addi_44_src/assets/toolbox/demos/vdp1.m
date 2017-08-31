function dy=vdp1(t,y)
// van der Pol differential equation with m=1
//disp('function vdp1');
//disp(nargin);

dy = zeros(2,1);
dy(1) = y(2);
dy(2) = 1*(1-y(1)*y(1))*y(2) - y(1);


/*
@GROUP
Demos
@SYNTAX
vdp1(t,y)
@DOC
van der Pol differential equation with m=1
@EXAMPLES
@NOTES
@SEE
vdp2
*/
