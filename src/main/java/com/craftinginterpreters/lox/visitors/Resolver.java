package com.craftinginterpreters.lox.visitors;

import com.craftinginterpreters.lox.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

/**
 * We need to visit everywhere a variable is declared, read or written to. We also need to recurse into
 * all structures where a scope is created/destroyed in order to recurse into the subtrees
 */
public class Resolver implements ExprVisitor<Void>, StmtVisitor<Void> {
    private final Interpreter interpreter;
    private final Stack<Map<String, Boolean>> scopes = new Stack<Map<String, Boolean>>();

    private final int[] noContextKeywords = new int[0];

    private int[] contextEnabledOps = noContextKeywords;

    public Resolver(Interpreter interpreter) {
        this.interpreter = interpreter;
    }

    @Override
    public Void visitBlockStmt(Stmt.Block stmt) {
        beginScope();
        resolve(stmt.getStatements());
        endScope();
        return  null;
    }

    @Override
    public Void visitVarStmt(Stmt.Var statement) {
        declare(statement.getName());
        if(statement.getExpression() != null) {
            resolve(statement.getExpression());
        }
        define(statement.getName());
        return  null;
    }

    @Override
    public Void visitVariableExpr(Expr.Variable expr) {
        if(!scopes.isEmpty()){
            Map<String, Boolean> scope = scopes.peek();
            if(scope.get(expr.getName().getLexeme()) == Boolean.FALSE){
                //if we are here, we are declaring a variable that references itself
                //i.e. var a = a
                Lox.report(
                        expr.getName().getLine(),
                        expr.getName().getLexeme(),
                        "Can't read local variable in its own initializer"
                );
            }
        }
        resolveLocal(expr, expr.getName());
        return  null;
    }

    @Override
    public Void visitAssignmentExpr(Expr.Assignment expr) {
        resolve(expr.getExpression());
        resolveLocal(expr, expr.getIdentifier());
        return  null;
    }

    @Override
    public Void visitFunctionStmt(Stmt.Function statement) {
        declare(statement.getName());
        define(statement.getName());
        resolveFunction(statement, statement.getParams(), statement.getBody(), false);
        return  null;
    }

    @Override
    public Void visitAnonymousFunctionExpr(Expr.AnonymousFunction expr) {
        resolveFunction(expr, expr.getParams(), expr.getBody(), true);
        return  null;
    }

    @Override
    public Void visitExpressionStmt(Stmt.Expression statement) {
        resolve(statement.getExpression());
        return  null;
    }

    @Override
    public Void visitIfStmt(Stmt.If statement) {
        resolve(statement.getCondition());
        resolve(statement.getThenStmt());
        if(statement.getElseStmt() != null)
            resolve(statement.getElseStmt());
        return  null;
    }

    @Override
    public Void visitPrintStmt(Stmt.Print statement) {
        resolve(statement.getExpression());
        return  null;
    }

    @Override
    public Void visitReturnStmt(Stmt.Return stmt) {
        if(!ContextualOps.has(contextEnabledOps, ContextualOps.RETURN)) {
            Lox.report(
                    stmt.getKeyword().getLine(),
                    stmt.getKeyword().getLexeme(),
                    "Can't return from top level code; 'return' keyword is only allowed inside a function");
        }
        if(stmt.getExpression() != null)
            resolve(stmt.getExpression());
        return  null;
    }

    @Override
    public Void visitWhileStmt(Stmt.While statement) {
        int[] prev = contextEnabledOps;
        contextEnabledOps = ContextualOps.getLoopContextOps(prev);
        resolve(statement.getCondition());
        resolve(statement.getBody());
        contextEnabledOps = prev;
        return  null;
    }

    @Override
    public Void visitForStmt(Stmt.For statement) {
        int[] prev = contextEnabledOps;
        contextEnabledOps = ContextualOps.getLoopContextOps(prev);
        if(statement.getInitializer() != null) {
            beginScope();
            resolve(statement.getInitializer());
        }
        if(statement.getCondition() != null)
            resolve(statement.getCondition());
        if(statement.getIncrement() != null)
            resolve(statement.getIncrement());
        resolve(statement.getBody());
        contextEnabledOps = prev;
        if(statement.getInitializer() != null) {
            endScope();
        }
        return  null;
    }

    @Override
    public Void visitBinaryExpr(Expr.Binary expr) {
        resolve(expr.getLeft());
        resolve(expr.getRight());
        return  null;
    }

    @Override
    public Void visitCallExpr(Expr.Call expr) {
        resolve(expr.getCallee());
        for(Expr argument : expr.getArguments())
            resolve(argument);
        return  null;
    }

    @Override
    public Void visitGroupingExpr(Expr.Grouping expr) {
        resolve(expr.getExpression());
        return  null;
    }

    @Override
    public Void visitLiteralExpr(Expr.Literal expr) {
        return null;
    }

    @Override
    public Void visitUnaryExpr(Expr.Unary expr) {
        resolve(expr.getOperand());
        return  null;
    }

    @Override
    public Void visitLogicalExpr(Expr.Logical expr) {
        resolve(expr.getLeft());
        resolve(expr.getRight());
        return  null;
    }

    @Override
    public Void visitKeywordStmt(Stmt.Keyword statement) {
        if(statement.getKeyword() == TokenType.BREAK && !ContextualOps.has(contextEnabledOps, ContextualOps.BREAK)) {
            Lox.error("The 'break' keyword is only allowed in the body of a for or while statement.");
        }
        else if(statement.getKeyword() == TokenType.CONTINUE && !ContextualOps.has(contextEnabledOps, ContextualOps.CONTINUE)) {
            Lox.error("The 'continue' keyword is only allowed in the body of a for or while statement.");
        }
        return null;
    }

    public void resolve(List<Stmt> statements) {
        for(Stmt s : statements)
            resolve(s);
    }

    private void resolve(Stmt statement) {
        statement.accept(this);
    }

    private void resolve(Expr expression) {
        expression.accept(this);
    }

    private void resolveLocal(Expr expr, Token name) {
        for(int i=scopes.size() - 1; i >= 0; i--) {
            Map<String, Boolean> scope = scopes.get(i);
            if(scope.containsKey(name.getLexeme())) {
                int index = scopes.size() - 1 - i;
                interpreter.resolve(expr, index);
            }
        }
    }

    private void resolveFunction(Object function, List<Token> params, List<Stmt> body, boolean isAnonymous) {
        int[] prev = contextEnabledOps;
        contextEnabledOps = ContextualOps.getFunctionContextOps();
        beginScope();
        for(Token param : params) {
            declare(param);
            define(param);
        }
        resolve(body);
        endScope();
        contextEnabledOps = prev;
    }

    private void beginScope() {
        scopes.push(new HashMap<>());
    }

    private void endScope() {
        scopes.pop();
    }

    private void declare(Token name) {
        if(scopes.isEmpty())
            return;
        Map<String, Boolean> scope = scopes.peek();
        if(scope.containsKey(name.getLexeme())) {
            Lox.report(
                    name.getLine(),
                    name.getLexeme(),
                    String.format("A variable with the same name '%s' already exists in the current scope", name.getLexeme())
            );
        }
        scope.put(name.getLexeme(), false);
    }

    private void define(Token token) {
        if(scopes.isEmpty())
            return;
        scopes.peek().put(token.getLexeme(), true);
    }

    //Upgraded from FunctionType in the book because I implemented support
    //for break and continue statements. There can be loops inside a function, and we
    //can return from inside a loop. This means we can have multiple contextual operations/keywords
    //enabled at the same time hence the use of an array.

    private static class ContextualOps {
        public static int RETURN = 1;
        public static int BREAK = 2;
        public static int CONTINUE = 3;

        private static final int[] FUNCTION_CONTEXT_OPS = new int[] { RETURN };

        private static final int[] LOOP_IN_FUNCTION = new int[] { RETURN, BREAK, CONTINUE };

        private static final int[] TOP_LEVEL_LOOP = new int[] { BREAK, CONTINUE };

        public static int[] getLoopContextOps(int[] currentOps)
        {
            if(has(currentOps, RETURN))
                return LOOP_IN_FUNCTION;
            return  TOP_LEVEL_LOOP;
        }

        public static int[] getFunctionContextOps() {
            return FUNCTION_CONTEXT_OPS;
        }

        public static boolean has(int[] values, int value)
        {
            for(int v : values)
            {
                if(v == value)
                    return  true;
            }
            return  false;
        }
    }
}
