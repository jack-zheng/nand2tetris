package com.jk.translator;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.FileFilter;
import java.util.Objects;

public class JackAnalyzer {
    public static void main(String[] args) throws Exception {
        String baseDir = Objects.requireNonNull(JackAnalyzer.class.getResource(".")).getPath();
        String folderPath = "/files/Square/";
        String fileName = "Main.jack";
        String outputFile = fileName.split("\\.")[0] + "_Tokenizer.xml";
        //JackTokenizer tokenizer = new JackTokenizer(new File(baseDir + folderPath + fileName));

        //Document doc = getTokenizerDocument(tokenizer);
        //writeDoc2Xml(doc, baseDir + folderPath + outputFile, false);

        //CompilationEngine engine = new CompilationEngine(new File(baseDir + folderPath + fileName));
        //writeDoc2Xml(engine.getParsedDocument(), baseDir + folderPath + outputFile, true);

        parseDir(baseDir + folderPath);
    }

    public static void parseDir(String path) throws TransformerException {
        File dir = new File(path);
        if (!dir.isDirectory()) {
            throw new RuntimeException("give a folder path, please");
        }

        FileFilter filter = pathname -> pathname.getName().lastIndexOf(".jack") > 0;

        for (File var : Objects.requireNonNull(dir.listFiles(filter))) {
            String jackFile = var.getAbsolutePath();
            CompilationEngine engine = new CompilationEngine(new File(jackFile));
            String outPath = jackFile.substring(0, jackFile.lastIndexOf(".jack")) + "_my.xml";
            System.out.println(outPath);
            writeDoc2Xml(engine.getParsedDocument(), outPath, true);
        }
    }

    public static Document getTokenizerDocument(JackTokenizer tokenizer) throws ParserConfigurationException {
        DocumentBuilderFactory documentFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder documentBuilder = documentFactory.newDocumentBuilder();

        Document document = documentBuilder.newDocument();
        Element root = document.createElement("tokens");
        document.appendChild(root);


        while (tokenizer.hasMoreTokens()) {
            tokenizer.advance();

            Element node = document.createElement(tokenizer.tokenType().getTagName());
            node.setTextContent(" " + tokenizer.getToken() + " ");
            root.appendChild(node);
        }

        return document;
    }

    /**
     * Token 测试的时候不需要缩进，语法分析的时候需要 2 格缩进
     *
     * @param doc
     * @param xmlFilePath
     * @param indent
     * @throws TransformerException
     */
    public static void writeDoc2Xml(Document doc, String xmlFilePath, boolean indent) throws TransformerException {
        // create the xml file
        //transform the DOM Object to an XML File
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        // 解析语法的时候设置为 true, 每个节点增加两格锁进
        if (indent) {
            transformerFactory.setAttribute("indent-number", 2);
        }
        Transformer transformer = transformerFactory.newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        // <tag></tag> instead of <tag/>
        transformer.setOutputProperty(OutputKeys.METHOD, "html");
        // 去除 xml 声明
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");

        DOMSource domSource = new DOMSource(doc);
        StreamResult streamResult = new StreamResult(new File(xmlFilePath));

        // If you use
        // StreamResult result = new StreamResult(System.out);
        // the output will be pushed to the standard output ...
        // You can use that for debugging

        transformer.transform(domSource, streamResult);
        System.out.println("Done creating XML File");
    }
}
