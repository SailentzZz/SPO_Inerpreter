set VC_HOME=C:\Program Files (x86)\Microsoft Visual Studio 14.0\VC
call "%VC_HOME%\bin\amd64\vcvars64.bat"
"%VC_HOME%\bin\amd64\link.exe" %1 /subsystem:console libcmt.lib legacy_stdio_definitions.lib
