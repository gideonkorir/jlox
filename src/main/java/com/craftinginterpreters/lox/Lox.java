package com.craftinginterpreters.lox;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import com.craftinginterpreters.lox.visitors.AstPrinter;

/**
 * Hello world!
 *
 */
public class Lox 
{
    private static boolean hadError;

    public static void main( String[] args ) throws IOException
    {
        if(args.length > 1)
        {
            System.out.println("Usage: jlox [script]");
            System.exit(64);
        }
        else if(args.length == 1) {
            //This path means we want to run a lox file
            runFile(args[0]);
        }
        else {
            //We want to run on console
            runPrompt();
        }
    }

    private static void runFile(String path) throws IOException {
        byte[] bytes = Files.readAllBytes(Paths.get(path));
        run(new String(bytes, Charset.defaultCharset()));
        if(hadError) { System.exit(65); }
    }

    static enum RunMode {
        PRINT_TOKENS,
        PRINT_AST
    }

    private static void runPrompt() throws IOException {
        RunMode runMode = RunMode.PRINT_TOKENS;
        InputStreamReader input = new InputStreamReader(System.in);
        BufferedReader reader = new BufferedReader(input);
        for(;;) {
            System.out.print("> ");
            String line = reader.readLine();
            if(line == null) { break; }
            if(line.compareToIgnoreCase("#printast") == 0) {
                //enable print ast
                runMode = RunMode.PRINT_AST;
            } else {
                if(runMode == RunMode.PRINT_TOKENS) {
                    run(line);
                } else if(runMode == RunMode.PRINT_AST) {
                    printAst(line);
                }
                hadError = false;
            }
        }
    }

    private static void run(String source) {
        Scanner scanner = new Scanner(source);
        List<Token> tokens = scanner.scanTokens();
        for(Token token : tokens) {
            System.out.println(token);
        }
    }

    private static void printAst(String source){
        Scanner scanner = new Scanner(source);
        List<Token> tokens = scanner.scanTokens();
        Expr expression = new Expr.BinaryExpr(
        new Expr.UnaryExpr(
            new Token(TokenType.MINUS, "-", null, 1),
            new Expr.LiteralExpr(123)),
        new Token(TokenType.STAR, "*", null, 1),
        new Expr.GroupingExpr(
            new Expr.LiteralExpr(45.67)));

        System.out.println(new AstPrinter().print(expression));
    }

    static void error(int line, String message) {
        report(line, "", message);
    }

    private static void report(int line, String where, String message)
    {
        System.err.println(
        "[line " + line + "] Error" + where + ": " + message);
        hadError = true;
    }
}
