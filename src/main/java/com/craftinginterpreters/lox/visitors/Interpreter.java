package com.craftinginterpreters.lox.visitors;

import com.craftinginterpreters.lox.*;

import java.util.List;
import java.util.Objects;

public class Interpreter implements ExprVisitor<Object>, StmtVisitor<Void> {

    private Environment environment = new Environment();
    public void interpret(List<Stmt> statements) {
        try {
            for (Stmt statement : statements) {
                execute(statement);
            }
        } catch (RuntimeError error) {
            Lox.runtimeError(error);
        }
    }

    void execute(Stmt stmt){
        stmt.accept(this);
    }

    @Override
    public Void visitPrintStmt(Stmt.Print stmt) {
        Object value = evaluate(stmt.getExpression());
        System.out.println(stringify(value));
        return null;
    }
    @Override
    public Void visitExpressionStmt(Stmt.Expression stmt) {
        evaluate(stmt.getExpression());
        return null;
    }

    @Override
    public Void visitVarStmt(Stmt.Var statement) {
        Expr expr  = statement.getExpression();
        Object value = expr == null
                ? UnassignedValue.Value
                : evaluate(expr);
        environment.declare(statement.getName(), value);
        return  null;
    }

    @Override
    public Void visitBlockStmt(Stmt.Block stmt) {
        executeBlock(stmt.getStatements(), new Environment(environment));
        return null;
    }

    public Void visitIfStmt(Stmt.If stmt){
        Object cond = evaluate(stmt.getCondition());
        if(isTruthy(cond)){
            execute(stmt.getThenStmt());
        } else if(stmt.getElseStmt() != null) {
            execute(stmt.getElseStmt());
        }
        return  null;
    }

    private void executeBlock(List<Stmt> statements,
                              Environment environment) {
        Environment previous = this.environment;
        try {
            this.environment = environment;

            for (Stmt statement : statements) {
                execute(statement);
            }
        } finally {
            this.environment = previous;
        }
    }

    public void interpret(Expr expression) {
        try {
            Object value = evaluate(expression);
            System.out.println(stringify(value));
        } catch (RuntimeError error) {
            Lox.runtimeError(error);
        }
    }
    private Object evaluate(Expr expr) {
        return expr.accept(this);
    }

    @Override
    public Object visitGroupingExpr(Expr.Grouping expr) {
        return evaluate(expr.getExpression());
    }

    @Override
    public Object visitVariableExpr(Expr.Variable expr) {
        return  environment.get(expr.getName());
    }

    @Override
    public Object visitLiteralExpr(Expr.Literal expr) {
        return expr.getValue();
    }

    @Override
    public Object visitUnaryExpr(Expr.Unary expr) {
        Object right = evaluate(expr.getOperand());
        if (Objects.requireNonNull(expr.getOperator().getType()) == TokenType.MINUS) {
            checkNumberOperand(expr.getOperator(), right);
            return -((double)right);
        }
        else if(expr.getOperator().getType() == TokenType.BANG){
            return !isTruthy(right);
        }
        return null;
    }

    private static boolean isTruthy(Object object) {
        if (object == null) return false;
        if (object instanceof Boolean) return (boolean)object;
        return true;
    }

    @Override
    public Object visitBinaryExpr(Expr.Binary expr) {
        Object left = evaluate(expr.getLeft());
        Object right = evaluate(expr.getRight());
        TokenType type = expr.getOperator().getType();
        if(type == TokenType.EQUAL_EQUAL){
            return isEqual(left, right);
        }
        if(type == TokenType.BANG_EQUAL) {
            return !isEqual(left, right);
        }
        boolean isMath = left instanceof Double && right instanceof Double;
        if(isMath)
        {
            double l = (double)left;
            double r = (double)right;
            switch (type)
            {
                case PLUS:
                    return l + r;
                case MINUS:
                    return  l - r;
                case STAR:
                    return l * r;
                case SLASH:
                    return l / r;
                case LESS:
                    return l < r;
                case LESS_EQUAL:
                    return l <= r;
                case GREATER:
                    return l > r;
                case GREATER_EQUAL:
                    return l >= r;
                default:
                    return  null;
            }
        }

        boolean isStrings = left instanceof String && right instanceof String;
        if(isStrings && type == TokenType.PLUS)
        {
            return ((String)left) + ((String)right);
        }

        throw new RuntimeError(expr.getOperator(),
                "Operation '" + expr.getOperator().getLexeme() + "' is not defined for values '"
         + stringify(left) + "' and '" + stringify(right) + "'");
    }

    @Override
    public Object visitAssignmentExpr(Expr.Assignment expr) {
        Object value = evaluate(expr.getExpression());
        environment.assign(expr.getIdentifier(), value);
        return value;
    }

    private static boolean isEqual(Object a, Object b) {
        if (a == null && b == null) return true;
        if (a == null) return false;

        return a.equals(b);
    }

    private void checkNumberOperand(Token operator, Object operand) {
        if (operand instanceof Double) return;
        throw new RuntimeError(operator, "Operand must be a number.");
    }

    private static String stringify(Object object) {
        if (object == null) return "nil";

        if (object instanceof Double) {
            String text = object.toString();
            if (text.endsWith(".0")) {
                text = text.substring(0, text.length() - 2);
            }
            return text;
        }

        return object.toString();
    }
}
