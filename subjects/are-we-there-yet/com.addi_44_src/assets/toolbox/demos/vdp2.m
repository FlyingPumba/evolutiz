function dy=vdp2(t,y)
// van der Pol differential equation with m=2
//disp('function vdp2');
//disp(nargin);

dy = zeros(2,1);
dy(1) = y(2);
dy(2) = 2*(1-y(1)*y(1))*y(2) - y(1);

/*
@GROUP
Demos
@SYNTAX
vdp2(t,y)
@DOC
van der Pol differential equation with m=2
@EXAMPLES
@NOTES
@SEE
vdp1
*/