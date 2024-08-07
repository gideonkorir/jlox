package com.craftinginterpreters.lox;

import lombok.*;

import java.util.List;

public abstract class Stmt {

    public abstract <T> T accept(StmtVisitor<T> visitor);
    @Data
    @RequiredArgsConstructor
    @EqualsAndHashCode(callSuper = false)
    public static class Expression extends Stmt {
        @Getter
        private final Expr expression;

        @Override
        public <T> T accept(StmtVisitor<T> visitor) {
            return visitor.visitExpressionStmt(this);
        }
    }

    @Data
    @RequiredArgsConstructor
    @EqualsAndHashCode(callSuper = false)
    public static class Print extends Stmt {
        @Getter
        private final Expr expression;

        @Override
        public <T> T accept(StmtVisitor<T> visitor) {
            return visitor.visitPrintStmt(this);
        }
    }

    @Data
    @RequiredArgsConstructor
    @EqualsAndHashCode(callSuper = false)
    public static class Var extends Stmt {
        @Getter
        private final Token name;
        @Getter
        private final Expr expression;

        @Override
        public <T> T accept(StmtVisitor<T> visitor) {
            return visitor.visitVarStmt(this);
        }
    }

    @Data
    @RequiredArgsConstructor
    @EqualsAndHashCode(callSuper = false)
    public static class Block extends Stmt {
        @Getter
        private final List<Stmt> statements;

        @Override
        public <T> T accept(StmtVisitor<T> visitor) {
            return visitor.visitBlockStmt(this);
        }
    }

    @Data
    @RequiredArgsConstructor
    @EqualsAndHashCode(callSuper = false)
    public static class If extends Stmt {
        @Getter
        private final Expr condition;
        @Getter
        private final Stmt thenStmt;
        @Getter
        private final Stmt elseStmt;

        @Override
        public <T> T accept(StmtVisitor<T> visitor) {
            return visitor.visitIfStmt(this);
        }
    }

    @Data
    @RequiredArgsConstructor
    @EqualsAndHashCode(callSuper = false)
    public static class While extends  Stmt {
        private final Expr condition;
        private final Stmt body;

        @Override
        public <T> T accept(StmtVisitor<T> visitor) {
            return  visitor.visitWhileStmt(this);
        }
    }

    @Data
    @RequiredArgsConstructor
    @EqualsAndHashCode(callSuper = false)
    public static class Keyword extends  Stmt {
        private final TokenType keyword;

        @Override
        public <T> T accept(StmtVisitor<T> visitor) {
            return  visitor.visitKeywordStmt(this);
        }
    }

    @Data
    @RequiredArgsConstructor
    @EqualsAndHashCode(callSuper = false)
    public static class For extends  Stmt {
        private final Stmt initializer;
        private final Expr condition;
        private final Expr increment;
        private final Stmt body;

        @Override
        public <T> T accept(StmtVisitor<T> visitor) {
            return  visitor.visitForStmt(this);
        }
    }

    @Data
    @RequiredArgsConstructor
    @EqualsAndHashCode(callSuper = false)
    public static class Function extends Stmt {

        private final Token name;
        private final List<Token> params;
        private final List<Stmt> body;
        private final boolean isStatic;
        @Override
        public <T> T accept(StmtVisitor<T> visitor) {
            return visitor.visitFunctionStmt(this);
        }
    }

    @Data
    @RequiredArgsConstructor
    @EqualsAndHashCode(callSuper = false)
    public static class Return extends Stmt {

        private final Token keyword;
        private final Expr expression;

        @Override
        public <T> T accept(StmtVisitor<T> visitor) {
            return visitor.visitReturnStmt(this);
        }
    }

    @Data
    @RequiredArgsConstructor
    @EqualsAndHashCode(callSuper = false)
    public static class Class extends Stmt {
        private final Token name;
        private final List<Function> methods;

        @Override
        public <T> T accept(StmtVisitor<T> visitor) {
            return visitor.visitClassStmt(this);
        }
    }

    @Data
    @RequiredArgsConstructor
    @EqualsAndHashCode(callSuper = false)
    public static class Property extends Stmt {

        private final Token name;
        private final List<Stmt> getter;
        private final List<Stmt> setter;
        private final boolean isStatic;
        @Override
        public <T> T accept(StmtVisitor<T> visitor) {
            throw new RuntimeException("Not implemented");
        }
    }
}
