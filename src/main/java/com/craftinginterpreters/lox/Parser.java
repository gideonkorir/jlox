package com.craftinginterpreters.lox;

import java.util.List;
import static com.craftinginterpreters.lox.TokenType.*;

public class Parser {
    private List<Token> tokens;
    private int current = 0;

    public Parser(List<Token> tokens) {
        super();
        this.tokens = tokens;
    }

    public Expr parse() {
        try {
            return expression();
        }
        catch(ParseError e) {
            return null;
        }
    }

    private Expr expression() {
        return equality();
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

        if(match(LEFT_PAREN)) {
            Expr expr = parse();
            consume(RIGHT_PAREN, "Expected ')' after expression");
            return new Expr.Grouping(expr);
        }
        throw new RuntimeException("Unknown unary type: " + peek().getType());
    }

    private boolean isAtEnd() {
        return peek().getType() == EOF;
    }

    private Token peek() {
        return tokens.get(current);
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

    private void consume(TokenType tokenType, String errorMessage) {
        if(check(tokenType)) {
            advance();
        }
        error(peek(), errorMessage);
    }

    private ParseError error(Token token, String message) {
        reportError(token, message);
        return new ParseError();
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
