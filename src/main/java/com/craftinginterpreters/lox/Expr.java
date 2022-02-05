package com.craftinginterpreters.lox;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;

public abstract class Expr {
    
    public abstract ExprType gExprType();

    @Data
    @RequiredArgsConstructor
    @EqualsAndHashCode(callSuper = false)
    public static class LiteralExpr extends Expr {
        private final Object value;

        @Override
        public ExprType gExprType() {
            return ExprType.LITERAL;
        }
    }

    @Data
    @RequiredArgsConstructor
    @EqualsAndHashCode(callSuper = false)
    public static class UnaryExpr extends Expr {
        private final Token operator;
        private final Expr right;
        @Override
        public ExprType gExprType() {
            return ExprType.UNARY;
        }
    }

    @Data
    @RequiredArgsConstructor
    @EqualsAndHashCode(callSuper = false)
    public static class BinaryExpr extends Expr {
        private final Expr left;
        private final Token operator;
        private final Expr right;
        @Override
        public ExprType gExprType() {
            return ExprType.BINARY;
        }
    }

    @Data
    @RequiredArgsConstructor
    @EqualsAndHashCode(callSuper = false)
    public static class GroupingExpr extends Expr {
        private final Expr expression;

        @Override
        public ExprType gExprType() {
            return ExprType.GROUPING;
        }
    }
}
