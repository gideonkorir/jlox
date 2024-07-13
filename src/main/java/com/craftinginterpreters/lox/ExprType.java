package com.craftinginterpreters.lox;

public enum ExprType {
    LITERAL,
    UNARY,
    BINARY,
    GROUPING,
    VARIABLE,
    ASSIGNMENT,
    LOGICAL,
    CALL,
    ANONYMOUS_FUNCTION,
    GET,
    SET,
    THIS
}
