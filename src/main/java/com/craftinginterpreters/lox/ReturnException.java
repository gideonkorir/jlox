package com.craftinginterpreters.lox;

import lombok.Getter;

public class ReturnException extends RuntimeException {
    @Getter
    private final Object value;

    public ReturnException(Object value) {
        super(null, null, false, false);
        this.value = value;
    }
}
