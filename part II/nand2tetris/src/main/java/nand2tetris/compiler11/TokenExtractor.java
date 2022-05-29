package nand2tetris.compiler11;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TokenExtractor {
    public static void main(String[] args) {
        String test = "aaa bbb  \"ccc\"";
        extractToken(test).forEach(System.out::println);
    }

    /**
     * There are 5 kinds of token:
     * keyword
     * symbol
     * integerConstant
     * stringConstant
     * identifier
     * <p>
     * 该方法的任务是从第一个字符开始匹配，找到所有的 token，存到 list 中并返回
     *
     * @param line
     * @return
     */
    public static List<Token> extractToken(String line) {
        //List<String> tokens = new ArrayList<>();
        List<Token> tmpTks = new ArrayList<>();

        int length = line.length();
        for (int i = 0, result = 0; i < length; i++) {
            result = matchString(line, i, length);
            if (result > i) {
                // 计算位置的时候去掉引号
                //tokens.add(line.substring(i + 1, result - 1));
                tmpTks.add(new Token(line.substring(i, result), TokenType.STRING_CONST));
                i = --result;
                continue;
            }

            // extract <=>
            result = match(">=<", line, i, length);
            if (result > i) {
                //tokens.add(line.substring(i, result));
                tmpTks.add(new Token(line.substring(i, result), TokenType.SYMBOL));
                i = --result;
                continue;
            }

            // extract symbol
            if (isInString(line.charAt(i), patternSymbol)) {
                //tokens.add(String.valueOf(line.charAt(i)));
                tmpTks.add(new Token(String.valueOf(line.charAt(i)), TokenType.SYMBOL));
            }

            // extract num
            result = match(patternInt, line, i, length);
            if (result > i) {
                //tokens.add(line.substring(i, result));
                tmpTks.add(new Token(line.substring(i, result), TokenType.INT_CONST));
                i = --result;
                continue;
            }

            // extract identity
            result = match(patternString, line, i, length);
            if (result > i) {
                String matchedIdentify = line.substring(i, result);

                if (Arrays.asList(KEYWORDS).contains(matchedIdentify)) {
                    tmpTks.add(new Token(line.substring(i, result), TokenType.KEYWORD));
                } else {
                    tmpTks.add(new Token(line.substring(i, result), TokenType.IDENTIFIER));
                }

                i = --result;
            }
        }

        return tmpTks;
    }

    private static int matchString(String line, int start, int end) {
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

    private static final String[] KEYWORDS = {"class", "constructor", "function", "method", "field", "static", "var",
            "int", "char", "boolean", "void", "true", "false", "null", "this", "let", "do", "if", "else", "while",
            "return"};

    private static final String patternInt = "1234567890";
    private static final String patternString = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz1234567890_";
    private static final String patternSymbol = "{}()[],.;+-*/&|<>=~";
}

