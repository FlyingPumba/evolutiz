function [tt,yy]=euler(func,t0f,y0,dt)

t0=t0f(1);
tf=t0f(2);

n=(tf-t0)/dt;
t=t0;
k=0;
y=y0;

disp("n   = "+n);
disp("t0  = "+t0);
disp("tf  = "+tf);
disp("func= "+func);

tt=linspace(t0,tf,n);
yy=linspace(0,1,n);
//yy=zeros(n,2);

disp(yy);
disp(n);

while (k<n)
{
disp("k= "+k);

dy = PerformFunction(func,t,y);

k=k+1;
t=t+dt;
y=y+dy*dt;
//disp(y)
yy(k,:)=y;

}
end;


// euler("vdp",[0,10],[2,0]',0.1)
