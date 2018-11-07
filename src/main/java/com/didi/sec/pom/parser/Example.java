package com.didi.sec.pom.parser;


import lombok.extern.slf4j.Slf4j;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.Node;

import java.io.IOException;
import java.util.List;


@Slf4j
public class Example {

    public static String getResourceFile(String file) {
        return Example.class.getResource("/" + file).getPath();
    }

    public static void getAllStudent() throws DocumentException, IOException {
        String file = getResourceFile("a.xml");
        System.out.println(file);
        Document document = PomUtils.loadDocument(file);

        List<Node> nodeList = document.selectNodes(PomUtils.getXMLNameSpaceFixed("/students/student"));//genXPathString(Arrays.asList("students", "student")));
        List<Node> nodeList2 = document.selectNodes(PomUtils.getXMLNameSpaceFixed("/students/student/name"));
        System.out.println(nodeList2.get(0).getText());
        for (Node node : nodeList) {
            String idv = node.valueOf("@id");
            Node name = node.selectSingleNode(PomUtils.getXMLNameSpaceFixed("name"));
            String nameVal = name.getText();
            Node age = node.selectSingleNode(PomUtils.getXMLNameSpaceFixed("age"));
            int ageVal = Integer.valueOf(age.getText());
            System.out.println(idv + "-" + nameVal + "-" + ageVal);
        }
    }

    public static void test() {
        String file = getResourceFile("simple_pom.xml");

        try {
            Document document = PomUtils.loadDocument(file);
            Element versionElement = (Element)document.selectSingleNode(PomUtils.getXMLNameSpaceFixed(PomConstants.VERSION_PATH));
            String version = versionElement.getTextTrim();
            System.out.println(version);
            versionElement.setText(PomUtils.removeSnapshot(version));

            //PomUtils.saveDocumentToFile(document, file + ".new");
            System.out.println(document.asXML());
            System.out.println(PomUtils.getPomData(document));

        } catch (DocumentException e) {
            log.error("", e);
        }
    }

    public static void main(String... args) throws DocumentException, IOException {
        getAllStudent();
        test();
        System.out.println();
    }
}
