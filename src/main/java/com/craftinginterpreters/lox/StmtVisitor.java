package com.craftinginterpreters.lox;

public interface StmtVisitor<T> {
    T visitPrintStmt(Stmt.Print statement);
    T visitExpressionStmt(Stmt.Expression statement);
    T visitVarStmt(Stmt.Var statement);
    T visitBlockStmt(Stmt.Block statement);
    T visitIfStmt(Stmt.If statement);
    T visitWhileStmt(Stmt.While statement);
    T visitForStmt(Stmt.For statement);
    T visitKeywordStmt(Stmt.Keyword statement);
    T visitFunctionStmt (Stmt.Function statement);
    T visitReturnStmt(Stmt.Return stmt);
    T visitClassStmt(Stmt.Class stmt);
}
