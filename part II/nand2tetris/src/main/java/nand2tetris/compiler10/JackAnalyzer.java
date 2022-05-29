package nand2tetris.compiler10;

import nand2tetris.vmtranslator08.Main;
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
        String filePath = "/10/StringTest";
        File source = new File(Main.class.getResource("/").getPath() + filePath);

        // for tokenizer testing
        //if (source.isDirectory()) {
        //    tokenizeFolder(source);
        //} else {
        //    tokenizeFile(source);
        //}

        // for tree parse testing
        if (source.isDirectory()) {
            parseDir(source);
        } else {
            parseFile(source);
        }
    }

    private static void tokenizeFile(File source) throws Exception {
        JackTokenizer tokenizer = new JackTokenizer(source);
        Document doc = getTokenizerDocument(tokenizer);

        String outPath = source.getParent() + "/" + source.getName().split("\\.")[0] + "_token.xml";
        System.out.println(outPath);
        writeDoc2Xml(doc, outPath, false);
    }

    private static void tokenizeFolder(File source) throws Exception {
        FileFilter filter = pathname -> pathname.getName().lastIndexOf(".jack") > 0;
        for (File jackFile : Objects.requireNonNull(source.listFiles(filter))) {
            tokenizeFile(jackFile);
        }
    }

    public static void parseFile(File file) throws TransformerException {
        String jackFile = file.getPath();
        CompilationEngine engine = new CompilationEngine(new File(jackFile));
        String outPath = file.getParent() + "/" + file.getName().split("\\.")[0] + "_tree.xml";
        writeDoc2Xml(engine.getParsedDocument(), outPath, true);
    }

    public static void parseDir(File dir) throws TransformerException {

        FileFilter filter = pathname -> pathname.getName().lastIndexOf(".jack") > 0;

        for (File var : Objects.requireNonNull(dir.listFiles(filter))) {
            parseFile(var);
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
