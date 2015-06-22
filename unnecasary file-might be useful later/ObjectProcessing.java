/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package XDCValidator.objconst;

/**
 *
 * @author DillaWahid
 */

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.basex.query.XSLTemplate;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import static org.joox.JOOX.*;
import org.joox.*;
import org.xml.sax.SAXException;

import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;
import org.apache.xpath.XPathAPI;
import org.w3c.dom.Document;
//import com.sun.org.apache.xpath.internal.XPathAPI;

public class ObjectProcessing {
    
        public static String rootNode ="";
    
        public static List<String> getXPath(String f)throws SAXException, SAXException, IOException{
            String path =null;

            // Parse the document from a file
            String xmlFile = f;
            //DOMSource domSource = new DOMSource(doc)

            //get all xpath of new subtree
            File file = new File(xmlFile);
            //Document doc = db.parse(file);
            Document document = $(file).document();   // Wrap the document with the jOOX API
            List<String> list = $(document).xpath("//*").map(new Mapper<String>() {
                public String map(Context context) {
                  return $(context).xpath();
                }
            });
            
 
            return list;
            //Match x1 = $(document);
            //Match x1 = $(document).find(ele);
           
        }
	
	public static final void prettyPrint(Document xml, String filename) throws Exception {
		Transformer tf = TransformerFactory.newInstance().newTransformer();
		tf.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
		tf.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
		tf.setOutputProperty(OutputKeys.INDENT, "yes");
		Writer out = new StringWriter();
		tf.transform(new DOMSource(xml), new StreamResult(out));
		//System.out.println(out.toString());
                
                FileWriter fstream = new FileWriter(filename);
                BufferedWriter wrt = new BufferedWriter(fstream);

                    //System.out.println("contain"+xmlString.contains("&quot;"));
                    //xmlString = format(xmlString);

                wrt.write(out.toString());
                wrt.close();
		
	}
	
	public static void Main(Object[] args) throws Exception {
		
            String rootpath= (String) args[0]; 
            ArrayList<String> objpath = (ArrayList<String>)args[1];
            String inputXML = (String) args[2];
            int index = (Integer) args[3];
            
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setValidating(false);
            DocumentBuilder db = dbf.newDocumentBuilder();

            Document doc1 = db.parse(new FileInputStream(new File(inputXML)));
            Document doc2 = db.newDocument();

            XPathFactory factory = XPathFactory.newInstance();
            XPath xpath = factory.newXPath();
            Node node = (Node) xpath.evaluate(rootpath, doc1, XPathConstants.NODE);

            rootNode =node.getNodeName();
            //System.out.println(node.getNodeName());

            NodeList list = doc1.getElementsByTagName(node.getNodeName());
            Element element = (Element) list.item(0);

            // Imports a node from another document to this document, without altering 
        // or removing the source node from the original document
            Node copiedNode = doc2.importNode(element, true);

            // Adds the node to the end of the list of children of this node
            doc2.appendChild(copiedNode);
            
            //set the file name of each constraint obj
            String objfilename = "ConstraintData/"+"objsubtree_"+index+".xml";
            //System.out.println("Copy of subtree...");
            prettyPrint(doc2, objfilename); //copy to new file name objsubtree_x.xml

            
            List<String> xplist = getXPath(objfilename);
            
            /*
            System.out.println("XML xpath list");
            for(int x=0; x< xplist.size();x++){
                System.err.println(xplist.get(x));
            }          */
            
            //normalize xplist-sebab lepas copy menjadi subtree, xpath tak sama macam yang ori dalam list
            String n = xplist.get(0).substring(0, 6);
            //System.err.println(">>"+n);
            int ind = objpath.get(0).indexOf(n);
            //System.err.println(">>"+ind);
            String toRep = objpath.get(0).substring(0, ind);
            //System.err.println(">>"+toRep);
            
            List<String> objpathnew = (ArrayList<String>) normalizePath(objpath, toRep);
            
            /*
            System.out.println("Root :"+rootpath);
            System.out.println("Constraint Path...");
            for(int x=0; x<objpathnew.size();x++){
                 System.out.println(objpathnew.get(x));

            }  */         
            
	}
     
        //xpath normalization
        public static List<String> normalizePath(List<String> objpath, String toRep){
            
            List<String> objnew = new ArrayList<String>();
            for(int x =0; x< objpath.size(); x++){
                //int ind = objpath.get(x).indexOf(n);
                
                //String toRep = objpath.get(x).substring(0, ind);
                String Rep = objpath.get(x);
                Rep = Rep.replace(toRep, "");
                //System.out.println("++"+Rep);
                objnew.add(x, Rep);
            }
            return objnew;
        }
        

}