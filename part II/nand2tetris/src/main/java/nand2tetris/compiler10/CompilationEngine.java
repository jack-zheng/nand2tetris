package nand2tetris.compiler10;

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

    private enum SyntaxType {
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
        eatOneToken(cls);

        // for class name identifier
        eatOneToken(cls);

        // for symbol {
        eatOneToken(cls);

        // get next token and checking
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
        eatOneToken(cls);
    }

    public void compileClassVarDec() {
        Element varDec = doc.createElement(SyntaxType.CLASSVARDEC.tagName);
        cls.appendChild(varDec);

        // for keyword static|field
        eatOneToken(varDec);
        // for keyword boolean ect.
        eatOneToken(varDec);
        // for identifiers till meet ';'
        while (!";".equals(tokenizer.peekNextToken())) {
            eatOneToken(varDec);
        }
        // for ';'
        eatOneToken(varDec);
    }

    private void eatOneToken(Element parent) {
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
        eatOneToken(subroutineDec);
        // for type like void, int, boolean ect or function/constructor name
        eatOneToken(subroutineDec);
        // for constructor/func/method name
        eatOneToken(subroutineDec);
        // for (
        eatOneToken(subroutineDec);
        // for parameter list
        compileParameterList(subroutineDec);
        // for )
        eatOneToken(subroutineDec);
        // for routine body
        compileRoutineBody(subroutineDec);
    }

    public void compileParameterList(Element subroutineDec) {
        // write <parameterList>
        Element parameterList = doc.createElement(SyntaxType.PARAMETERLIST.tagName);
        subroutineDec.appendChild(parameterList);

        while (!")".equals(tokenizer.peekNextToken())) {
            eatOneToken(parameterList);
        }
    }

    public void compileRoutineBody(Element subroutineDec) {
        // write <subroutineBody>
        Element routineBody = doc.createElement(SyntaxType.SUBROUTINEBODY.tagName);
        subroutineDec.appendChild(routineBody);

        // for {
        eatOneToken(routineBody);
        while ("var".equals(tokenizer.peekNextToken())) {
            // 变量声明
            compileVarDec(routineBody);
        }
        // for statements
        compileStatements(routineBody);
        // for }
        eatOneToken(routineBody);
    }

    public void compileVarDec(Element subroutineBody) {
        Element varDec = doc.createElement(SyntaxType.VARDEC.tagName);
        subroutineBody.appendChild(varDec);

        while (!";".equals(tokenizer.peekNextToken())) {
            eatOneToken(varDec);
        }
        // for
        eatOneToken(varDec);
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
                    throw new RuntimeException("Unsupport type: " + nextToken);
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
        eatOneToken(doStatement);
        // subroutineName | className | varName
        eatOneToken(doStatement);
        while (".".equals(tokenizer.peekNextToken())) {
            // .
            eatOneToken(doStatement);
            // name
            eatOneToken(doStatement);
        }

        // (
        eatOneToken(doStatement);
        // expressionList
        compileExpressionList(doStatement);
        // )
        eatOneToken(doStatement);
        // ;
        eatOneToken(doStatement);
    }

    public void compileLet(Element node) {
        Element letStatement = doc.createElement(SyntaxType.LETSTATEMENT.tagName);
        node.appendChild(letStatement);

        // let
        eatOneToken(letStatement);
        // varName
        eatOneToken(letStatement);
        // check if need to parse expression
        if ("[".equals(tokenizer.peekNextToken())) {
            // [
            eatOneToken(letStatement);
            // expression
            compileExpression(letStatement);
            // ]
            eatOneToken(letStatement);
        }
        // =
        eatOneToken(letStatement);
        // expression
        compileExpression(letStatement);
        // for ;
        eatOneToken(letStatement);
    }

    public void compileWhile(Element node) {
        Element whileStatement = doc.createElement(SyntaxType.WHILESTATEMENT.tagName);
        node.appendChild(whileStatement);

        // while
        eatOneToken(whileStatement);
        // (
        eatOneToken(whileStatement);
        // expression
        compileExpression(whileStatement);
        // )
        eatOneToken(whileStatement);
        // {
        eatOneToken(whileStatement);
        // statements
        compileStatements(whileStatement);
        // }
        eatOneToken(whileStatement);
    }

    public void compileReturn(Element node) {
        Element returnStatement = doc.createElement(SyntaxType.RETURNSTATEMENT.tagName);
        node.appendChild(returnStatement);

        // return
        eatOneToken(returnStatement);
        if (!";".equals(tokenizer.peekNextToken())) {
            compileExpression(returnStatement);
        }
        // ;
        eatOneToken(returnStatement);
    }

    public void compileIf(Element node) {
        Element ifStatement = doc.createElement(SyntaxType.IFSTATEMENT.tagName);
        node.appendChild(ifStatement);

        // if
        eatOneToken(ifStatement);
        // (
        eatOneToken(ifStatement);
        // expression
        compileExpression(ifStatement);
        // )
        eatOneToken(ifStatement);
        // {
        eatOneToken(ifStatement);
        // statements
        compileStatements(ifStatement);
        // }
        eatOneToken(ifStatement);
        // check else condition
        if ("else".equals(tokenizer.peekNextToken())) {
            // else
            eatOneToken(ifStatement);
            // {
            eatOneToken(ifStatement);
            // statements
            compileStatements(ifStatement);
            // }
            eatOneToken(ifStatement);
        }
    }

    public void compileExpression(Element node) {
        Element exp = doc.createElement(SyntaxType.EXPRESSION.tagName);
        node.appendChild(exp);

        compileTerm(exp);
        // op +-*/&\>=< >= <=
        while (tokenizer.peekNextToken().matches("\\+|-|\\*|/|&|\\||>|=|<|>=|<=")) {
            eatOneToken(exp);
            compileTerm(exp);
        }
    }

    public void compileTerm(Element node) {
        Element term = doc.createElement(SyntaxType.TERM.tagName);
        node.appendChild(term);

        Token nextTokenObj = tokenizer.peekNextTokenObj();
        String nextToken = nextTokenObj.getToken();
        if ("(".equals(nextToken)) {
            // (
            eatOneToken(term);
            compileExpression(term);
            // )
            eatOneToken(term);
        } else if (nextToken.matches("\\-|~")) {
            // - ~
            eatOneToken(term);
            compileTerm(term);
        } else if (
                nextTokenObj.getType().equals(TokenType.INT_CONST) ||
                        nextTokenObj.getType().equals(TokenType.STRING_CONST) ||
                        nextTokenObj.getType().equals(TokenType.KEYWORD)
        ) {
            // 123 abc
            eatOneToken(term);
        } else if (TokenType.IDENTIFIER.equals(tokenizer.tokenType(nextToken))) {
            eatOneToken(term);

            String nextToken2 = tokenizer.peekNextToken();
            // name[]
            if ("[".equals(nextToken2)) {
                // [
                eatOneToken(term);
                // expression
                compileExpression(term);
                // ]
                eatOneToken(term);
            } else if ("(".equals(nextToken2)) {
                // name(expList)
                // (
                eatOneToken(term);
                compileExpressionList(term);
                // )
                eatOneToken(term);
            } else if (".".equals(nextToken2)) {
                // name.sub(expList)
                // .
                eatOneToken(term);
                // sub
                eatOneToken(term);
                // (
                eatOneToken(term);
                // expList
                compileExpressionList(term);
                // )
                eatOneToken(term);
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
            while (",".equals(tokenizer.peekNextToken())) {
                // ,
                eatOneToken(expList);
                compileExpression(expList);
            }
        }
    }

    public Document getParsedDocument() {
        return doc;
    }
}
