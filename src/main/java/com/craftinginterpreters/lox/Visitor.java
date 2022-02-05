package com.craftinginterpreters.lox;

import com.craftinginterpreters.lox.Expr.BinaryExpr;
import com.craftinginterpreters.lox.Expr.GroupingExpr;
import com.craftinginterpreters.lox.Expr.LiteralExpr;
import com.craftinginterpreters.lox.Expr.UnaryExpr;

public interface Visitor<R> {
    R visitLiteralExpr(LiteralExpr expr);

    R visitUnaryExpr(UnaryExpr expr);

    R visitBinaryExpr(BinaryExpr expr);

    R visitGroupingExpr(GroupingExpr expr);
}
