b=[5,6,7]
a=[2,3,5]
yindex=0;   
   


for index = 1:length (b)
    index
    yindex
      if (all (a != b (index)))
        y(yindex++) = b(index)
      endif
    endfor
    