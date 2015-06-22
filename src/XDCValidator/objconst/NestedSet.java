package XDCValidator.objconst;

import javax.xml.transform.*;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import java.io.*;
import javax.xml.parsers.*;
import org.w3c.dom.*;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.dom.DOMSource;

import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import static org.joox.JOOX.*;
import org.joox.Match;
import org.xml.sax.InputSource;
/**

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author DillaWahid
 */
public class NestedSet {

    /**
     * @param args the command line arguments
     */
    static List<String> coord = new <String>ArrayList();
    
    public static void getLeaf(String xml){
        xml = "<a><b><c></c></b></a>";
        
        String newxml ="";
        int val=0;

        StringTokenizer st = new StringTokenizer(xml, "<");
        
        while (st.hasMoreElements()) {
			//System.out.println(st.nextElement());
                        String temp = (String) st.nextElement();
                        
                        if(temp.charAt(0)=='/'){
                            temp = val+"<"+temp;
                        }
                        else{
                            temp = "<"+temp+val+",";
                        }
                        newxml =newxml+temp;
                        
                        val++;
		}
        try {     
            //System.out.println("\n\n"+newxml);
           
                getCoordinate(newxml);
        } catch (SAXException ex) {
            Logger.getLogger(NestedSet.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(NestedSet.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ParserConfigurationException ex) {
            Logger.getLogger(NestedSet.class.getName()).log(Level.SEVERE, null, ex);
        }

    }
    
    public static void getCoordinate(String xml) throws SAXException, IOException, ParserConfigurationException {
        
            //xml = "<a>0,<b>1,<c>2,3</c>4</b>5</a>";
            
            while(xml!=""){
            org.w3c.dom.Document doc = null;
            DocumentBuilderFactory factory = DocumentBuilderFactory
                    .newInstance();
            factory.setNamespaceAware(true);
            DocumentBuilder builder;
            builder = factory.newDocumentBuilder();
            doc = (org.w3c.dom.Document) builder.parse(new InputSource(
                    new StringReader(xml)));
            
            Document document = $(doc).document();  
                        
            Match x= $(document).find().leaf();
            xml = xml.replace(x.toString(), "");
            
            //System.out.println(xml);     
            
            if($(x).isEmpty()){
                //System.out.println("true");
                xml="";
                x = $(document);
            }
            //System.out.println($(x).content());
            
            coord.add($(x).content());

            }
            
            for(int x=0; x< coord.size(); x++){
                System.out.println(coord.get(x));
            }
            
    }
}
