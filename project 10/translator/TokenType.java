package com.jk.translator;

public enum TokenType {
    KEYWORD("keyword"), SYMBOL("symbol"), IDENTIFIER("identifier"), INT_CONST("integerConstant"), STRING_CONST("stringConstant");

    private String tagName;

    TokenType(String xmlTag) {
        this.tagName = xmlTag;
    }

    public String getTagName() {
        return tagName;
    }

    public static void main(String[] args) {
        System.out.println(KEYWORD);
        System.out.println(KEYWORD.getTagName());
    }
}
