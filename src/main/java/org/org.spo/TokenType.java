package org.org.spo;

import java.util.regex.Pattern;

public enum TokenType {
    NUMBER("[0-9]+"),
    ID("[a-zA-Z_][a-zA-Z_0-9]*"),
    ADD("\\+"),
    SUB("-"),
    MUL("\\*"),
    DIV("/"),
    LPAR("\\("),
    RPAR("\\)"),
    SPACE("[ \t\r\n]+");

    final Pattern pattern;

    TokenType(String regexp) {
        pattern = Pattern.compile(regexp);
    }
}
