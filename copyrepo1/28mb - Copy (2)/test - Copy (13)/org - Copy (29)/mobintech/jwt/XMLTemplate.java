package org.mobintech.jwt;

import java.io.*;
import java.util.*;
import org.w3c.dom.*;
import javax.xml.parsers.*;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: Afina</p>
 * @author Boris Byk
 * @version 1.0
 */

public class XMLTemplate extends Template {

    public void assignXML(String filename) throws Exception {
        File file = new File(filename);

        if (!file.exists()) {
            throw new FileNotFoundException();
        }

        assignXML(new FileInputStream(file));
    }

    public void assignXML(InputStream in) throws Exception {

        DocumentBuilderFactory fact = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = fact.newDocumentBuilder();

        Document doc = builder.parse(in);

        NodeList list = doc.getElementsByTagName("template-data");
        if (list.getLength() == 0) {
            throw new IllegalArgumentException();
        }

        assignXMLNode("", list.item(0));
    }

    public void assignXMLNode(String handle, Node node) {
        Hashtable row = new Hashtable();
        NodeList list = node.getChildNodes();

        int n = list.getLength();
        for (int i = 0; i < n; i++) {
            Node thisNode = list.item(i);

            if (thisNode instanceof Element) {

                if (thisNode.hasChildNodes()) {
                    Node child = thisNode.getFirstChild().getNextSibling();

                    if (child != null && child instanceof Element) {
                        assignXMLNode(handle.equals("")?thisNode.getNodeName():handle + "." + thisNode.getNodeName(), thisNode);
                    } else {

                        String name = thisNode.getNodeName();
                        String value = thisNode.getFirstChild().getNodeValue();

                        row.put(name, value);
                    }
                }
            }
        }

        assignBlockVars(handle, row);
    }
}