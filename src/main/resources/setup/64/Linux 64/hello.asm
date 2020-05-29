section .text
    global main
    extern printf

main:
    sub rsp, 8        ; required for 16-byte aligned stack (Linux calling convention)
    mov rdi, message  ; parameter 1
    mov rsi, 42       ; parameter 2
    mov al, 0         ; number of floating-point parameters (for variadic-parameter functions like printf)
    mov r11, printf   ; load the address of a function to 64-bit register
    call r11
    add rsp, 8        ; restoring stack
    ret

message:
    db  'Hello, World %d', 10, 0
