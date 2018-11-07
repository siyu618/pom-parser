package com.didi.sec.pom.parser;

import lombok.extern.slf4j.Slf4j;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;

import java.io.File;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static com.didi.sec.pom.parser.PomConstants.NAMESPACE;
import static com.didi.sec.pom.parser.PomConstants.SNAPSHOT;

@Slf4j
public class PomUtils {

    public static String getXMLNameSpaceFixed(String xpath)
    {
        xpath= xpath.replaceAll("/(\\w)", "/"+ NAMESPACE +":$1");//replace start with "/"
        xpath= xpath.replaceAll("^(\\w)", NAMESPACE + ":$1");    //replace start with word
        return xpath;
    }

    public static String removeSnapshot(String version)
    {
        if (version.endsWith(SNAPSHOT)) {
            return version.substring(0, version.length() - SNAPSHOT.length() );
        }
        version= version.replaceAll(".*" +  SNAPSHOT +  "$","$1");    //replace start with word
        return version;
    }

    public static boolean saveDocumentToFile(Document document, String outFile) {
        try {
            FileOutputStream out = new FileOutputStream(outFile);
            OutputFormat format = OutputFormat.createPrettyPrint();
            format.setEncoding("UTF-8");
            XMLWriter writer = new XMLWriter(out, format);
            writer.write(document);
            writer.close();
            return true;
        } catch (Exception err) {
            log.error("save failed.", err);
            return false;
        }
    }

    public static String getNamespace(String file) throws DocumentException {
        SAXReader sr = new SAXReader();
        Document document = sr.read(new File(file));
        return document.getRootElement().getNamespaceURI();
    }

    public static Document loadDocument(String pomFile) throws DocumentException {
        SAXReader sr = new SAXReader();
        Map<String, String> map = new HashMap<>();
        map.put(NAMESPACE, getNamespace(pomFile));
        sr.getDocumentFactory().setXPathNamespaceURIs(map);
        Document document = sr.read(new File(pomFile));
        return document;
    }

    private static String getUniquePathValue(Document document, String xPath) {
        if (Objects.isNull(xPath)) {
            return null;
        }
        Element element = (Element)document.selectSingleNode(getXMLNameSpaceFixed(xPath));
        if (Objects.isNull(element)) {
            return null;
        }
        return element.getTextTrim();
    }
    public static PomData getPomData(Document document) {
        String groupId = getUniquePathValue(document, PomConstants.GROUP_ID_PATH);
        String artifactId = getUniquePathValue(document, PomConstants.ARTIFACT_ID_PATH);
        String version = getUniquePathValue(document, PomConstants.VERSION_PATH);
        String packaging = getUniquePathValue(document,PomConstants.PACKING_PATH);
        return PomData.builder().groupId(groupId).artifactId(artifactId).version(version).packaging(packaging).build();
    }

}
