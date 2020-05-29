package org.org.spo;

import org.spo.ASTree.BinOpNode;
import org.spo.ASTree.ExprNode;
import org.spo.ASTree.NumbNode;
import org.spo.ASTree.VarNode;

import java.util.Arrays;
import java.util.List;

public class Parser {

    private final List<Token> tokens;
    private int pos = 0;

    public Parser(List<Token> tokens) {
        this.tokens = tokens;
    }

    private void error(String message) {
        if (pos < tokens.size()) {
            Token t = tokens.get(pos);
            throw new RuntimeException(message + " в позиции " + t.pos);
        } else {
            throw new RuntimeException(message + " в конце файла");
        }
    }

    private Token match(TokenType... expected) {
        if (pos < tokens.size()) {
            Token curr = tokens.get(pos);
            if (Arrays.asList(expected).contains(curr.type)) {
                pos++;
                return curr;
            }
        }
        return null;
    }

    private Token require(TokenType... expected) {
        Token t = match(expected);
        if (t == null)
            error("Ожидается " + Arrays.toString(expected));
        return t;
    }

    private ExprNode parseElem() {
        Token num = match(TokenType.NUMBER);
        if (num != null)
            return new NumbNode(num);
        Token id = match(TokenType.ID);
        if (id != null)
            return new VarNode(id);
        error("Ожидается число или переменная");
        return null;
    }

    private ExprNode parseMnozh() {
        if (match(TokenType.LPAR) != null) {
            ExprNode e = parseExpression();
            require(TokenType.RPAR);
            return e;
        } else {
            return parseElem();
        }
    }

    public ExprNode parseSlag() {
        ExprNode e1 = parseMnozh();
        Token op;
        while ((op = match(TokenType.MUL, TokenType.DIV)) != null) {
            ExprNode e2 = parseMnozh();
            e1 = new BinOpNode(op, e1, e2);
        }
        return e1;
    }

    public ExprNode parseExpression() {
        ExprNode e1 = parseSlag();
        Token op;
        while ((op = match(TokenType.ADD, TokenType.SUB)) != null) {
            ExprNode e2 = parseSlag();
            e1 = new BinOpNode(op, e1, e2);
        }
        return e1;
    }
}
