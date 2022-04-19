package com.jk.translator;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Scanner;
import java.util.regex.Pattern;

public class JackTokenizer {

    private Scanner myReader;
    private String token;
    private List<String> tokens = new ArrayList<>();
    private int currentPos = 0;

    // Keyword
    public static final String[] KEYWORDS = {"class", "constructor", "function", "method", "field", "static", "var",
            "int", "char", "boolean", "void", "true", "false", "null", "this", "let", "do", "if", "else", "while",
            "return"};
    // SYMBOLS
    public static final String[] SYMBOLS = {"{", "}", "(", ")", "[", "]", ".", ",", ";", "+", "-", "*", "/", "&", "|",
            "<", ">", "=", "~"};

    boolean isMultiLine = false;

    public JackTokenizer(File file) {
        try {
            myReader = new Scanner(file);
        } catch (Exception e) {
            e.printStackTrace();
        }

        String patternInt = "1234567890";
        String patternString = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz1234567890_";
        String patternSymbol = "{}()[],.;+-*/&|<>=~";

        while (myReader.hasNextLine()) {
            String line = myReader.nextLine();
            // remove redundant space
            line = line.trim();

            // 注释行包含以下形式
            // 1. // 开头
            // 2. // 在中间
            // 2. /* xxx */
            // 3. /* 复数行形式 */

            // code from: hitori-Janai
            // for scenario 1,2
            if (line.contains("//")) {
                line = line.substring(0, line.indexOf("//"));
            }

            // for scenario 3,4
            if (!isMultiLine && line.contains("/*")) {
                isMultiLine = true;
            }

            if (isMultiLine && line.contains("*/")) {
                isMultiLine = false;
                continue;
            }

            if (isMultiLine) {
                continue;
            }

            if (line.isEmpty()) {
                continue;
            }

            // after format, extract token
            int length = line.length();
            for (int i = 0, result = 0; i < length; i++) {
                result = matchString(line, i, length);
                if (result > i) {
                    // 计算位置的时候去掉引号
                    tokens.add(line.substring(i + 1, result - 1));
                    i = --result;
                    continue;
                }

                // extract <=>
                result = match(">=<", line, i, length);
                if (result > i) {
                    tokens.add(line.substring(i, result));
                    i = --result;
                    continue;
                }

                // extract symbol
                if (isInString(line.charAt(i), patternSymbol)) {
                    tokens.add(String.valueOf(line.charAt(i)));
                }

                // extract num
                result = match(patternInt, line, i, length);
                if (result > i) {
                    tokens.add(line.substring(i, result));
                    i = --result;
                    continue;
                }

                // extract identity
                result = match(patternString, line, i, length);
                if (result > i) {
                    // System.out.println(line.substring(i, result));
                    tokens.add(line.substring(i, result));
                    i = --result;
                }
            }
        }

        //System.out.println("tokens: ");
        //tokens.forEach(System.out::println);

        myReader.close();
    }

    public static int matchString(String line, int start, int end) {
        if (line.charAt(start) != '"') {
            return start;
        }

        while (++start < end && line.charAt(start) != '"') {
        }
        return ++start;
    }

    private static int match(String pattern, String line, int start, int end) {
        while (start < end && isInString(line.charAt(start), pattern)) {
            start++;
        }
        return start;
    }

    private static boolean isInString(char a, String pattern) {
        for (int i = 0; i < pattern.length(); i++) {
            if (pattern.charAt(i) == a) {
                return true;
            }
        }
        return false;
    }

    public boolean hasMoreTokens() {
        return tokens.size() - currentPos > 0;
    }

    public String getToken() {
        return token;
    }

    public void advance() {
        if (!hasMoreTokens()) {
            throw new RuntimeException("No more element");
        }
        token = tokens.get(currentPos++);
    }

    public TokenType tokenType(String token) {
        for (String var : KEYWORDS) {
            if (var.equals(token)) {
                return TokenType.KEYWORD;
            }
        }

        for (String var : SYMBOLS) {
            if (var.equals(token)) {
                return TokenType.SYMBOL;
            }
        }

        // identify type
        String IDENTIFIER = "^[a-zA-Z_]{1}[a-zA-Z0-9_]*";
        Pattern pa = Pattern.compile(IDENTIFIER);
        if (pa.matcher(token).matches()) {
            return TokenType.IDENTIFIER;
        }

        // number type
        Pattern paInt = Pattern.compile("[0-9]*");
        if (paInt.matcher(token).matches()) {
            return TokenType.INT_CONST;
        }

        return TokenType.STRING_CONST;
    }

    public TokenType tokenType() {
        return tokenType(this.token);
    }

    //public KeyWord keyword() {
    //    if (tokenType() != TokenType.KEYWORD) {
    //        throw new RuntimeException("Illegal invoke");
    //    }
    //    return KeyWord.valueOf(token.toUpperCase());
    //}

    public String symbol() {
        if (tokenType() != TokenType.SYMBOL) {
            throw new RuntimeException("Illegal invoke");
        }

        if (token.equals("<")) {
            return "&lt;";
        } else if (token.equals(">")) {
            return "&gt;";
        } else if (token.equals("&")) {
            return "&amp";
        } else {
            return token;
        }
    }

    public String identifier() {
        if (tokenType() != TokenType.IDENTIFIER) {
            throw new RuntimeException("Illegal invoke");
        }
        return token;
    }

    public int intVal() {
        if (tokenType() != TokenType.INT_CONST) {
            throw new RuntimeException("Illegal invoke");
        }
        return Integer.parseInt(token);
    }

    public String stringVal() {
        if (tokenType() != TokenType.STRING_CONST) {
            throw new RuntimeException("Illegal invoke");
        }
        return token;
    }

    public String peekNextToken() {
        if(hasMoreTokens()) {
            return tokens.get(currentPos);
        }
        return null;
    }

    public static void main(String[] args) {
        //System.out.println(JackTokenizer.isCommentLine("// asdf "));
        //System.out.println(JackTokenizer.isCommentLine("/* sdfad */"));
        //System.out.println(JackTokenizer.isCommentLine("/** sdfad */"));
        //System.out.println(JackTokenizer.isCommentLine("/** sdfad **/"));
        //System.out.println(JackTokenizer.isCommentLine("asdf"));

        //String line = "\"hello\"world";
        //System.out.println(JackTokenizer.matchString(line, 0, 11));
        //System.out.println(line.substring(0, 6));
        //
        //int i = 10;
        //System.out.println(i--);
        //System.out.println(--i);

        String baseDir = Objects.requireNonNull(JackAnalyzer.class.getResource(".")).getPath();
        String folderPath = "/files/ExpressionLessSquare/";
        String fileName = "Main.jack";

        JackTokenizer tokenizer = new JackTokenizer(new File(baseDir + folderPath + fileName));
        tokenizer.advance();
        System.out.println(tokenizer.token);
        System.out.println(tokenizer.peekNextToken());
    }
}
