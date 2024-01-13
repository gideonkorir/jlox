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

    public Object getAt(int distance, Token token) {
        Environment envToUse = getAncestor(distance);
        //Here the environment is trusting that the resolver class
        //made sure the variable is there!. This is logical coupling :)
        return  envToUse.variables.get(token.getLexeme());
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

    public void assignAt(int distance, Token name, Object value) {
        Environment envToUse = getAncestor(distance);
        envToUse.variables.put(name.getLexeme(), value);
    }

    private Environment getAncestor(int distance) {
        Environment envToUse = this;
        for(int i=0; i<distance; i++){
            envToUse = envToUse._enclosingScope;
        }
        return  envToUse;
    }

    public void define(String identifier, LoxCallable callable) {
        variables.put(identifier, callable);
    }

    public void defineThis(LoxInstance instance)
    {
        variables.put("this", instance);
    }
}
