package com.craftinginterpreters.lox;

import com.craftinginterpreters.lox.Expr.Binary;
import com.craftinginterpreters.lox.Expr.Grouping;
import com.craftinginterpreters.lox.Expr.Literal;
import com.craftinginterpreters.lox.Expr.Unary;

public interface ExprVisitor<R> {
    R visitLiteralExpr(Literal expr);

    R visitUnaryExpr(Unary expr);

    R visitBinaryExpr(Binary expr);

    R visitGroupingExpr(Grouping expr);
    R visitVariableExpr(Expr.Variable expr);

    R visitAssignmentExpr(Expr.Assignment expr);

    R visitLogicalExpr(Expr.Logical expr);

    R visitCallExpr(Expr.Call expr);

    R visitAnonymousFunctionExpr(Expr.AnonymousFunction expr);

    R visitGetExpr(Expr.Get expr);
    R visitSetExpr(Expr.Set expr);

    R visitThisExpr(Expr.This expr);
}
