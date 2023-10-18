package com.craftinginterpreters.lox.visitors;

import com.craftinginterpreters.lox.RuntimeError;
import com.craftinginterpreters.lox.Token;
import com.craftinginterpreters.lox.UnassignedValue;

import java.util.HashMap;

public class Environment {

    private final Environment _enclosingScope;
    private final HashMap<String, Object> variables = new HashMap<>();

    public Environment(){
        this(null);
    }

    public Environment(Environment enclosingScope){
        _enclosingScope = enclosingScope;
    }

    public void declare(Token key, Object value) {
        variables.put(key.getLexeme(), value);
    }

    public Object get(Token name) {
        Object value = null;
        if(variables.containsKey(name.getLexeme())){
            value =  variables.get(name.getLexeme());
        } else if(_enclosingScope != null){
            value = _enclosingScope.get(name);
        } else {
            //variable hasn't been declared
            throw new RuntimeError(
                    name,
                    String.format("Invalid access of undeclared variable '%s'", name.getLexeme())
            );
        }

        if(value == UnassignedValue.Value) {
            throw new RuntimeError(
                    name,
                    String.format("Access of unassigned variable '%s'", name.getLexeme())
            );
        }
        return  value;
    }

    public void assign(Token name, Object value) {
        if(!variables.containsKey(name.getLexeme())) {
            if(_enclosingScope != null) {
                _enclosingScope.assign(name, value);
            } else {
                throw new RuntimeError(name,
                        String.format("Can not assign to undeclared variable %s", name.getLexeme())
                );
            }
        } else {
            variables.replace(name.getLexeme(), value);
        }
    }

    public void define(String identifier, LoxCallable callable) {
        variables.put(identifier, callable);
    }
}
