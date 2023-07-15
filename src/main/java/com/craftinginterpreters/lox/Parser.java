package com.craftinginterpreters.lox;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import static com.craftinginterpreters.lox.TokenType.*;

public class Parser {
    private final List<Token> tokens;
    private int current = 0;

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
            if(match(VAR)){
                return varDeclaration();
            }
            return  statement();
        } catch (ParseError error){
            synchronize();
            return null;
        }
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
        Stmt body = statement();
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
        Stmt body = statement();

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
        return  body;
    }

    private Stmt continueStmt(){
        consume(SEMICOLON, "Semicolon required after continue statement");
        return new Stmt.Keyword(CONTINUE);
    }

    private Stmt breakStmt() {
        consume(SEMICOLON, "Semicolon required after break statement");
        return  new Stmt.Keyword(BREAK);
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
        return assignment();
    }

    private Expr assignment() {
        Expr expr = logicOr(); //if no = it will return the identifier

        if (match(EQUAL)) {
            Token equals = previous();
            Expr value = assignment();

            if (expr instanceof Expr.Variable) {
                Token name = ((Expr.Variable)expr).getName();
                return new Expr.Assignment(name, value);
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
        while(match(SLASH, STAR)) {
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
        return primary();
    }

    private Expr primary() {
        if(match(FALSE)) return new Expr.Literal(false);
        if(match(TRUE)) return new Expr.Literal(true);
        if(match(NIL)) return new  Expr.Literal(null);

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
}
