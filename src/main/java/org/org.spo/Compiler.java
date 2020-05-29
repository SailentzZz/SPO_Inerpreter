package org.org.spo;

import org.spo.ASTree.BinOpNode;
import org.spo.ASTree.ExprNode;
import org.spo.ASTree.NumbNode;
import org.spo.ASTree.VarNode;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Compiler {

    private List<String> instructions = new ArrayList<>();

    private void emit0(String code) {
        instructions.add(code);
    }

    private void emit(String code) {
        instructions.add("    " + code);
    }

    private static void gatherVariables(ExprNode node, Set<String> usedVariables) {
        if (node instanceof BinOpNode) {
            BinOpNode binOp = (BinOpNode) node;
            gatherVariables(binOp.left, usedVariables);
            gatherVariables(binOp.right, usedVariables);
        } else if (node instanceof VarNode) {
            String var = ((VarNode) node).id.text;
            usedVariables.add(var);
        }
    }

    //Код, генерируемый compileExpr, оставляет результат выражения на вершине стека
    private void compileExpr(ExprNode node) {
        if (node instanceof NumbNode) {
            NumbNode num = (NumbNode) node;
            emit("push dword " + num.number.text);
        } else if (node instanceof BinOpNode) {
            BinOpNode binOp = (BinOpNode) node;
            compileExpr(binOp.left);
            compileExpr(binOp.right);
            emit("pop ebx");
            emit("pop eax");
            switch (binOp.op.type) {
            case ADD:
                emit("add eax, ebx");
                break;
            case SUB:
                emit("sub eax, ebx");
                break;
            case MUL:
                emit("imul eax, ebx");
                break;
            case DIV:
                emit("mov edx, 0");
                emit("idiv ebx");
                break;
            }
            emit("push eax");
        } else if (node instanceof VarNode) {
            String var = ((VarNode) node).id.text;
            emit("push dword [" + var + "]");
        } else {
            throw new IllegalStateException("Should not happen!");
        }
    }

    //Генерация 32-битного ассемблера
    private void compile32(ExprNode node) {
        LinkedHashSet<String> usedVariables = new LinkedHashSet<>();
        gatherVariables(node, usedVariables);

        // Заголовок секции кода с объявлением main и printf:
        emit0("section .text");
        emit("global _main");
        emit("extern _printf");
        emit("extern _scanf");

        // Код функции main:
        emit0("_main:");
        for (String variable : usedVariables) {
            emit("push " + variable + "@prompt");
            emit("call _printf");
            emit("pop ebx");

            emit("push " + variable);
            emit("push scanf_format");
            emit("call _scanf");
            emit("pop ebx");
            emit("pop ebx");
        }
        compileExpr(node);
        emit("push message");
        emit("call _printf");
        emit("pop ebx");
        emit("pop ebx");
        emit("ret");

        emit0("");
        emit0("section .rdata");
        // Константа для вызова printf для вывода результата:
        emit0("message: db 'Result is %d', 10, 0");
        // Константа для вызова scanf:
        emit0("scanf_format: db '%d', 0");
        // Константы для вызова printf для приглашения ввода переменных:
        for (String variable : usedVariables) {
            emit0(variable + "@prompt: db 'Input " + variable + ": ', 0");
        }

        emit0("");
        emit0("section .bss");
        for (String variable : usedVariables) {
            emit0(variable + ": resd 1");
        }
    }

    private static Double getConstant(ExprNode node) {
        if (node instanceof NumbNode) {
            NumbNode numbNode = (NumbNode) node;
            return Double.parseDouble(numbNode.number.text);
        } else {
            return null;
        }
    }

    private static boolean isConstant(Double constValue, double value) {
        if (constValue != null) {
            return Double.compare(constValue.doubleValue(), value) == 0;
        } else {
            return false;
        }
    }

    private static ExprNode foldConstants(ExprNode node) {
        if (node instanceof NumbNode) {
            return node;
        } else if (node instanceof VarNode) {
            return node;
        } else if (node instanceof BinOpNode) {
            BinOpNode binOpNode = (BinOpNode) node;
            ExprNode left = foldConstants(binOpNode.left);
            ExprNode right = foldConstants(binOpNode.right);
            TokenType op = binOpNode.op.type;
            Double leftConst = getConstant(left);
            Double rightConst = getConstant(right);
            if (leftConst != null && rightConst != null) {
                double result;
                switch (op) {
                case ADD:
                    result = leftConst.doubleValue() + rightConst.doubleValue();
                    break;
                case SUB:
                    result = leftConst.doubleValue() - rightConst.doubleValue();
                    break;
                case MUL:
                    result = leftConst.doubleValue() * rightConst.doubleValue();
                    break;
                case DIV:
                    result = leftConst.doubleValue() / rightConst.doubleValue();
                    break;
                default:
                    throw new IllegalStateException("Should not happen!");
                }
                return new NumbNode(new Token(TokenType.NUMBER, String.valueOf(result), binOpNode.op.pos));
            } else if (isConstant(leftConst, 0) && op == TokenType.ADD) {
                // 0+x => x
                return right;
            } else if (isConstant(rightConst, 0) && op == TokenType.ADD) {
                // x+0 => x
                return left;
            } else if (isConstant(leftConst, 1) && op == TokenType.MUL) {
                // 1*x => x
                return right;
            } else if (isConstant(rightConst, 1) && op == TokenType.MUL) {
                // x*1 => x
                return left;
            } else if (isConstant(rightConst, 0) && op == TokenType.SUB) {
                // x-0 => x
                return left;
            } else if (isConstant(rightConst, 1) && op == TokenType.DIV) {
                // x/1 => x
                return left;
            } else {
                return new BinOpNode(binOpNode.op, left, right);
            }
        } else {
            throw new IllegalStateException("Should not happen!");
        }
    }

    private void outputWithPeepholeOptimization() {
        Pattern pushPattern = Pattern.compile("\\s*push (.+)");
        Pattern popPattern = Pattern.compile("\\s*pop (.+)");
        for (int i = 0; i < instructions.size(); i++) {
            String instruction = instructions.get(i);
            if (i + 1 < instructions.size()) {
                String nextInstruction = instructions.get(i + 1);
                Matcher pushMatcher = pushPattern.matcher(instruction);
                Matcher popMatcher = popPattern.matcher(nextInstruction);
                if (pushMatcher.matches() && popMatcher.matches()) {
                    String pushed = pushMatcher.group(1);
                    String popped = popMatcher.group(1);
                    System.out.println("    mov " + popped + ", " + pushed);
                    i++; // превратили две инструкции в одну
                    continue;
                }
            }
            System.out.println(instruction);
        }
    }

    public static void main(String[] args) {
        String text = "x + 20 * (3 + y)";

        Lexer l = new Lexer(text);
        List<Token> tokens = l.lex();
        tokens.removeIf(t -> t.type == TokenType.SPACE);

        Parser p = new Parser(tokens);
        ExprNode node = p.parseExpression();
        ExprNode optimizedNode = foldConstants(node);

        Compiler compiler = new Compiler();
        compiler.compile32(optimizedNode);
        compiler.outputWithPeepholeOptimization();
    }
}
