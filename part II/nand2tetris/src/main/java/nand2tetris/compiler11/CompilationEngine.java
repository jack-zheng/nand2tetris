package nand2tetris.compiler11;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;

public class CompilationEngine {
    private JackTokenizer tokenizer;
    private Document doc;
    private Element cls;
    private final String fileName;
    // constructor, method or function
    private String subroutineKind;
    // data type or void
    private String subroutineRetType;
    private String subroutineName;
    private SymbolTable symbolTable = new SymbolTable();
    private VMWriter vmWriter;
    private int ifCounter = 0;
    private static final String IF_TRUE = "IF_TRUE";
    private static final String IF_FALSE = "IF_FALSE";
    private static final String IF_END = "IF_END";
    private int whileCounter = 0;
    private static final String whileLabelPrefix = "while";
    private static final String WHILE_EXP = "WHILE_EXP";
    private static final String WHILE_END = "WHILE_END";

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
        fileName = source.getName().split("\\.")[0];
        tokenizer = new JackTokenizer(source);

        // calculate output path
        String parentPath = source.getParent();
        vmWriter = new VMWriter(new File(parentPath + "/" + fileName + ".vm"));

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
        vmWriter.close();
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

    /**
     * 当解析 class var dec 时，将定义的类变量设置进去
     */
    public void compileClassVarDec() {
        Element varDec = doc.createElement(SyntaxType.CLASSVARDEC.tagName);
        cls.appendChild(varDec);

        // for keyword static|field
        eatOneToken(varDec);
        SymbolTable.Kind kind = SymbolTable.Kind.valueOf(tokenizer.getToken().toUpperCase());
        // for keyword boolean ect.
        eatOneToken(varDec);
        // for identifiers till meet ';'
        String type = tokenizer.getToken();
        while (!";".equals(tokenizer.peekNextToken())) {
            if (tokenizer.peekNextToken().equals(",")) {
                eatOneToken(varDec);
            } else {
                eatIdentifier(varDec, type, kind.name());
                symbolTable.define(tokenizer.getToken(), type, kind);
            }
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

    private void eatIdentifier(Element parent, String type, String kind) {
        tokenizer.advance();
        Element node = doc.createElement(tokenizer.tokenType().getTagName());
        node.setAttribute("type", type);
        node.setAttribute("kind", kind);
        node.setAttribute("status", "defined");
        node.setTextContent(" " + tokenizer.getToken() + " ");
        parent.appendChild(node);

        System.out.println("append [" + node.getTagName() + ":" + node.getTextContent() + "] to " + parent.getTagName());
    }

    private void eatIdentifierInStatement(Element parent, String status) {
        tokenizer.advance();
        Element node = doc.createElement(tokenizer.tokenType().getTagName());
        String token = tokenizer.getToken();
        node.setAttribute("type", symbolTable.typeOf(token));
        // 如果在 let 语句中出现效用函数的情况，会抛做
        //node.setAttribute("kind", symbolTable.kindOf(token).name());
        node.setAttribute("status", "used");
        node.setTextContent(" " + token + " ");
        parent.appendChild(node);

        System.out.println("append [" + node.getTagName() + ":" + node.getTextContent() + "] to " + parent.getTagName());
    }

    public void compileSubroutine() {
        // 每次编译 subroutine 时重置 subroutineTable
        symbolTable.startSubroutine();

        Element subroutineDec = doc.createElement(SyntaxType.SUBROUTINEDEC.tagName);
        cls.appendChild(subroutineDec);

        // (constructor | function | method)
        eatOneToken(subroutineDec);
        subroutineKind = tokenizer.getToken();
        // 如果是 method 类型的，需要将 this 加入到 symboleTable 中作为第一个 ARG 类型的变量
        if (subroutineKind.equals("method")) {
            symbolTable.define("argument0", "This", SymbolTable.Kind.ARG);
        }

        // for type like void, int, boolean ect or function/constructor name
        eatOneToken(subroutineDec);
        // for constructor/func/method name
        eatOneToken(subroutineDec);
        subroutineName = tokenizer.getToken();
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
            // parse e.g. int count, String name
            // eat type
            eatOneToken(parameterList);
            String type = tokenizer.getToken();
            // eat identify
            eatIdentifier(parameterList, type, SymbolTable.Kind.ARG.name());
            symbolTable.define(tokenizer.getToken(), type, SymbolTable.Kind.ARG);
            if (",".equals(tokenizer.peekNextToken())) {
                // eat ','
                eatOneToken(parameterList);
            }
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
        // 收集完 subroutine 中的 local 变量信息之后就可以写 function 声明语句了
        vmWriter.writeFunction(fileName + "." + subroutineName, symbolTable.varCount(SymbolTable.Kind.VAR));

        if (subroutineKind.equals("constructor")) {
            // 指定 field 数量
            int fieldCount = symbolTable.varCount(SymbolTable.Kind.FIELD);
            vmWriter.writePush(VMWriter.Segment.CONST, fieldCount);
            // 调用 memory 方法拿到起始地址 call Memory.alloc 1
            vmWriter.writeCall("Memory.alloc", 1);
            // pop pointer 0
            vmWriter.writePop(VMWriter.Segment.POINTER, 0);
        } else if (subroutineKind.equals("method")) {
            // 处理 method 类型的方法时，需要先将对象 push 进去作为第一个 ARG，pop 到 pointer 0 在做处理
            vmWriter.writePush(VMWriter.Segment.ARG, 0);
            vmWriter.writePop(VMWriter.Segment.POINTER, 0);
        }

        // for statements
        compileStatements(routineBody);
        // for }
        eatOneToken(routineBody);
    }

    public void compileVarDec(Element subroutineBody) {
        Element varDec = doc.createElement(SyntaxType.VARDEC.tagName);
        subroutineBody.appendChild(varDec);

        // eat var
        eatOneToken(varDec);
        SymbolTable.Kind kind = SymbolTable.Kind.valueOf(tokenizer.getToken().toUpperCase());
        // eat type
        eatOneToken(varDec);
        String type = tokenizer.getToken();
        while (!";".equals(tokenizer.peekNextToken())) {
            if (",".equals(tokenizer.peekNextToken())) {
                eatOneToken(varDec);
            } else {
                eatIdentifier(varDec, type, kind.name());
                symbolTable.define(tokenizer.getToken(), type, kind);
            }
        }
        // for ;
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
        String subroutineCallName = "";
        // subroutineName | className | varName
        eatOneToken(doStatement);
        subroutineCallName += tokenizer.getToken();
        while (".".equals(tokenizer.peekNextToken())) {
            // .
            eatOneToken(doStatement);
            subroutineCallName += tokenizer.getToken();
            // name
            eatOneToken(doStatement);
            subroutineCallName += tokenizer.getToken();
        }

        int argsCount = 0;
        if (subroutineCallName.contains(".")) {
            String firstPart = subroutineCallName.split("\\.")[0];
            if (symbolTable.kindOf(firstPart) != null) {
                String type = symbolTable.typeOf(firstPart);
                subroutineCallName = type + subroutineCallName.substring(subroutineCallName.indexOf("."));

                // obj.method(xx) 需要将 obj 作为第一个参数 push 进去
                vmWriter.writePush(getSegmentOfIdentity(firstPart), symbolTable.indexOf(firstPart));
                // args 计数 + 1
                argsCount++;
            }
        } else { // for (this.)method
            vmWriter.writePush(VMWriter.Segment.POINTER, 0);
            subroutineCallName = fileName + "." + subroutineCallName;
            argsCount++;
        }

        // (
        eatOneToken(doStatement);
        // expressionList
        compileExpressionList(doStatement);
        // )
        eatOneToken(doStatement);
        // ;
        eatOneToken(doStatement);

        // 我们可以通过痛都 doStatement 下的 expressionList
        // 下的 expression 的 node 个数确定 arg 的数量
        Node expList = doStatement.getElementsByTagName("expressionList").item(0);
        NodeList list = expList.getChildNodes();

        for (int i = 0; i < list.getLength(); i++) {
            Node tmp = list.item(i);
            if (tmp.getNodeName() == "expression") {
                argsCount++;
            }
        }


        vmWriter.writeCall(subroutineCallName, argsCount);
        // 当处理 void method 时，按照协议需要 pop 掉最顶部的栈帧
        vmWriter.writePop(VMWriter.Segment.TEMP, 0);
    }

    public void compileLet(Element node) {
        Element letStatement = doc.createElement(SyntaxType.LETSTATEMENT.tagName);
        node.appendChild(letStatement);

        // let
        eatOneToken(letStatement);
        // varName
        //eatOneToken(letStatement);
        eatIdentifierInStatement(letStatement, "used");
        String identify = tokenizer.getToken();

        // check if need to parse expression
        boolean dealArr = false;
        if ("[".equals(tokenizer.peekNextToken())) {
            dealArr = true;
            // [
            eatOneToken(letStatement);
            // expression
            compileExpression(letStatement);
            // ]
            eatOneToken(letStatement);
            vmWriter.writePush(getSegmentOfIdentity(identify), symbolTable.indexOf(identify));
            vmWriter.writeArithmetic(VMWriter.Command.ADD);
        }
        // =
        eatOneToken(letStatement);
        // expression
        compileExpression(letStatement);
        // for ;
        eatOneToken(letStatement);

        if (dealArr) {
            vmWriter.writePop(VMWriter.Segment.TEMP, 0);
            vmWriter.writePop(VMWriter.Segment.POINTER, 1);
            vmWriter.writePush(VMWriter.Segment.TEMP, 0);
            vmWriter.writePop(VMWriter.Segment.THAT, 0);
        } else {
            vmWriter.writePop(getSegmentOfIdentity(identify), symbolTable.indexOf(identify));
        }
    }

    private VMWriter.Segment getSegmentOfIdentity(String identity) {
        SymbolTable.Kind kind = symbolTable.kindOf(identity);
        VMWriter.Segment seg;
        switch (kind) {
            case VAR:
                seg = VMWriter.Segment.LOCAL;
                break;
            case STATIC:
                seg = VMWriter.Segment.STATIC;
                break;
            case FIELD:
                seg = VMWriter.Segment.THIS;
                break;
            case ARG:
                seg = VMWriter.Segment.ARG;
                break;
            default:
                throw new RuntimeException("Unexpected kind: " + kind);
        }
        return seg;
    }

    /**
     * label L1
     * compiled (expression)
     * not
     * if-goto L2
     * compiled (statements)
     * goto L1
     * label L2
     */
    public void compileWhile(Element node) {
        Element whileStatement = doc.createElement(SyntaxType.WHILESTATEMENT.tagName);
        node.appendChild(whileStatement);
        // 保证计数从 0 开始
        String labelSuffix = (whileCounter++) + "";

        vmWriter.writeLabel(WHILE_EXP + labelSuffix);
        // while
        eatOneToken(whileStatement);
        // (
        eatOneToken(whileStatement);
        // expression
        compileExpression(whileStatement);
        // )
        vmWriter.writeArithmetic(VMWriter.Command.NOT);
        vmWriter.writeIf(WHILE_END + labelSuffix);
        eatOneToken(whileStatement);
        // {
        eatOneToken(whileStatement);
        // statements
        compileStatements(whileStatement);
        // }
        eatOneToken(whileStatement);
        vmWriter.writeGoto(WHILE_EXP + labelSuffix);
        vmWriter.writeLabel(WHILE_END + labelSuffix);
    }

    public void compileReturn(Element node) {
        Element returnStatement = doc.createElement(SyntaxType.RETURNSTATEMENT.tagName);
        node.appendChild(returnStatement);

        // return
        eatOneToken(returnStatement);
        if (!";".equals(tokenizer.peekNextToken())) {
            compileExpression(returnStatement);
        } else {
            // 如果是 return ; 这种形式，表明返回 void 类型，需要先 push constant 0
            vmWriter.writePush(VMWriter.Segment.CONST, 0);
        }
        // ;
        eatOneToken(returnStatement);
        // 最后调用写 return 的方法
        vmWriter.writeReturn();
    }

    public void compileIf(Element node) {
        Element ifStatement = doc.createElement(SyntaxType.IFSTATEMENT.tagName);
        node.appendChild(ifStatement);
        // 保证计数从 0 开始
        String ifSuffix = (ifCounter++) + "";

        // if
        eatOneToken(ifStatement);
        // (
        eatOneToken(ifStatement);
        // expression
        compileExpression(ifStatement);
        // )

        // if-else 代码块模版
        // compiled (expression)
        vmWriter.writeIf(IF_TRUE + ifSuffix);
        vmWriter.writeGoto(IF_FALSE + ifSuffix);
        // if-goto IF_TRUE
        // label IF_TRUE
        // compiled (statements1)
        // goto IF_END
        // label IF_FALSE
        // compiled (statements2)
        // label IF_END

        eatOneToken(ifStatement);
        // {
        vmWriter.writeLabel(IF_TRUE + ifSuffix);
        eatOneToken(ifStatement);
        // statements
        compileStatements(ifStatement);

        vmWriter.writeGoto(IF_END + ifSuffix);
        // }
        eatOneToken(ifStatement);
        // check else condition
        vmWriter.writeLabel(IF_FALSE + ifSuffix);
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
        vmWriter.writeLabel(IF_END + ifSuffix);
    }

    public void compileExpression(Element node) {
        Element exp = doc.createElement(SyntaxType.EXPRESSION.tagName);
        node.appendChild(exp);

        compileTerm(exp);
        // op +-*/&\>=< >= <=
        while (tokenizer.peekNextToken().matches("\\+|-|\\*|/|&|\\||>|=|<|>=|<=")) {
            eatOneToken(exp);
            String sign = tokenizer.getToken();

            compileTerm(exp);

            // 根据符号选择计算方式
            if (sign.equals("+")) {
                vmWriter.writeArithmetic(VMWriter.Command.ADD);
            } else if (sign.equals("-")) {
                vmWriter.writeArithmetic(VMWriter.Command.SUB);
            } else if (sign.equals("*")) {
                vmWriter.writeArithmetic(VMWriter.Command.MUT);
            } else if (sign.equals("/")) {
                vmWriter.writeArithmetic(VMWriter.Command.DIV);
            } else if (sign.equals("&")) {
                vmWriter.writeArithmetic(VMWriter.Command.AND);
            } else if (sign.equals("|")) {
                vmWriter.writeArithmetic(VMWriter.Command.OR);
            } else if (sign.equals(">")) {
                vmWriter.writeArithmetic(VMWriter.Command.GT);
            } else if (sign.equals("=")) {
                vmWriter.writeArithmetic(VMWriter.Command.EQ);
            } else if (sign.equals("<")) {
                vmWriter.writeArithmetic(VMWriter.Command.LT);
            } else {
                throw new RuntimeException("Illegal operation: " + sign);
            }
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
            String operation = tokenizer.getToken();
            compileTerm(term);
            if (operation.equals("-")) {
                vmWriter.writeArithmetic(VMWriter.Command.NEG);
            } else {
                vmWriter.writeArithmetic(VMWriter.Command.NOT);
            }
        } else if (nextTokenObj.getType().equals(TokenType.INT_CONST) ||
                nextTokenObj.getType().equals(TokenType.STRING_CONST) ||
                nextTokenObj.getType().equals(TokenType.KEYWORD)
        ) {
            //TokenType.INT_CONST.equals(tokenizer.tokenType(nextToken)) ||
            //        TokenType.STRING_CONST.equals(tokenizer.tokenType(nextToken)) ||
            //        TokenType.KEYWORD.equals(tokenizer.tokenType(nextToken))
            // 123 abc
            eatOneToken(term);
            // 消费完的这个 token 有多种情况，分类讨论
            String token = tokenizer.getTokenObj().getToken();
            TokenType type = tokenizer.getTokenObj().getType();
            if (type.equals(TokenType.INT_CONST)) {
                vmWriter.writePush(VMWriter.Segment.CONST, tokenizer.intVal());
            } else if (token.equals("true")) {
                vmWriter.writePush(VMWriter.Segment.CONST, 0);
                vmWriter.writeArithmetic(VMWriter.Command.NOT);
            } else if (token.equals("false") || tokenizer.getToken().equals("null")) {
                vmWriter.writePush(VMWriter.Segment.CONST, 0);
            } else if (token.equals("this")) {
                vmWriter.writePush(VMWriter.Segment.POINTER, 0);
            } else if (token.equals("that")) {
                vmWriter.writePush(VMWriter.Segment.POINTER, 1);
            } else if (token.contains("\"")) { // 字符串
                token = token.substring(1, token.length() - 1);
                vmWriter.writePush(VMWriter.Segment.CONST, token.length());
                vmWriter.writeCall("String.new", 1);
                for (char c : token.toCharArray()) {
                    vmWriter.writePush(VMWriter.Segment.CONST, c);
                    vmWriter.writeCall("String.appendChar", 2);
                }
            } else {
                SymbolTable.Kind kind = symbolTable.kindOf(token);
                VMWriter.Segment segment;
                if (kind.equals(SymbolTable.Kind.VAR)) {
                    segment = VMWriter.Segment.LOCAL;
                } else if (kind.equals(SymbolTable.Kind.ARG)) {
                    segment = VMWriter.Segment.ARG;
                } else if (kind.equals(SymbolTable.Kind.STATIC)) {
                    segment = VMWriter.Segment.STATIC;
                } else {
                    segment = VMWriter.Segment.THIS;
                }
                vmWriter.writePush(segment, symbolTable.indexOf(token));
            }
        } else if (TokenType.IDENTIFIER.equals(nextTokenObj.getType())) {
            //eatOneToken(term);
            eatIdentifierInStatement(term, "used");
            String curIdentify = tokenizer.getToken();

            String nextToken2 = tokenizer.peekNextToken();
            // name[]
            if ("[".equals(nextToken2)) {
                // [
                eatOneToken(term);

                // expression
                compileExpression(term);
                // ]
                eatOneToken(term);
                vmWriter.writePush(getSegmentOfIdentity(curIdentify), symbolTable.indexOf(curIdentify));
                vmWriter.writeArithmetic(VMWriter.Command.ADD);
                vmWriter.writePop(VMWriter.Segment.POINTER, 1);
                vmWriter.writePush(VMWriter.Segment.THAT, 0);
            } else if (".".equals(nextToken2) || "(".equals(nextToken2)) {
                // 当后一个是 token 是 '.' 时，那就是 method 调用或者 function 调用了
                // 如果是 method 调用，需要先把对象 push 进去，在 push arg, 然后 call
                // 如果是 function 调用，push arg 然后 call
                // * name.sub(expList);
                // * let value = Memory.peek(8000);
                // * 还有一种情况，调用自己内部定义的方法 sub(1);

                // 通过查看 symbolTable 中是否有当前变量判断它是不是 method call
                int argsCount = 0;
                String methodName = "";
                if (".".equals(nextToken2)) {
                    // 好像语法目前已知的方法调用只有一层
                    // .xxx
                    // for .
                    eatOneToken(term);
                    methodName += tokenizer.getToken();
                    // for xxx
                    eatOneToken(term);
                    methodName += tokenizer.getToken();
                }

                // push object if necessary
                SymbolTable.Kind curIdentifyKind = symbolTable.kindOf(curIdentify);
                if (curIdentifyKind != null && curIdentifyKind.equals(SymbolTable.Kind.FIELD)) {
                    vmWriter.writePush(VMWriter.Segment.THIS, symbolTable.indexOf(curIdentify));
                    argsCount++;
                } else if (curIdentifyKind != null && (
                        curIdentifyKind.equals(SymbolTable.Kind.VAR) ||
                                curIdentifyKind.equals(SymbolTable.Kind.ARG) ||
                                curIdentifyKind.equals(SymbolTable.Kind.STATIC)
                )) {
                    vmWriter.writePush(getSegmentOfIdentity(curIdentify), symbolTable.indexOf(curIdentify));
                    argsCount++;
                }


                // (
                eatOneToken(term);
                // expList
                compileExpressionList(term);
                // 计算 arg 个数
                Node expList = term.getElementsByTagName("expressionList").item(0);

                NodeList list = expList.getChildNodes();
                int nodeCount = 0;
                for (int i = 0; i < list.getLength(); i++) {
                    Node tmp = list.item(i);
                    if (tmp.getNodeName() == "expression") {
                        nodeCount++;
                    }
                }
                argsCount += nodeCount;
                // )

                eatOneToken(term);

                String callName;
                if (curIdentifyKind == null) {
                    callName = curIdentify + methodName;
                } else {
                    callName = symbolTable.typeOf(curIdentify) + methodName;
                }

                // 写 call
                vmWriter.writeCall(callName, argsCount);
            } else {
                vmWriter.writePush(getSegmentOfIdentity(curIdentify), symbolTable.indexOf(curIdentify));
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
