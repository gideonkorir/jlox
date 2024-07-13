package com.craftinginterpreters.lox.visitors;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
public class LoxClassFunction extends LoxClassMember implements LoxCallable {

    @Getter
    private final String name;

    @Getter
    private final LoxFunction function;

    @Override
    public int arity() {
        return function.arity();
    }

    @Override
    public Object call(Interpreter interpreter, List<Object> arguments) {
        return function.call(interpreter, arguments);
    }
}
