#!/bin/sh
exefile="${1%.asm}"
objfile="$exefile.o"
rm -f "$objfile" "$exefile"
nasm -f elf64 $1
gcc -o "$exefile" "$objfile"
