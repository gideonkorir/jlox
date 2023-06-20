package com.craftinginterpreters.lox;

import com.craftinginterpreters.lox.Expr.Binary;
import com.craftinginterpreters.lox.Expr.Grouping;
import com.craftinginterpreters.lox.Expr.Literal;
import com.craftinginterpreters.lox.Expr.Unary;
import com.craftinginterpreters.lox.Expr.Tenary;

public interface Visitor<R> {
    R visitLiteralExpr(Literal expr);

    R visitUnaryExpr(Unary expr);

    R visitBinaryExpr(Binary expr);

    R visitGroupingExpr(Grouping expr);

    R visitTenaryExpr(Tenary expr);
}
