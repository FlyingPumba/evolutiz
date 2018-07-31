
function outData = cast(inData, inType)

  if (nargin != 2)
    usage ("cast (data,'type')");
  endif

  if (ischar(inType))

    if (strcmp(inType,'uint8'))
       outData = uint8(inData);
    elseif (strcmp(inType,'uint16'))
       outData = uint16(inData); 
    elseif (strcmp(inType,'uint32'))
       outData = uint32(inData);
    elseif (strcmp(inType,'uint64'))
       outData = uint64(inData);
    elseif (strcmp(inType,'int8'))
       outData = int8(inData);
    elseif (strcmp(inType,'int16'))
       outData = int16(inData); 
    elseif (strcmp(inType,'int32'))
       outData = int32(inData);
    elseif (strcmp(inType,'int64'))
       outData = int64(inData);
    elseif (strcmp(inType,'single'))
       outData = single(inData);
    elseif (strcmp(inType,'double'))
       outData = double(inData);
    else
       error ("cast: illegal type specified");
    endif

  else
    error ("cast: expecting string type 2nd argument");
  endif

endfunction

/*
@GROUP
general
@SYNTAX
cast(data, 'type')
@DOC
Outputs the data cast as 'type'
@NOTES
@EXAMPLES
@SEE
CCX
*/
