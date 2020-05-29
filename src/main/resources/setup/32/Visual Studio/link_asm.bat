set VC_HOME=C:\Program Files (x86)\Microsoft Visual Studio 14.0\VC
call "%VC_HOME%\bin\vcvars32.bat"
"%VC_HOME%\bin\link.exe" %1 /subsystem:console libcmt.lib legacy_stdio_definitions.lib
