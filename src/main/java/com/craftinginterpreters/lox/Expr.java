package com.craftinginterpreters.lox;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;

public abstract class Expr {
    
    public abstract ExprType getExprType();

    public abstract <R> R accept(Visitor<R> visitor);

    @Data
    @RequiredArgsConstructor
    @EqualsAndHashCode(callSuper = false)
    public static class LiteralExpr extends Expr {
        private final Object value;

        @Override
        public ExprType getExprType() {
            return ExprType.LITERAL;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitLiteralExpr(this);
        }
    }

    @Data
    @RequiredArgsConstructor
    @EqualsAndHashCode(callSuper = false)
    public static class UnaryExpr extends Expr {
        private final Token operator;
        private final Expr operand;

        @Override
        public ExprType getExprType() {
            return ExprType.UNARY;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitUnaryExpr(this);
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
        public ExprType getExprType() {
            return ExprType.BINARY;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitBinaryExpr(this);
        }
    }

    @Data
    @RequiredArgsConstructor
    @EqualsAndHashCode(callSuper = false)
    public static class GroupingExpr extends Expr {
        private final Expr expression;

        @Override
        public ExprType getExprType() {
            return ExprType.GROUPING;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitGroupingExpr(this);
        }
    }
}
