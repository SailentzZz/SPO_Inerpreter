section .text
    global  _main
    extern  _printf

_main:
    mov eax, 42
    ; �������� ���������� �������� EAX:
    ; �������� � ���� 2 ��������� printf � �������� �������
    push eax
    push message
    ; �������� ������� printf
    call _printf
    ; ������� ��������� �� �����
    pop ebx
    pop ebx
    ; ��������� �����
    ret

message:
    db  'Hello, World %d', 10, 0
