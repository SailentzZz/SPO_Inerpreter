package org.org.spo;

import org.spo.ASTree.BinOpNode;
import org.spo.ASTree.ExprNode;
import org.spo.ASTree.NumbNode;
import org.spo.ASTree.VarNode;

import java.util.List;
import java.util.Scanner;

public class Interpreter {

    public static int eval(ExprNode node) {
        if (node instanceof NumbNode) {
            NumbNode num = (NumbNode) node;
            return Integer.parseInt(num.number.text);
        } else if (node instanceof BinOpNode) {
            BinOpNode binOp = (BinOpNode) node;
            int l = eval(binOp.left);
            int r = eval(binOp.right);
            switch (binOp.op.type) {
            case ADD: return l + r;
            case SUB: return l - r;
            case MUL: return l * r;
            case DIV: return l / r;
            }
        } else if (node instanceof VarNode) {
            VarNode var = (VarNode) node;
            System.out.println("Введите значение " + var.id.text + ":");
            String line = new Scanner(System.in).nextLine();
            return Integer.parseInt(line);
        }
        throw new IllegalStateException();
    }

    public static void main(String[] args) {
        String text = "10 + 20 * (3 + 1)";

        Lexer l = new Lexer(text);
        List<Token> tokens = l.lex();
        tokens.removeIf(t -> t.type == TokenType.SPACE);

        Parser p = new Parser(tokens);
        ExprNode node = p.parseExpression();

        int result = eval(node);
        System.out.println(result);
    }
}
