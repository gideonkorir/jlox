package com.craftinginterpreters.lox.visitors;

import com.craftinginterpreters.lox.Expr.Binary;
import com.craftinginterpreters.lox.Expr.Grouping;
import com.craftinginterpreters.lox.Expr.Literal;
import com.craftinginterpreters.lox.Expr.Unary;
import com.craftinginterpreters.lox.Expr;
import com.craftinginterpreters.lox.ExprVisitor;
import com.craftinginterpreters.lox.Stmt;
import com.craftinginterpreters.lox.StmtVisitor;

public class AstPrinter implements ExprVisitor<String>, StmtVisitor<String> {

    public String print(Expr expr) {
        return expr.accept(this);
    }

    public String print(Stmt stmt) {
        return stmt.accept(this);
    }

    @Override
    public String visitExpressionStmt(Stmt.Expression statement) {
        return print(statement.getExpression());
    }

    @Override
    public String visitVarStmt(Stmt.Var statement) {
        String val = statement.getExpression() == null ? "null" : print(statement.getExpression());
        return  String.format("declare %s = %s", statement.getName().getLexeme(), val);
    }

    @Override
    public String visitPrintStmt(Stmt.Print statement) {
        return  "print: " + print(statement.getExpression());
    }

    @Override
    public String visitLiteralExpr(Literal expr) {
        if(expr.getValue() == null) {
            return "nil";
        }
        return parenthesize(expr.getValue().toString());
    }

    @Override
    public String visitUnaryExpr(Unary expr) {
        return parenthesize(expr.getOperator().getLexeme(), expr.getOperand());
    }

    @Override
    public String visitBlockStmt(Stmt.Block statement) {
        StringBuilder builder = new StringBuilder();
        builder.append("{").append(System.lineSeparator());
        for(Stmt stmt : statement.getStatements()){
            builder.append(print(stmt));
            builder.append(System.lineSeparator());
        }
        builder.append("}");
        return  builder.toString();
    }

    @Override
    public String visitIfStmt(Stmt.If stmt) {
        if(stmt.getElseStmt() != null) {
            return  String.format("if (%s)  %s else %s",
                    print(stmt.getCondition()),
                    print(stmt.getThenStmt()),
                    print(stmt.getElseStmt())
            );
        } else {
            return  String.format("if (%s)  %s",
                    print(stmt.getCondition()),
                    print(stmt.getThenStmt())
            );
        }
    }

    @Override
    public String visitWhileStmt(Stmt.While statement) {
        String body = statement.getBody().accept(this);
        return  String.format("while (%s) %s", statement.getCondition(), body);
    }

    @Override
    public String visitKeywordStmt(Stmt.Keyword statement) {
        return statement.getKeyword().name();
    }

    @Override
    public String visitBinaryExpr(Binary expr) {
        return parenthesize(expr.getOperator().getLexeme(), expr.getLeft(), expr.getRight());
    }

    @Override
    public String visitGroupingExpr(Grouping expr) {
        return parenthesize("group", expr.getExpression());
    }

    @Override
    public String visitVariableExpr(Expr.Variable expr) {
        return String.format("variable %s", expr.getName().getLexeme());
    }

    @Override
    public String visitAssignmentExpr(Expr.Assignment expr) {
        Expr value = expr.getExpression();
        String v = value == null ? null : value.accept(this);
        return String.format("%s = %s", expr.getIdentifier().getLexeme(), v);
    }

    @Override
    public String visitLogicalExpr(Expr.Logical expr) {
        return  parenthesize(
                expr.getOperator().toString(),
                expr.getLeft(),
                expr.getRight()
                );
    }

    private String parenthesize(String name, Expr... exprs) {
        StringBuilder builder = new StringBuilder();
    
        builder.append("(").append(name);
        for (Expr expr : exprs) {
          builder.append(" ");
          builder.append(expr.accept(this));
        }
        builder.append(")");
    
        return builder.toString();
    }

}