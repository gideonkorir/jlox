package com.craftinginterpreters.lox.visitors;

import com.craftinginterpreters.lox.Expr.BinaryExpr;
import com.craftinginterpreters.lox.Expr.GroupingExpr;
import com.craftinginterpreters.lox.Expr.LiteralExpr;
import com.craftinginterpreters.lox.Expr.UnaryExpr;
import com.craftinginterpreters.lox.Expr;
import com.craftinginterpreters.lox.Visitor;

public class AstPrinter implements Visitor<String> {

    public String print(Expr expr) {
        return expr.accept(this);
    }

    @Override
    public String visitLiteralExpr(LiteralExpr expr) {
        if(expr.getValue() == null) {
            return "nil";
        }
        return parenthesize(expr.getValue().toString());
    }

    @Override
    public String visitUnaryExpr(UnaryExpr expr) {
        return parenthesize(expr.getOperator().getLexeme(), expr.getOperand());
    }

    @Override
    public String visitBinaryExpr(BinaryExpr expr) {
        return parenthesize(expr.getOperator().getLexeme(), expr.getLeft(), expr.getRight());
    }

    @Override
    public String visitGroupingExpr(GroupingExpr expr) {
        return parenthesize("group", expr.getExpression());
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