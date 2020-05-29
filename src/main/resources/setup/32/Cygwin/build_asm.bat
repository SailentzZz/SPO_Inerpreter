@echo off
set CYG_HOME=C:\cygwin64

for %%f in (%1) do (
  del %%~nf.obj 2>nul
  del %%~nf.exe 2>nul
)
"%CYG_HOME%\bin\nasm.exe" -f win32 %1
for %%f in (%1) do (
  if exist %%~nf.obj "%CYG_HOME%\bin\i686-pc-cygwin-gcc.exe" -o %%~nf %%~nf.obj
)
