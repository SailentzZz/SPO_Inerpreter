section .text
    global main
    extern printf

main:
    sub rsp, 40       ; reserved for shadow space and 16-byte stack alignment (Windows x64 calling convention)
    mov rcx, message  ; parameter 1
    mov rdx, 42       ; parameter 2
    mov r11, printf   ; load the address of a function to 64-bit register
    call r11
    add rsp, 40       ; restoring stack
    ret

message:
    db  'Hello, World %d', 10, 0
