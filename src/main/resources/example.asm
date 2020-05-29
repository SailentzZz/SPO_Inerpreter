section .text
    global  _main
    extern  _printf

_main:
    mov eax, 42
    ; печатаем содержимое регистра EAX:
    ; помещаем в стек 2 параметра printf в обратном порядке
    push eax
    push message_to_print
    ; вызываем функцию printf
    call _printf
    ; убираем параметры из стека
    pop ebx
    pop ebx
    ; завершаем вызов
    ret

section .data
message_to_print:
    db  'Hello, World %d', 10, 0
