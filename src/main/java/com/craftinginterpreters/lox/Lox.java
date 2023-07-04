package com.craftinginterpreters.lox;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Statement;
import java.util.List;

import com.craftinginterpreters.lox.visitors.AstPrinter;
import com.craftinginterpreters.lox.visitors.Interpreter;

/**
 * Hello world!
 *
 */
public class Lox 
{
    private static boolean hadError;
    private static boolean hadRuntimeError;

    private final static Interpreter interpreter = new Interpreter();

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
        PRINT_AST,
        EVALUATE
    }

    private static void runPrompt() throws IOException {
        RunMode runMode = RunMode.EVALUATE;
        InputStreamReader input = new InputStreamReader(System.in);
        BufferedReader reader = new BufferedReader(input);
        for(;;) {
            System.out.print("> ");
            String line = reader.readLine();
            if(line == null) { break; }
            if(line.length() == 0) { continue; }
            if(line.compareToIgnoreCase("#printast") == 0) {
                //enable print ast
                runMode = RunMode.PRINT_AST;
            } else if(line.compareToIgnoreCase("#printtokens") == 0){
                runMode = RunMode.PRINT_TOKENS;
            } else {
                if(runMode == RunMode.PRINT_TOKENS) {
                    run(line);
                } else if(runMode == RunMode.PRINT_AST) {
                    printAst(line);
                } else if(runMode == RunMode.EVALUATE) {
                    evaluate(line);
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
        Parser parser = new Parser(tokens);
        List<Stmt> statements = parser.parse();
        AstPrinter printer = new AstPrinter();
        for (Stmt s: statements) {
            System.out.println(printer.print(s));
        }
    }

    private static void evaluate(String source) {
        Scanner scanner = new Scanner(source);
        List<Token> tokens = scanner.scanTokens();
        Parser parser = new Parser(tokens);
        List<Stmt> statements = parser.parse();
        try {
            interpreter.interpret(statements);
        } catch (RuntimeError ignored) {

        }
    }

    static void error(int line, String message) {
        report(line, "", message);
    }

    public static void report(int line, String where, String message)
    {
        System.err.println(
        "[line " + line + "] Error" + where + ": " + message);
        hadError = true;
    }

    public static void runtimeError(RuntimeError error) {
        System.err.println(error.getMessage() +
                "\n[line " + error.token.getLine() + "]");
        hadRuntimeError = true;
    }
}
