package com.craftinginterpreters.lox.visitors;

import java.util.List;
import java.util.Map;

public class LoxClass implements LoxCallable{
    final String name;
    final Map<String, LoxFunction> functions;
    final LoxFunction init;

    public LoxClass(String name, Map<String, LoxFunction> functions)
    {
        this.name = name;
        this.functions = functions;
        init = (LoxFunction) functions.getOrDefault("init", null);
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public int arity() {
        return init == null ? 0 : init.arity();
    }

    @Override
    public Object call(Interpreter interpreter, List<Object> arguments) {
        LoxInstance instance = new LoxInstance(this);
        if(init != null) {
            LoxFunction ctor = init.bind(instance);
            ctor.call(interpreter, arguments);
        }
        return  instance;
    }

    public LoxFunction findMethod(String name) {
        if (functions.containsKey(name)) {
            return(LoxFunction) functions.get(name);
        }

        return null;
    }
}
