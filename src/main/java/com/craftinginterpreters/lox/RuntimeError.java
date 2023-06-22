package com.craftinginterpreters.lox;

import com.craftinginterpreters.lox.Token;
import lombok.Data;
import lombok.Getter;
public class RuntimeError extends RuntimeException {
    @Getter
    final Token token;

    public RuntimeError(Token token, String message) {
        super(message);
        this.token = token;
    }
}