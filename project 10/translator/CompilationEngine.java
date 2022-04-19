package com.jk.translator;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;

public class CompilationEngine {
    private JackTokenizer tokenizer;
    private Document doc;
    private Element cls;

    private static enum SyntaxType {
        CLASSVARDEC("classVarDec"),
        SUBROUTINEDEC("subroutineDec"),
        PARAMETERLIST("parameterList"),
        SUBROUTINEBODY("subroutineBody"),
        VARDEC("varDec"),
        STATEMENTS("statements"),
        LETSTATEMENT("letStatement"),
        DOSTATEMENT("doStatement"),
        IFSTATEMENT("ifStatement"),
        WHILESTATEMENT("whileStatement"),
        RETURNSTATEMENT("returnStatement"),
        EXPRESSIONLIST("expressionList"),
        EXPRESSION("expression"),
        TERM("term");

        private String tagName;

        SyntaxType(String tagName) {
            this.tagName = tagName;
        }

        public String getTagName() {
            return tagName;
        }
    }

    public CompilationEngine(File source) {
        tokenizer = new JackTokenizer(source);

        try {
            DocumentBuilderFactory documentFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder documentBuilder = documentFactory.newDocumentBuilder();
            doc = documentBuilder.newDocument();
            cls = doc.createElement("class");
            doc.appendChild(cls);

        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        }

        compileClass();
    }

    public void compileClass() {
        // for class keyword
        //tokenizer.advance();
        //Element cls = doc.createElement(tokenizer.tokenType().getTagName());
        //cls.setTextContent(" " + tokenizer.getToken() + " ");
        //root.appendChild(cls);
        appendNodeTo(cls);

        // for class name identifier
        //tokenizer.advance();
        //Element clsName = doc.createElement(tokenizer.tokenType().getTagName());
        //cls.setTextContent(" " + tokenizer.getToken() + " ");
        //root.appendChild(clsName);
        appendNodeTo(cls);

        // for symbol {
        //tokenizer.advance();
        //Element symbol1 = doc.createElement(tokenizer.tokenType().getTagName());
        //cls.setTextContent(" " + tokenizer.getToken() + " ");
        //root.appendChild(symbol1);
        appendNodeTo(cls);

        // get next token for check
        // if it is '}', that means class end,
        // else it may classVarDec* or subroutineDec*
        String nextToken;
        while (!(nextToken = tokenizer.peekNextToken()).equals("}")) {
            // parse class var dec
            if (nextToken.matches("field|static")) {
                compileClassVarDec();
            } else if (nextToken.matches("constructor|function|method")) {
                // parse subroutine dec
                compileSubroutine();
            } else {
                throw new RuntimeException("Unexpected keyword " + nextToken);
            }
        }

        // for the end '}'
        appendNodeTo(cls);
    }

    public void compileClassVarDec() {
        Element varDec = doc.createElement(SyntaxType.CLASSVARDEC.tagName);
        cls.appendChild(varDec);

        // for keyword static|field
        appendNodeTo(varDec);
        // for keyword boolean ect.
        appendNodeTo(varDec);
        // for identifiers till meet ';'
        while (!";".equals(tokenizer.peekNextToken())) {
            appendNodeTo(varDec);
        }
        // for ';'
        appendNodeTo(varDec);
    }

    private void appendNodeTo(Element parent) {
        tokenizer.advance();
        Element node = doc.createElement(tokenizer.tokenType().getTagName());
        node.setTextContent(" " + tokenizer.getToken() + " ");
        parent.appendChild(node);

        System.out.println("append [" + node.getTagName() + ":" + node.getTextContent() + "] to " + parent.getTagName());
    }

    public void compileSubroutine() {
        Element subroutineDec = doc.createElement(SyntaxType.SUBROUTINEDEC.tagName);
        cls.appendChild(subroutineDec);

        // (constructor | function | method)
        appendNodeTo(subroutineDec);
        // for type like void, int, boolean ect or function/constructor name
        appendNodeTo(subroutineDec);
        // for constructor/func/method name
        appendNodeTo(subroutineDec);
        // for (
        appendNodeTo(subroutineDec);
        // for parameter list
        compileParameterList(subroutineDec);
        // for )
        appendNodeTo(subroutineDec);
        // for routine body
        compileRoutineBody(subroutineDec);
    }

    public void compileParameterList(Element subroutineDec) {
        // write <parameterList>
        Element parameterList = doc.createElement(SyntaxType.PARAMETERLIST.tagName);
        subroutineDec.appendChild(parameterList);

        while (!")".equals(tokenizer.peekNextToken())) {
            appendNodeTo(parameterList);
        }
    }

    public void compileRoutineBody(Element subroutineDec) {
        // write <subroutineBody>
        Element routineBody = doc.createElement(SyntaxType.SUBROUTINEBODY.tagName);
        subroutineDec.appendChild(routineBody);

        // for {
        appendNodeTo(routineBody);
        while ("var".equals(tokenizer.peekNextToken())) {
            // 变量声明
            compileVarDec(routineBody);
        }
        // for statements
        compileStatements(routineBody);
        // for }
        appendNodeTo(routineBody);
    }

    public void compileVarDec(Element subroutineBody) {
        Element varDec = doc.createElement(SyntaxType.VARDEC.tagName);
        subroutineBody.appendChild(varDec);

        while (!";".equals(tokenizer.peekNextToken())) {
            appendNodeTo(varDec);
        }
        // for
        appendNodeTo(varDec);
    }

    public void compileStatements(Element node) {
        Element statements = doc.createElement(SyntaxType.STATEMENTS.tagName);
        node.appendChild(statements);

        while (!"}".equals(tokenizer.peekNextToken())) {
            String nextToken = tokenizer.peekNextToken();
            System.out.println("compileStatements testing token: " + nextToken);
            switch (nextToken) {
                case "let":
                    compileLet(statements);
                    break;
                case "do":
                    compileDo(statements);
                    break;
                case "if":
                    compileIf(statements);
                    break;
                case "while":
                    compileWhile(statements);
                    break;
                case "return":
                    compileReturn(statements);
                    break;
                default:
                    throw new RuntimeException("Unsupport type");
            }
        }
    }

    /**
     * do subroutineName '(' expressionList')' | (className | varName) '.' subroutineName '(' expressionList ')'
     *
     * @param node
     */
    public void compileDo(Element node) {
        Element doStatement = doc.createElement(SyntaxType.DOSTATEMENT.tagName);
        node.appendChild(doStatement);

        // do
        appendNodeTo(doStatement);
        // subroutineName | className | varName
        appendNodeTo(doStatement);
        while (".".equals(tokenizer.peekNextToken())) {
            // .
             appendNodeTo(doStatement);
             // name
             appendNodeTo(doStatement);
        }

        // (
        appendNodeTo(doStatement);
        // expressionList
        compileExpressionList(doStatement);
        // )
        appendNodeTo(doStatement);
        // ;
        appendNodeTo(doStatement);
    }

    public void compileLet(Element node) {
        Element letStatement = doc.createElement(SyntaxType.LETSTATEMENT.tagName);
        node.appendChild(letStatement);

        // let
        appendNodeTo(letStatement);
        // varName
        appendNodeTo(letStatement);
        // check if need to parse expression
        if ("[".equals(tokenizer.peekNextToken())) {
            // [
            appendNodeTo(letStatement);
            // expression
            compileExpression(letStatement);
            // ]
            appendNodeTo(letStatement);
        }
        // =
        appendNodeTo(letStatement);
        // expression
        compileExpression(letStatement);
        // for ;
        appendNodeTo(letStatement);
    }

    public void compileWhile(Element node) {
        Element whileStatement = doc.createElement(SyntaxType.WHILESTATEMENT.tagName);
        node.appendChild(whileStatement);

        // while
        appendNodeTo(whileStatement);
        // (
        appendNodeTo(whileStatement);
        // expression
        compileExpression(whileStatement);
        // )
        appendNodeTo(whileStatement);
        // {
        appendNodeTo(whileStatement);
        // statements
        compileStatements(whileStatement);
        // }
        appendNodeTo(whileStatement);
    }

    public void compileReturn(Element node) {
        Element returnStatement = doc.createElement(SyntaxType.RETURNSTATEMENT.tagName);
        node.appendChild(returnStatement);

        // return
        appendNodeTo(returnStatement);
        if (!";".equals(tokenizer.peekNextToken())) {
            compileExpression(returnStatement);
        }
        // ;
        appendNodeTo(returnStatement);
    }

    public void compileIf(Element node) {
        Element ifStatement = doc.createElement(SyntaxType.IFSTATEMENT.tagName);
        node.appendChild(ifStatement);

        // if
        appendNodeTo(ifStatement);
        // (
        appendNodeTo(ifStatement);
        // expression
        compileExpression(ifStatement);
        // )
        appendNodeTo(ifStatement);
        // {
        appendNodeTo(ifStatement);
        // statements
        compileStatements(ifStatement);
        // }
        appendNodeTo(ifStatement);
        // check else condition
        if ("else".equals(tokenizer.peekNextToken())) {
            // else
            appendNodeTo(ifStatement);
            // {
            appendNodeTo(ifStatement);
            // statements
            compileStatements(ifStatement);
            // }
            appendNodeTo(ifStatement);
        }
    }

    public void compileExpression(Element node) {
        Element exp = doc.createElement(SyntaxType.EXPRESSION.tagName);
        node.appendChild(exp);

        compileTerm(exp);
        // op +-*/&\>=< >= <=
        while (tokenizer.peekNextToken().matches("\\+|-|\\*|/|&|\\||>|=|<|>=|<=")) {
            appendNodeTo(exp);
            compileTerm(exp);
        }
    }

    public void compileTerm(Element node) {
        Element term = doc.createElement(SyntaxType.TERM.tagName);
        node.appendChild(term);

        String nextToken = tokenizer.peekNextToken();
        if ("(".equals(nextToken)) {
            // (
            appendNodeTo(term);
            compileExpression(term);
            // )
            appendNodeTo(term);
        } else if (nextToken.matches("\\-|~")) {
            // - ~
            appendNodeTo(term);
            compileTerm(term);
        } else if (
                TokenType.INT_CONST.equals(tokenizer.tokenType(nextToken)) ||
                        TokenType.STRING_CONST.equals(tokenizer.tokenType(nextToken)) ||
                        TokenType.KEYWORD.equals(tokenizer.tokenType(nextToken))
        ) {
            // 123 abc
            appendNodeTo(term);
        } else if (TokenType.IDENTIFIER.equals(tokenizer.tokenType(nextToken))) {
            appendNodeTo(term);

            String nextToken2 = tokenizer.peekNextToken();
            // name[]
            if ("[".equals(nextToken2)) {
                // [
                appendNodeTo(term);
                // expression
                compileExpression(term);
                // ]
                appendNodeTo(term);
            } else if ("(".equals(nextToken2)) {
                // name(expList)
                // (
                appendNodeTo(term);
                compileExpressionList(term);
                // )
                appendNodeTo(term);
            } else if (".".equals(nextToken2)) {
                // name.sub(expList)
                // .
                appendNodeTo(term);
                // sub
                appendNodeTo(term);
                // (
                appendNodeTo(term);
                // expList
                compileExpressionList(term);
                // )
                appendNodeTo(term);
            }
        }
    }

    public void compileExpressionList(Element node) {
        Element expList = doc.createElement(SyntaxType.EXPRESSIONLIST.tagName);
        node.appendChild(expList);

        // 当 expression list 是空的时候格式如下 <expressionList> </expressionList>
        if (!")".equals(tokenizer.peekNextToken())) {
            // expression
            compileExpression(expList);
            while(",".equals(tokenizer.peekNextToken())) {
                // ,
                appendNodeTo(expList);
                compileExpression(expList);
            }
        }
    }

    public Document getParsedDocument() {
        return doc;
    }
}
