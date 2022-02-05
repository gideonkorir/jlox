package com.craftinginterpreters.lox;

import static org.junit.Assert.assertEquals;
import static com.craftinginterpreters.lox.TokenType.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

/**
 * Unit test for simple App.
 */
public class ScannerTest 
{
    /**
     * Rigorous Test :-)
     */
    @Test
    public void shouldLexAssignment()
    {
        Scanner scanner = new Scanner("var a = 1 + 1;");
        List<Token> tokens = scanner.scanTokens();
        assertHasTokenTypes(tokens, VAR, IDENTIFIER, EQUAL, NUMBER, PLUS, NUMBER, SEMICOLON, EOF);
    }

    @Test
    public void shouldLexBinaryExpression() {
        HashMap<String, TokenType> binaryOperators = new HashMap<>();
        binaryOperators.put("*", STAR);
        binaryOperators.put("+", PLUS);
        binaryOperators.put("/", SLASH);
        binaryOperators.put("-", MINUS);
        binaryOperators.put("<", LESS);
        binaryOperators.put("<=", LESS_EQUAL);
        binaryOperators.put(">", GREATER);
        binaryOperators.put(">=", GREATER_EQUAL);
        binaryOperators.put("!=", BANG_EQUAL);
        binaryOperators.put("or", OR);
        binaryOperators.put("and", AND);

        for(Map.Entry<String, TokenType> entry : binaryOperators.entrySet()) {
            String text = "5 " + entry.getKey() + " 8;";
            Scanner scanner = new Scanner(text);
            List<Token> tokens = scanner.scanTokens();
            assertHasTokenTypes(tokens, NUMBER, entry.getValue(), NUMBER, SEMICOLON);
        }
    }

    @Test
    public void shouldLexNumbers() {
        Scanner s = new Scanner("5 + 3.432");
        List<Token> tokens = s.scanTokens();
        assertEquals(Double.valueOf(5), tokens.get(0).getLiteral());
        assertEquals(Double.parseDouble("3.432"), tokens.get(2).getLiteral());
    }

    @Test
    public void shouldLexSingleLineString() {
        Scanner s = new Scanner("var a = \"This is amazing\"");
        List<Token> tokens = s.scanTokens();
        assertHasTokenTypes(tokens, VAR, IDENTIFIER, EQUAL, STRING);
        assertEquals("This is amazing", tokens.get(3).getLiteral());
    }

    @Test
    public void shouldLexMultiLineString() {
        Scanner s = new Scanner("var a = \"This is amazing \n lox is an amazing language\"");
        List<Token> tokens = s.scanTokens();
        assertHasTokenTypes(tokens, VAR, IDENTIFIER, EQUAL, STRING);
        assertEquals("This is amazing \n lox is an amazing language", tokens.get(3).getLiteral());
    }

    @Test
    public void shouldLexFunDefinition() {
        Scanner s = new Scanner("fun myfunc() { return 4 + 1; }");
        List<Token> tokens = s.scanTokens();
        assertHasTokenTypes(tokens, FUN, IDENTIFIER, LEFT_PAREN, RIGHT_PAREN, LEFT_BRACE, RETURN, NUMBER, PLUS, NUMBER, SEMICOLON, RIGHT_BRACE);
    }

    @Test
    public void shouldLexClassDefinition() {
        Scanner s = new Scanner("class A < B { init(meat) { super.init(meat); this.drink = 2; } }");
        List<Token> tokens = s.scanTokens();
        assertHasTokenTypes(tokens, 
            CLASS, IDENTIFIER, LESS, IDENTIFIER, //class A < B
             LEFT_BRACE, 
             IDENTIFIER, LEFT_PAREN, IDENTIFIER, RIGHT_PAREN, //init(meat)
             LEFT_BRACE,
             SUPER, DOT, IDENTIFIER, LEFT_PAREN, IDENTIFIER, RIGHT_PAREN, SEMICOLON, //super.init(meat);
             THIS, DOT, IDENTIFIER, EQUAL, NUMBER, SEMICOLON, //this.drink = 2;
             RIGHT_BRACE,
             RIGHT_BRACE
        );
    }

    @Test
    public void shouldLexIfStatement(){
        Scanner s = new Scanner("if (a >= b) { print a; } else { print b; }");
        List<Token> tokens = s.scanTokens();
        assertHasTokenTypes(tokens, IF, 
            LEFT_PAREN, IDENTIFIER, GREATER_EQUAL, IDENTIFIER, RIGHT_PAREN, //(a >= b)
            LEFT_BRACE, PRINT, IDENTIFIER, SEMICOLON, RIGHT_BRACE,
            ELSE, 
            LEFT_BRACE, PRINT, IDENTIFIER, SEMICOLON, RIGHT_BRACE
            );
    }

    @Test
    public void shouldConsumeMultiLineComment(){
        Scanner s = new Scanner("if (a >= b) { print a; /*print \n a*/ } else { print b; /* print b */ }");
        List<Token> tokens = s.scanTokens();
        assertHasTokenTypes(tokens, IF, 
            LEFT_PAREN, IDENTIFIER, GREATER_EQUAL, IDENTIFIER, RIGHT_PAREN, //(a >= b)
            LEFT_BRACE, PRINT, IDENTIFIER, SEMICOLON, RIGHT_BRACE,
            ELSE, 
            LEFT_BRACE, PRINT, IDENTIFIER, SEMICOLON, RIGHT_BRACE
            );
    }

    @Test
    public void shouldConsumeNestedMultiLineComment() {
    Scanner s = new Scanner("if (a >= b) { print a; /*print a /* not sure why */ */ } else { print b; }");
        List<Token> tokens = s.scanTokens();
        assertHasTokenTypes(tokens, IF, 
            LEFT_PAREN, IDENTIFIER, GREATER_EQUAL, IDENTIFIER, RIGHT_PAREN, //(a >= b)
            LEFT_BRACE, PRINT, IDENTIFIER, SEMICOLON, RIGHT_BRACE,
            ELSE, 
            LEFT_BRACE, PRINT, IDENTIFIER, SEMICOLON, RIGHT_BRACE
            );
    }

    private static void assertHasTokenTypes(List<Token> tokens, TokenType... expected){
        for(int i=0; i<expected.length; i++){
            assertEquals(expected[i], tokens.get(i).getType());
        }
    }

}
