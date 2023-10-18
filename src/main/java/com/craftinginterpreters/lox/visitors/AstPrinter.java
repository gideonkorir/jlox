package com.craftinginterpreters.lox.visitors;

import com.craftinginterpreters.lox.*;
import com.craftinginterpreters.lox.Expr.Binary;
import com.craftinginterpreters.lox.Expr.Grouping;
import com.craftinginterpreters.lox.Expr.Literal;
import com.craftinginterpreters.lox.Expr.Unary;

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
    public String visitForStmt(Stmt.For statement) {
        String init = statement.getInitializer() == null
                ? ""
                : statement.getInitializer().accept(this);
        String cond = statement.getCondition() == null
                ? ""
                : statement.getCondition().accept(this);
        String incr = statement.getIncrement() == null
                ? ""
                : statement.getIncrement().accept(this);
        String body = statement.getBody().accept(this);
        return String.format("for(%s; %s; %s){ %s }",
                init,
                cond,
                incr,
                body
        );
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

    @Override
    public String visitAnonymousFunctionExpr(Expr.AnonymousFunction expr) {
        StringBuilder builder = new StringBuilder();
        builder.append("<anon fn>(");
        boolean started = false;
        for (Token t : expr.getParams())
        {
            if(started) { builder.append(", "); }
            builder.append(t.getLexeme());
            if(!started) { started = true; }
        }
        builder.append(")");
        return  builder.toString();
    }

    @Override
    public String visitFunctionStmt(Stmt.Function statement) {
        StringBuilder builder = new StringBuilder();
        builder.append("fun ")
                .append(statement.getName().getLexeme())
                .append("(");
        int length = statement.getParams().size();
        for(int i = 0; i< length; i++){
            Token param = statement.getParams().get(i);
            builder.append(param.getLexeme());
            if(i < length - 1){
                builder.append(", ");
            }
        }
        builder.append(") { ").append(System.lineSeparator());
        for (Stmt stmt: statement.getBody()) {
            builder.append("\t");
            builder.append(stmt.accept(this));
            builder.append(System.lineSeparator());
        }
        builder.append("}");
        return builder.toString();
    }

    @Override
    public String visitReturnStmt(Stmt.Return stmt) {
        if(stmt.getExpression() != null) {
            return  "return " + stmt.getExpression().accept(this) + ";";
        } else {
            return  "return;";
        }
    }

    @Override
    public String visitCallExpr(Expr.Call expr) {
        StringBuilder builder = new StringBuilder();
        builder.append(expr.getCallee().accept(this))
                .append('(');
        for(int i=0; i<expr.getArguments().size(); i++) {
            Expr arg = expr.getArguments().get(i);
            builder.append(arg.accept(this));
            if(i < expr.getArguments().size() - 1){
                builder.append(", ");
            }
        }
        builder.append(")");
        return  builder.toString();
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