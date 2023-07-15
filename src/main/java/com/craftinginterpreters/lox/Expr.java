package com.craftinginterpreters.lox;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;

public abstract class Expr {
    
    public abstract ExprType getExprType();

    public abstract <R> R accept(ExprVisitor<R> visitor);

    @Data
    @RequiredArgsConstructor
    @EqualsAndHashCode(callSuper = false)
    public static class Literal extends Expr {
        private final Object value;

        @Override
        public ExprType getExprType() {
            return ExprType.LITERAL;
        }

        @Override
        public <R> R accept(ExprVisitor<R> visitor) {
            return visitor.visitLiteralExpr(this);
        }
    }

    @Data
    @RequiredArgsConstructor
    @EqualsAndHashCode(callSuper = false)
    public static class Unary extends Expr {
        private final Token operator;
        private final Expr operand;

        @Override
        public ExprType getExprType() {
            return ExprType.UNARY;
        }

        @Override
        public <R> R accept(ExprVisitor<R> visitor) {
            return visitor.visitUnaryExpr(this);
        }
    }

    @Data
    @RequiredArgsConstructor
    @EqualsAndHashCode(callSuper = false)
    public static class Binary extends Expr {
        private final Expr left;
        private final Token operator;
        private final Expr right;

        @Override
        public ExprType getExprType() {
            return ExprType.BINARY;
        }

        @Override
        public <R> R accept(ExprVisitor<R> visitor) {
            return visitor.visitBinaryExpr(this);
        }
    }

    @Data
    @RequiredArgsConstructor
    @EqualsAndHashCode(callSuper = false)
    public static class Grouping extends Expr {
        private final Expr expression;

        @Override
        public ExprType getExprType() {
            return ExprType.GROUPING;
        }

        @Override
        public <R> R accept(ExprVisitor<R> visitor) {
            return visitor.visitGroupingExpr(this);
        }
    }

    @Data
    @RequiredArgsConstructor
    @EqualsAndHashCode(callSuper = false)
    public static class Variable extends Expr {
        private final Token name;

        @Override
        public ExprType getExprType() {
            return ExprType.VARIABLE;
        }

        @Override
        public <R> R accept(ExprVisitor<R> visitor) {
            return visitor.visitVariableExpr(this);
        }
    }

    @Data
    @RequiredArgsConstructor
    @EqualsAndHashCode(callSuper = false)
    public  static class Assignment extends  Expr {
        private final Token identifier;
        private final Expr expression;

        @Override
        public ExprType getExprType() {
            return ExprType.ASSIGNMENT;
        }

        @Override
        public <R> R accept(ExprVisitor<R> visitor) {
            return visitor.visitAssignmentExpr(this);
        }
    }

    @Data
    @RequiredArgsConstructor
    @EqualsAndHashCode(callSuper = false)
    public  static class Logical extends  Expr {
        private final Expr left;
        private final TokenType operator;
        private final Expr right;

        @Override
        public ExprType getExprType() {
            return ExprType.LOGICAL;
        }

        @Override
        public <R> R accept(ExprVisitor<R> visitor) {
            return visitor.visitLogicalExpr(this);
        }
    }
}
