rem ���� make_console.bat
rem ���������� � ���������� ���������� ����������
rem � Borland Builder C++ 5.5
rem ���������� ����������
path c:\temp\BCC55\bin;%path%
set include=c:\temp\BCC55\include
set lib=c:\temp\BCC55\lib
rem ������� ������� ���������� ����������
if exist %appp%.exe del %app%.exe
if exist %appp%.obj del %app%.obj
rem ������ �����������
bcc32.exe -I%include% -L%lib% level9.c generic.c > errout.txt
