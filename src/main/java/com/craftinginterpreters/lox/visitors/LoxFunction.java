package com.craftinginterpreters.lox.visitors;

import com.craftinginterpreters.lox.ReturnException;
import com.craftinginterpreters.lox.Stmt;

import java.util.List;

public class LoxFunction implements LoxCallable {
    private final Stmt.Function function;
    private final Environment closure;
    private final FunctionType functionType;

    public LoxFunction(Stmt.Function function, Environment closure, FunctionType functionType) {

        this.function = function;
        this.closure = closure;
        this.functionType = functionType;
    }

    @Override
    public int arity() {
        return  function.getParams().size();
    }

    @Override
    public Object call(Interpreter interpreter, List<Object> arguments) {
        Environment environment = new Environment(closure);
        for (int i = 0; i < function.getParams().size(); i++) {
            environment.declare(
                    function.getParams().get(i),
                    arguments.get(i)
            );
        }
        try {
            interpreter.executeBlock(function.getBody(), environment);
            return null;
        }catch (ReturnException ex) {
            return  ex.getValue();
        }
    }

    public LoxFunction bind(LoxInstance instance)
    {
        Environment e = new Environment(closure);
        e.defineThis(instance);
        return  new LoxFunction(function, e, functionType);
    }
}
