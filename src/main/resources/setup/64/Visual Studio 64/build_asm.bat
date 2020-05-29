@echo off
set NASM=nasm.exe

for %%f in (%1) do (
  del %%~nf.obj 2>nul
  del %%~nf.exe 2>nul
)
"%NASM%" -f win64 %1
for %%f in (%1) do (
  if exist %%~nf.obj call link_asm.bat %%~nf.obj
)
