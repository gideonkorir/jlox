package com.craftinginterpreters.lox.visitors;

import com.craftinginterpreters.lox.*;

import java.util.*;

public class Interpreter implements ExprVisitor<Object>, StmtVisitor<Void> {
    private Environment environment = new Environment();
    private final Environment globals = environment;

    private final Map<Expr, Integer> locals = new HashMap<>();

    public Interpreter(){
        globals.define("clock", new LoxCallable() {
            @Override
            public int arity() {
                return 0;
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                return (double)System.currentTimeMillis() / 1000.0;
            }

            @Override
            public String toString() { return "<native fn>"; }
        });
    }
    private LoopState loopState = LoopState.None;

    public Environment getGlobals() {
        return  globals;
    }
    public void interpret(List<Stmt> statements) {
        try {
            for (Stmt statement : statements) {
                execute(statement);
            }
        } catch (RuntimeError error) {
            Lox.runtimeError(error);
        }
    }

    void execute(Stmt stmt){
        stmt.accept(this);
    }

    @Override
    public Void visitPrintStmt(Stmt.Print stmt) {
        Object value = evaluate(stmt.getExpression());
        System.out.println(stringify(value));
        return null;
    }
    @Override
    public Void visitExpressionStmt(Stmt.Expression stmt) {
        evaluate(stmt.getExpression());
        return null;
    }

    @Override
    public Void visitVarStmt(Stmt.Var statement) {
        Expr expr  = statement.getExpression();
        Object value = expr == null
                ? UnassignedValue.Value
                : evaluate(expr);
        environment.declare(statement.getName(), value);
        return  null;
    }

    @Override
    public Void visitBlockStmt(Stmt.Block stmt) {
        executeBlock(stmt.getStatements(), new Environment(environment));
        return null;
    }

    public Void visitIfStmt(Stmt.If stmt){
        Object cond = evaluate(stmt.getCondition());
        if(isTruthy(cond)){
            execute(stmt.getThenStmt());
        } else if(stmt.getElseStmt() != null) {
            execute(stmt.getElseStmt());
        }
        return  null;
    }

    void executeBlock(List<Stmt> statements,
                              Environment environment) {
        Environment previous = this.environment;
        try {
            this.environment = environment;
            for (Stmt statement : statements) {
                execute(statement);
                if(loopState != LoopState.None) {
                    break;
                }
            }
        } finally {
            this.environment = previous;
        }
    }

    public void interpret(Expr expression) {
        try {
            Object value = evaluate(expression);
            System.out.println(stringify(value));
        } catch (RuntimeError error) {
            Lox.runtimeError(error);
        }
    }
    private Object evaluate(Expr expr) {
        return expr.accept(this);
    }

    @Override
    public Object visitGroupingExpr(Expr.Grouping expr) {
        return evaluate(expr.getExpression());
    }

    @Override
    public Object visitVariableExpr(Expr.Variable expr) {
        return  lookupVariable(expr.getName(), expr);
    }

    @Override
    public Object visitLiteralExpr(Expr.Literal expr) {
        return expr.getValue();
    }

    @Override
    public Object visitUnaryExpr(Expr.Unary expr) {
        Object right = evaluate(expr.getOperand());
        if (Objects.requireNonNull(expr.getOperator().getType()) == TokenType.MINUS) {
            checkNumberOperand(expr.getOperator(), right);
            return -((double)right);
        }
        else if(expr.getOperator().getType() == TokenType.BANG){
            return !isTruthy(right);
        }
        return null;
    }

    private static boolean isTruthy(Object object) {;if (object == null) return false;
        if (object instanceof Boolean) return (boolean)object;
        return true;
    }

    @Override
    public Object visitBinaryExpr(Expr.Binary expr) {
        Object left = evaluate(expr.getLeft());
        Object right = evaluate(expr.getRight());
        TokenType type = expr.getOperator().getType();
        if(type == TokenType.EQUAL_EQUAL){
            return isEqual(left, right);
        }
        if(type == TokenType.BANG_EQUAL) {
            return !isEqual(left, right);
        }
        boolean isMath = left instanceof Double && right instanceof Double;
        if(isMath)
        {
            double l = (double)left;
            double r = (double)right;
            switch (type)
            {
                case PLUS:
                    return l + r;
                case MINUS:
                    return  l - r;
                case STAR:
                    return l * r;
                case SLASH:
                    return l / r;
                case PERCENT:
                    return l % r;
                case LESS:
                    return l < r;
                case LESS_EQUAL:
                    return l <= r;
                case GREATER:
                    return l > r;
                case GREATER_EQUAL:
                    return l >= r;
                default:
                    return  null;
            }
        }

        boolean isConcat = left instanceof String;
        if(isConcat && type == TokenType.PLUS)
        {
            return ((String)left) + right.toString();
        }

        throw new RuntimeError(expr.getOperator(),
                "Operation '" + expr.getOperator().getLexeme() + "' is not defined for values '"
         + stringify(left) + "' and '" + stringify(right) + "'");
    }

    @Override
    public Object visitAssignmentExpr(Expr.Assignment expr) {
        Object value = evaluate(expr.getExpression());
        Integer distance = locals.get(expr);
        if(distance != null) {
            environment.assignAt(distance, expr.getIdentifier(), value);
        } else {
            globals.assign(expr.getIdentifier(), value);
        }
        environment.assign(expr.getIdentifier(), value);
        return value;
    }

    @Override
    public Object visitLogicalExpr(Expr.Logical expr) {
        Object value = evaluate(expr.getLeft());
        if(isTruthy(value)) {
            if(expr.getOperator() == TokenType.OR) {
                return  true; //short circuit the OR operator
            } else {
                return  isTruthy(evaluate(expr.getRight()));
            }
        } else {
            if(expr.getOperator() == TokenType.OR) {
                return  isTruthy(evaluate(expr.getRight())); //evaluate right of OR
            } else {
                return  false;
            }
        }
    }

    @Override
    public Object visitGetExpr(Expr.Get expr) {
        Object object = evaluate(expr.getOperand());
        if(object instanceof LoxInstance){
            LoxInstance loxInstance = (LoxInstance) object;
            return loxInstance.get(expr.getMember());
        }
        throw new RuntimeError(expr.getMember(), "Only instances have properties.");
    }

    @Override
    public Object visitSetExpr(Expr.Set expr) {
        Object object = evaluate(expr.getOperand());
        if(object instanceof LoxInstance){
            LoxInstance loxInstance = (LoxInstance) object;
            Object value = evaluate(expr.getValue());
            return loxInstance.set(expr.getMember(), value);
        }
        throw new RuntimeError(expr.getMember(), "Only instances have properties.");
    }

    @Override
    public Object visitThisExpr(Expr.This expr) {
        return lookupVariable(expr.getKeyword(), expr);
    }

    @Override
    public Void visitWhileStmt(Stmt.While statement) {
        return  Loop(
                null,
                statement.getCondition(),
                null,
                statement.getBody()
        );
    }

    @Override
    public Void visitForStmt(Stmt.For statement) {
        return  Loop(
                statement.getInitializer(),
                statement.getCondition() == null ? new Expr.Literal(true) : statement.getCondition(),
                statement.getIncrement(),
                statement.getBody()
                );
    }

    Void Loop(Stmt initializer, Expr condition, Expr increment, Stmt body){
        Environment prevScope = this.environment;
        try {
            if (initializer != null) {
                this.environment = new Environment(prevScope);
                execute(initializer);
            }

            Object result = evaluate(condition);

            while (isTruthy(result)) {
                execute(body);
                LoopState current = loopState;
                loopState = LoopState.None;
                if (current == LoopState.Break) {
                    break;
                }
                if (increment != null) {
                    evaluate(increment);
                }
                result = evaluate(condition);
            }
            return null;
        } finally {
            this.environment = prevScope;
        }
    }

    @Override
    public Void visitKeywordStmt(Stmt.Keyword statement) {
        if(statement.getKeyword() == TokenType.BREAK) {
            loopState = LoopState.Break;
        } else if(statement.getKeyword() == TokenType.CONTINUE) {
            loopState = LoopState.Continue;
        }
        return null;
    }

    @Override
    public Object visitCallExpr(Expr.Call expr) {
        Object callee = evaluate(expr.getCallee());
        List<Object> args = new ArrayList<>(expr.getArguments().size());
        for (Expr arg : expr.getArguments()) {
            args.add(evaluate(arg));
        }
        if (!(callee instanceof LoxCallable)) {
            throw new RuntimeError(expr.getParen(),
                    "Can only call functions and classes.");
        }
        LoxCallable function = (LoxCallable) callee;
        if (args.size() != function.arity()) {
            throw new RuntimeError(expr.getParen(), "Expected " +
                    function.arity() + " arguments but got " +
                    args.size() + ".");
        }
        return  function.call(this, args);
    }

    @Override
    public Void visitClassStmt(Stmt.Class stmt) {
        environment.define(stmt.getName().getLexeme(), null);
        Map<String, LoxFunction> methods = new HashMap<>();
        for(Stmt.Function method : stmt.getMethods())
        {
            FunctionType functionType = method.getName().getLexeme().equals("init")
                    ? FunctionType.INITIALIZER
                    : FunctionType.METHOD;

            LoxFunction fn = new LoxFunction(method, environment, functionType);
            methods.put(method.getName().getLexeme(), fn);
        }
        LoxClass klass = new LoxClass(stmt.getName().getLexeme(), methods);
        environment.assign(stmt.getName(), klass);
        return  null;
    }

    @Override
    public Void visitFunctionStmt(Stmt.Function statement) {
        environment.define(
                statement.getName().getLexeme(),
                new LoxFunction(statement, environment, FunctionType.NAMED)
                );
        return  null;
    }

    @Override
    public Object visitAnonymousFunctionExpr(Expr.AnonymousFunction expr) {
        //Create fake name
        Token name = new Token(TokenType.IDENTIFIER, "anonymous", null, 1);
        return new LoxFunction(
                new Stmt.Function(name, expr.getParams(), expr.getBody()),
                environment,
                FunctionType.ANONYMOUS
        );
    }

    @Override
    public Void visitReturnStmt(Stmt.Return stmt) {
        Object value = null;
        if(stmt.getExpression() != null) {
            value = stmt.getExpression().accept(this);
        }
        throw new ReturnException(value);
    }

    private static boolean isEqual(Object a, Object b) {
        if (a == null && b == null) return true;
        if (a == null) return false;

        return a.equals(b);
    }

    private void checkNumberOperand(Token operator, Object operand) {
        if (operand instanceof Double) return;
        throw new RuntimeError(operator, "Operand must be a number.");
    }

    private static String stringify(Object object) {
        if (object == null) return "nil";

        if (object instanceof Double) {
            String text = object.toString();
            if (text.endsWith(".0")) {
                text = text.substring(0, text.length() - 2);
            }
            return text;
        }

        return object.toString();
    }

    protected void resolve(Expr expr, int depth) {
        locals.put(expr, depth);
    }

    private Object lookupVariable(Token name, Expr expr) {
        Integer distance = locals.get(expr);
        if(distance != null){
            return environment.getAt(distance, name);
        }
        else {
            return  globals.get(name);
        }

    }

    private static enum LoopState {
        None,
        Break,
        Continue
    }
}
