package com.craftinginterpreters.lox;

import com.craftinginterpreters.lox.visitors.Interpreter;
import com.craftinginterpreters.lox.visitors.LoxCallable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static com.craftinginterpreters.lox.TokenType.*;

public class Parser {
    private final List<Token> tokens;
    private int current = 0;

    private ParseContext parseContext =  ParseContext.DEFAULT;

    private int anonymousFunctionCount = 0;

    public Parser(List<Token> tokens) {
        super();
        this.tokens = tokens;
    }

    public List<Stmt> parse() {
        List<Stmt> statements = new ArrayList<>();
        try {
            while(!isAtEnd()) {
                statements.add(declaration());
            }
            return  statements;
        }
        catch(ParseError e) {
            return null;
        }
    }

    private Stmt declaration() {
        try{
            if(match(CLASS)) {
                return classDeclaration();
            }
            if(match(FUN)) {
                return function("function");
            }
            else if(match(VAR)){
                return varDeclaration();
            }
            return  statement();
        } catch (ParseError error){
            synchronize();
            return null;
        }
    }

    private Stmt classDeclaration() {
        Token name = consume(IDENTIFIER, "Expected class name.");
        consume(LEFT_BRACE, "Expected '{' before class body.");
        List<Stmt.Function> methods = new ArrayList<>();
        while(!check(RIGHT_BRACE) && !isAtEnd()) {
            methods.add(function("method"));
        }
        consume(RIGHT_BRACE, "Expected '}' after class body.");
        return  new Stmt.Class(name, methods);
    }

    private Stmt.Function function(String kind) {
        Token name = consume(IDENTIFIER, "Expect " + kind + " name.");
        FunctionDetail detail = getFunctionDetail(kind);
        return  new Stmt.Function(name, detail.getParams(), detail.getBody());
    }

    private FunctionDetail getFunctionDetail(String kind) {
        consume(LEFT_PAREN, "Expect '(' after " + kind + " name.");
        List<Token> parameters = new ArrayList<>();
        if (!check(RIGHT_PAREN)) {
            do {
                if (parameters.size() >= 255) {
                    throw error(peek(), "Can't have more than 255 parameters.");
                }

                parameters.add(
                        consume(IDENTIFIER, "Expect parameter name."));
            } while (match(COMMA));
        }
        consume(RIGHT_PAREN, "Expect ')' after parameters.");
        consume(LEFT_BRACE, "Expect '{' before " + kind + " body.");
        List<Stmt> body = block();
        return new FunctionDetail(parameters, body);
    }

    private Stmt varDeclaration() {
        Token name = consume(IDENTIFIER, "Expect variable name.");
        Expr initializer = null;
        if (match(EQUAL)) {
            initializer = expression();
        }

        consume(SEMICOLON, "Expect ';' after variable declaration.");
        return new Stmt.Var(name, initializer);
    }

    private Stmt statement(){
        if(match(PRINT)) {
            return printStatement();
        } else if(match(IF)){
            return ifStatement();
        } else if (match(WHILE)) {
            return whileStatement();
        } else if (match(FOR)) {
            return  forStatement();
        }else if (match(LEFT_BRACE)){
            return new Stmt.Block(block());
        } else if (match(CONTINUE)) {
            return continueStmt();
        } else if (match(BREAK)) {
            return breakStmt();
        } else if (match(RETURN)) {
            return returnStmt();
        }
        return  expressionStatement();
    }

    private Stmt printStatement() {
        Expr value = expression();
        consume(SEMICOLON, "Expect ';' after value.");
        return new Stmt.Print(value);
    }

    private Stmt ifStatement() {
        consume(LEFT_PAREN, "Expected '(' after if.");
        Expr condition = expression();
        consume(RIGHT_PAREN, "Expected ')' after if condition.");
        Stmt thenBranch = statement();
        Stmt elseBranch = null;
        if(match(ELSE)){
            elseBranch = statement();
        }
        return new Stmt.If(condition, thenBranch, elseBranch);
    }

    private Stmt whileStatement() {
        consume(LEFT_PAREN, "expected '(' for while statement condition.");
        Expr condition = expression();
        consume(RIGHT_PAREN, "Expected ')' for while statement condition");
        parseContext = ParseContext.LOOP_BODY;
        Stmt body = statement();
        parseContext = ParseContext.DEFAULT;
        return  new Stmt.While(condition, body);
    }

    private Stmt forStatement() {
        consume(LEFT_PAREN, "Expected '(' after for keyword");
        Stmt initializer;
        if(match(VAR)) {
            initializer = varDeclaration();
        } else if(match(SEMICOLON)){
            initializer = null;
        } else {
            initializer = expressionStatement();
        }

        //the semicolon is already consumed at this point
        Expr condition = null;
        if(!check(SEMICOLON)) {
            condition = expression();
        }
        consume(SEMICOLON, "Expected ';' after loop condition");

        Expr increment = null;
        if(!check(RIGHT_PAREN)) {
            increment = expression();
        }
        consume(RIGHT_PAREN, "Expected ')' after for clause");
        parseContext = ParseContext.LOOP_BODY;
        Stmt body = statement();
        parseContext = ParseContext.DEFAULT;
        return new Stmt.For(initializer, condition, increment, body);

        /* Note: Decided against de sugaring for now so that I can implement continue on the for loop
        // When I desugar, I can't find a good way to implement the continue statement, I need the increment
        // to always be executed otherwise the loop just hangs because the loop variable isn't updated.
        // I checked: https://stackoverflow.com/questions/14386679/how-to-use-the-statement-continue-in-while-loop#:~:text=Do%20your%20increment%20at%20the%20beginning%20instead%20of,or%20this%20doesn%27t%20really%20make%20much%20sense%20%7D
        //desugaring
        if (increment != null) {
            //if the increment isn't null then include the increment to the end
            //of the iteration body i.e., the increment is done after an execution of the body.
            body = new Stmt.Block(
                    Arrays.asList(
                            body,
                            new Stmt.Expression(increment)));
        }
        if (condition == null) {
            //No condition means true
            condition = new Expr.Literal(true);
        }
        body = new Stmt.While(condition, body);

        if (initializer != null) {
            //if the initializer exists then add it as a statement before
            //the while loop.
            body = new Stmt.Block(Arrays.asList(initializer, body));
        }
        return  body;*/
    }

    private Stmt continueStmt(){
        if(parseContext != ParseContext.LOOP_BODY) {
            throw error(previous(), "The continue keyword is only allowed in the context of a loop");
        }
        consume(SEMICOLON, "Semicolon required after continue statement");
        return new Stmt.Keyword(CONTINUE);
    }

    private Stmt breakStmt() {
        if(parseContext != ParseContext.LOOP_BODY) {
            throw error(previous(), "The break keyword is only allowed in the context of a loop");
        }
        consume(SEMICOLON, "Semicolon required after break statement");
        return  new Stmt.Keyword(BREAK);
    }

    private Stmt returnStmt() {
        Token keyword = previous();
        Expr value = null;
        if (!check(SEMICOLON)) {
            value = expression();
        }

        consume(SEMICOLON, "Expect ';' after return value.");
        return new Stmt.Return(keyword, value);
    }

    private List<Stmt> block() {
        List<Stmt> statements = new ArrayList<>();

        while (!check(RIGHT_BRACE) && !isAtEnd()) {
            statements.add(declaration());
        }

        consume(RIGHT_BRACE, "Expect '}' after block.");
        return statements;
    }

    private Stmt expressionStatement() {
        int eCurrent = current;
        Expr expr = expression();
        if(eCurrent == 0 && isAtEnd()){
            return  new Stmt.Print(expr);
        }
        consume(SEMICOLON, "Expect ';' after expression.");
        return new Stmt.Expression(expr);
    }

    private Expr expression() {
        //function expression
        if(match(FUN)) {
            return  anonymousFunction();
        }
        return assignment();
    }

    private Expr anonymousFunction () {
        //we've already matched fun here
        FunctionDetail detail =getFunctionDetail("anonymous function");
        return  new Expr.AnonymousFunction(detail.getParams(), detail.getBody());
    }

    private Expr assignment() {
        Expr expr = logicOr(); //if no = it will return the identifier

        if (match(EQUAL)) {
            Token equals = previous();
            Expr value = expression();

            if (expr instanceof Expr.Variable) {
                Token name = ((Expr.Variable)expr).getName();
                return new Expr.Assignment(name, value);
            } else if(expr instanceof Expr.Get) {
                Expr.Get get = (Expr.Get)expr;
                return new Expr.Set(get.getOperand(), get.getMember(), value);
            }

            throw error(equals, "Invalid assignment target.");
        }

        return expr;
    }

    private Expr logicOr() {
        Expr left = logicAnd();
        if(match(OR)) {
            Expr right = logicAnd();
            left = new Expr.Logical(left, OR, right);
        }
        return  left;
    }

    private Expr logicAnd() {
        Expr left = equality();
        if(match(AND)){
            Expr right = equality();
            left = new Expr.Logical(left, AND, right);
        }
        return  left;
    }

    private Expr equality() {
        Expr left = comparison();

        while(match(BANG_EQUAL, EQUAL_EQUAL))
        {
            Token operator = previous();
            Expr right = comparison();
            left = new Expr.Binary(left, operator, right);
        }
        return left;
    }

    private Expr comparison() {
        Expr left = term();
        while(match(GREATER, GREATER_EQUAL, LESS, LESS_EQUAL)) {
            Token operator = previous();
            Expr right = term();
            left = new Expr.Binary(left, operator, right);
        }
        return left;
    }

    private Expr term() {
        Expr left = factor();
        while(match(MINUS, PLUS)) {
            Token operator = previous();
            Expr right = factor();
            left = new Expr.Binary(left, operator, right);
        }
        return left;
    }

    private Expr factor() {
        Expr left = unary();
        while(match(SLASH, STAR, PERCENT)) {
            Token operator = previous();
            Expr right = unary();
            left = new Expr.Binary(left, operator, right);
        }
        return left;
    }

    private Expr unary() {
        if(match(BANG, MINUS)) {
            Token operator = previous();
            Expr right = unary();
            return new Expr.Unary(operator, right);
        }
        return call();
    }

    private Expr call() {
        Expr expr = primary();
        while(true) {
            if(match(LEFT_PAREN)){
                expr = finishCall(expr);
            } else if(match(DOT)) {
                Token name = consume(IDENTIFIER, "Expected identifier after '.'.");
                expr = new Expr.Get(expr, name);
            } else {
                break;
            }
        }

        return  expr;
    }

    private Expr finishCall(Expr callee) {
        List<Expr> arguments = new ArrayList<>();
        if(!check(RIGHT_PAREN)){
            do {
                if(arguments.size() == 255){
                    throw error(peek(), "Can't have more than 255 arguments.");
                }
                arguments.add(expression());
            }while(match(COMMA));
        }
        Token paren = consume(RIGHT_PAREN, "Expect ')' after arguments.");
        return  new Expr.Call(callee, paren, arguments);
    }

    private Expr primary() {
        if(match(FALSE)) return new Expr.Literal(false);
        if(match(TRUE)) return new Expr.Literal(true);
        if(match(NIL)) return new  Expr.Literal(null);
        if(match(THIS)) return new Expr.This(previous());

        if(match(NUMBER, STRING)) {
            return new Expr.Literal(previous().getLiteral());
        }

        if(match(IDENTIFIER)){
            return  new Expr.Variable(previous());
        }
        if(match(LEFT_PAREN)) {
            Expr expr = expression();
            consume(RIGHT_PAREN, "Expected ')' after expression");
            return new Expr.Grouping(expr);
        }
        throw new RuntimeException("Unknown primary type: " + peek().getType());
    }

    private boolean isAtEnd() {
        return peek().getType() == EOF;
    }

    private Token peek() {
        return tokens.get(current);
    }

    private Token peek(int lookAhead) {
        int next = current + lookAhead;
        if(next >= tokens.size()){
            return  null;
        }
        return  tokens.get(next);
    }

    private Token previous() {
        return tokens.get(current - 1);
    }

    private Token advance() {
        Token temp = peek();
        if(!isAtEnd()) {
            current ++;
        }
        return temp;
    }

    private boolean match(TokenType... tokenTypes)
    {
        for (TokenType tokenType : tokenTypes) {
            if(check(tokenType)) {
                advance();
                return true;
            }
        }
        return false;
    }

    private boolean check(TokenType tokenType)
    {
        if(isAtEnd()) {
            return false;
        }
        return peek().getType() == tokenType;
    }

    private Token consume(TokenType tokenType, String errorMessage) {
        if(check(tokenType)) {
            return advance();
        }
        throw error(peek(), errorMessage);
    }

    private ParseError error(Token token, String message) {
        reportError(token, message);
        return new ParseError();
    }

    private void synchronize() {
        advance();

        while (!isAtEnd()) {
            if (previous().getType() == SEMICOLON) return;

            switch (peek().getType()) {
                case CLASS:
                case FUN:
                case VAR:
                case FOR:
                case IF:
                case WHILE:
                case PRINT:
                case RETURN:
                    return;
            }

            advance();
        }
    }

    private static void reportError(Token token, String message) {
        if (token.getType() == TokenType.EOF) {
            Lox.report(token.getLine(), " at end", message);
        } else {
            Lox.report(token.getLine(), " at '" + token.getLexeme() + "'", message);
        }
    }

    private static class ParseError extends RuntimeException {}

    private static enum ParseContext{
        DEFAULT,
        LOOP_BODY
    }

    @Data
    @RequiredArgsConstructor
    @EqualsAndHashCode(callSuper = false)
    private static class FunctionDetail
    {
        private final List<Token> params;
        private final List<Stmt> body;
    }
}
