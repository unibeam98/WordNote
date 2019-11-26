package com.example.word;


import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.SAXParserFactory;

public class JinShanXMLparser {
    public SAXParserFactory factory = null;
    public XMLReader reader = null;

    public JinShanXMLparser(){
        try {
            factory = SAXParserFactory.newInstance();
            reader = factory.newSAXParser().getXMLReader();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void parseJinShanXml(DefaultHandler content, InputSource isSource){
        if(isSource == null)
            return;
        try{
            reader.setContentHandler(content);
            reader.parse(isSource);
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
