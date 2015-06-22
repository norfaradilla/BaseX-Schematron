/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package XDCValidator.objconst;

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
import java.util.StringTokenizer;

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
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

/**
 *
 * @author DillaWahid
 */
public class UpdateQProcessing {
    
    public static String inputXML= System.getProperty("user.dir")+"/XMLdata/fitness1.xml";
    
    
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
	
        //----------VALIDATION------
    //rootOp = del/rep operation-root path
    //Oppath =list of path for each object
    public static List updateQProcess(String rootOp, List<String> Oppath, int x ) throws XPathExpressionException, ParserConfigurationException, SAXException, IOException, Exception {
        
        String root =rootOp;
         DocumentBuilderFactory domFactory = 
         DocumentBuilderFactory.newInstance();
         domFactory.setNamespaceAware(true); 
         DocumentBuilder builder = domFactory.newDocumentBuilder();
         Document doc = builder.parse(inputXML);
         //XPath xpath = XPathFactory.newInstance().newXPath();
         
         //1. bagi setiap root path dalam alObj, get the subtree dari original XML, copy to new XML document and all xpath of the subtree(xList[])
         //2. yList[] = objPath[x][i]
         //3. compare both
         
        //String rootpath= (String) args[0]; 
        //ArrayList<String> objpath = (ArrayList<String>)args[1];
        //String inputXML = (String) args[2];
        //int index = (Integer) args[3];

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setValidating(false);
        DocumentBuilder db = dbf.newDocumentBuilder();

        Document doc1 = db.parse(new FileInputStream(new File(inputXML)));
        Document doc2 = db.newDocument();

        XPathFactory factory = XPathFactory.newInstance();
        XPath xpath = factory.newXPath();
        Node node = (Node) xpath.evaluate(rootOp, doc1, XPathConstants.NODE);

        rootOp =node.getNodeName();
        //System.out.println(node.getNodeName());

        NodeList list = doc1.getElementsByTagName(node.getNodeName());
        Element element = (Element) list.item(0);

        // Imports a node from another document to this document, without altering 
    // or removing the source node from the original document
        Node copiedNode = doc2.importNode(element, true);

        // Adds the node to the end of the list of children of this node
        doc2.appendChild(copiedNode);

        String objfilename="";
        //set the file name of each constraint obj
        if(x==0){
            objfilename = "XMLUpdateData/"+"delsubtree.xml";
        }
        else if(x==1){
            objfilename = "XMLUpdateData/"+"repsubtree.xml";
        }    
        //System.out.println("Copy of subtree...");
        prettyPrint(doc2, objfilename); //copy to new file name objsubtree_x.xml


            //normalize xplist-sebab lepas copy menjadi subtree, xpath tak sama macam yang ori dalam list
        List<String> xplist = getActualPath(objfilename);

        /*
        System.out.println("XML xpath list");
        for(int x=0; x< xplist.size();x++){
            System.err.println(xplist.get(x));
        }          */

        //normalize xplist sebab xplist punya path might be diff dengan Oppath list sebab dah copy subtree
        int mark= xplist.get(0).indexOf("/");
        String n = xplist.get(0).substring(0, mark);
        //System.err.println(">>"+n);
        int ind = Oppath.get(0).indexOf(n);
        List<String> Oppathnew;
        //System.err.println(">>"+ind);
        if(ind!=0){
            String toRep = Oppath.get(0).substring(0, ind);
            //System.err.println(">>"+toRep);
            Oppathnew = (ArrayList<String>) normalizePath(Oppath, toRep);
        }
        else {
            Oppathnew= Oppath;
        }
        
        
        int m;
        for(int t=0; t<Oppathnew.size();t++){
            m= longestSubstr(root, Oppathnew.get(t));
            String todel = Oppathnew.get(t).substring(0, m);
            String temp = Oppathnew.get(t).replace(todel, "");
            //System.err.println(root);
            String temp2= root+temp;
            if(temp2.charAt(0)!= '/'){
                temp2= '/'+temp2;
            }
            int lgt =  temp2.length();
            if(temp2.charAt(lgt-1)!= ']'){
                temp2= temp2+"[1]";
            }
            Oppathnew.set(t, temp2);
            //System.out.println("Oppathnew.get(t):"+Oppathnew.get(t));
        }
       
        /*
        
        System.out.println("\nRoot :"+root);
        System.out.println("XML Path...");
        for(int t=0; t<Oppathnew.size();t++){
             System.out.println(Oppathnew.get(t));

        }
        */
        List returnXList = new ArrayList(Oppathnew);
        
        return returnXList;
        
               
    }
    
    public static int longestSubstr(String first, String second) {
        if (first == null || second == null || first.length() == 0 || second.length() == 0) {
            return 0;
        }

        int maxLen = 0;
        int fl = first.length();
        int sl = second.length();
        int[][] table = new int[fl][sl];

        for (int i = 0; i < fl; i++) {
            for (int j = 0; j < sl; j++) {
                if (first.charAt(i) == second.charAt(j)) {
                    if (i == 0 || j == 0) {
                        table[i][j] = 1;
                    }
                    else {
                        table[i][j] = table[i - 1][j - 1] + 1;
                    }
                    if (table[i][j] > maxLen) {
                        maxLen = table[i][j];
                    }
                }
            }
        }
        return maxLen;
    }
    
 
   private static void print(BufferedWriter out, Document doc) throws IOException {
        OutputFormat fmt = new OutputFormat();
        fmt.setIndenting(true);
        XMLSerializer ser = new XMLSerializer(out, fmt);
        ser.serialize(doc);
        //String write =ser.toString();
        //System.err.println(write);

    }
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
    public static List<String> getActualPath(String f)throws SAXException, SAXException, IOException{
            //String path =null;

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
}
