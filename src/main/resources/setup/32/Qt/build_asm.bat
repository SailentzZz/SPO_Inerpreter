@echo off
set NASM=nasm.exe

for %%f in (%1) do (
  del %%~nf.obj 2>nul
  del %%~nf.exe 2>nul
)
"%NASM%" -f win32 %1
for %%f in (%1) do (
  if exist %%~nf.obj gcc -o %%~nf %%~nf.obj
)
