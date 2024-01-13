package com.craftinginterpreters.lox.visitors;

import com.craftinginterpreters.lox.RuntimeError;
import com.craftinginterpreters.lox.Token;

import java.util.Map;
import java.util.HashMap;

public class LoxInstance {
    private final LoxClass klass;

    private final Map<String, Object> fields = new HashMap<>();

    public LoxInstance(LoxClass klass) {
        this.klass = klass;
    }

    public LoxClass getKlass()
    {
        return  klass;
    }

    @Override
    public String toString() {
        return  klass.name + " instance";
    }

    public Object get(Token name)
    {
        if(fields.containsKey(name.getLexeme())) {
            return fields.get(name.getLexeme());
        }
        LoxFunction method = klass.findMethod(name.getLexeme());
        if(method != null)
        {
            return method.bind(this);
        }
        throw new RuntimeError(name, "Undefined property '" + name.getLexeme() + "'.");
    }

    public Object set(Token name, Object value)
    {
        fields.put(name.getLexeme(), value);
        return value;
    }
}
