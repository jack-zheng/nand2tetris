package nand2tetris.compiler10;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Scanner;
import java.util.regex.Pattern;

public class JackTokenizer {
    private Scanner myReader;
    private Token token;
    private final List<Token> tokens = new ArrayList<>();
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

        while (myReader.hasNextLine()) {
            String line = myReader.nextLine();
            extractOneLine(tokens, line);
        }
        myReader.close();
    }

    public JackTokenizer(String line) {
        extractOneLine(tokens, line);
    }

    private void extractOneLine(List<Token> tokens, String line) {
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
            return;
        }

        if (isMultiLine) {
            return;
        }

        if (line.isEmpty()) {
            return;
        }

        // after format, extract token
        tokens.addAll(TokenExtractor.extractToken(line));
    }

    public boolean hasMoreTokens() {
        return currentPos < tokens.size();
    }

    public String getToken() {
        return token.getToken();
    }

    public void advance() {
        if (!hasMoreTokens()) {
            throw new RuntimeException("No more element");
        }
        token = tokens.get(currentPos);
        currentPos++;
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
        return token.getType();
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
            return token.getToken();
        }
    }

    public String identifier() {
        if (tokenType() != TokenType.IDENTIFIER) {
            throw new RuntimeException("Illegal invoke");
        }
        return token.getToken();
    }

    public int intVal() {
        if (tokenType() != TokenType.INT_CONST) {
            throw new RuntimeException("Illegal invoke");
        }
        return Integer.parseInt(token.getToken());
    }

    public String stringVal() {
        if (tokenType() != TokenType.STRING_CONST) {
            throw new RuntimeException("Illegal invoke");
        }
        return token.getToken();
    }

    public String peekNextToken() {
        if (hasMoreTokens()) {
            return tokens.get(currentPos).getToken();
        }
        return null;
    }

    public Token peekNextTokenObj() {
        if (hasMoreTokens()) {
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

        //String baseDir = Objects.requireNonNull(JackAnalyzer.class.getResource(".")).getPath();
        //String folderPath = "/files/ExpressionLessSquare/";
        //String fileName = "Main.jack";
        //
        //JackTokenizer tokenizer = new JackTokenizer(new File(baseDir + folderPath + fileName));
        //tokenizer.advance();
        //System.out.println(tokenizer.token);
        //System.out.println(tokenizer.peekNextToken());

        JackTokenizer analyzer = new JackTokenizer("\"/\"");
        while (analyzer.hasMoreTokens()) {
            analyzer.advance();
            System.out.println(analyzer.getToken() + " - " + analyzer.tokenType());
        }


    }
}
