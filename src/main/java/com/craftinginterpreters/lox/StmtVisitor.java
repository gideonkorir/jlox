package com.craftinginterpreters.lox;

public interface StmtVisitor<T> {
    T visitPrintStmt(Stmt.Print statement);
    T visitExpressionStmt(Stmt.Expression statement);
    T visitVarStmt(Stmt.Var statement);
    T visitBlockStmt(Stmt.Block statement);
    T visitIfStmt(Stmt.If statement);
}
