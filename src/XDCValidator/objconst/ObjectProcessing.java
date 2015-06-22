/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
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
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.dom.DOMSource;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.basex.query.XSLTemplate;
import org.joox.Context;

import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import static org.joox.JOOX.*;
import org.joox.Mapper;
import org.joox.Match;
import org.xml.sax.InputSource;
/**
 *
 * @author DillaWahid
 */
public class ObjectProcessing{

    /**
     * @param args the command line arguments
     */
 
    public static String rootNode ="";
    static List<List<String>> objpath = new ArrayList<List<String>>();//list of [objects][paths]
    static ArrayList rootObj = new ArrayList();
    public static String inputXML= System.getProperty("user.dir")+"/XMLdata/fitness1.xml";
    
    static List<String> objReturn=null;

    public static String getElementXpath(String ele) throws SAXException, SAXException, IOException{
        String path =null;
        // Parse the document from a file
        String xmlFile = inputXML;
      
        //DOMSource domSource = new DOMSource(doc)
        
        File file = new File(xmlFile);
        //Document doc = db.parse(file);
        Document document = $(file).document();   // Wrap the document with the jOOX API
        //Match x1 = $(document);
        Match x1 = $(document).find(ele);
        path = $(x1).xpath();
       
        //System.out.println(ele);
        return path;
    }
    
    
    public static String getFullXPath(Node n) {
        // abort early
        if (null == n)
          return null;

        // declarations
        Node parent = null;
        Stack<Node> hierarchy = new Stack<Node>();
        StringBuffer buffer = new StringBuffer();

        // push element on stack
        hierarchy.push(n);

        switch (n.getNodeType()) {
        case Node.ATTRIBUTE_NODE:
          parent = ((Attr) n).getOwnerElement();
          break;
        case Node.ELEMENT_NODE:
          parent = n.getParentNode();
          break;
        case Node.DOCUMENT_NODE:
          parent = n.getParentNode();
          break;
        default:
          throw new IllegalStateException("Unexpected Node type" + n.getNodeType());
        }

        while (null != parent && parent.getNodeType() != Node.DOCUMENT_NODE) {
          // push on stack
          hierarchy.push(parent);

          // get parent of parent
          parent = parent.getParentNode();
        }

        // construct xpath
        Object obj = null;
        while (!hierarchy.isEmpty() && null != (obj = hierarchy.pop())) {
          Node node = (Node) obj;
          boolean handled = false;

          if (node.getNodeType() == Node.ELEMENT_NODE) {
            Element e = (Element) node;

            // is this the root element?
            if (buffer.length() == 0) {
              // root element - simply append element name
              buffer.append(node.getNodeName());
            } else {
              // child element - append slash and element name
              buffer.append("/");
              buffer.append(node.getNodeName());

              if (node.hasAttributes()) {
                // see if the element has a name or id attribute
                if (e.hasAttribute("id")) {
                  // id attribute found - use that
                  buffer.append("[@id='" + e.getAttribute("id") + "']");
                  handled = true;
                } else if (e.hasAttribute("name")) {
                  // name attribute found - use that
                  buffer.append("[@name='" + e.getAttribute("name") + "']");
                  handled = true;
                }
              }

              if (!handled) {
                // no known attribute we could use - get sibling index
                int prev_siblings = 1;
                Node prev_sibling = node.getPreviousSibling();
                while (null != prev_sibling) {
                  if (prev_sibling.getNodeType() == node.getNodeType()) {
                    if (prev_sibling.getNodeName().equalsIgnoreCase(
                        node.getNodeName())) {
                      prev_siblings++;
                    }
                  }
                  prev_sibling = prev_sibling.getPreviousSibling();
                }
                buffer.append("[" + prev_siblings + "]");
              }
            }
          } else if (node.getNodeType() == Node.ATTRIBUTE_NODE) {
            buffer.append("/@");
            buffer.append(node.getNodeName());
          }
        }
        // return buffer
        return buffer.toString();
    }          


    public static void writeFile(String yourXML) throws IOException{
        BufferedWriter out = null;
        try {
            out = new BufferedWriter(new FileWriter("schematronOri.xml"));
            out.write(yourXML);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            out.close();
        }
    }
    
    public static void SchematronXMLParser(String xmlFile){
        
       XSLTemplate template = new XSLTemplate();
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            File file = new File(xmlFile);
            
            
            
            if (file.exists()) {
                Document doc = db.parse(file);
                Element docEle = doc.getDocumentElement();
 
                NodeList patternList = docEle.getElementsByTagName("pattern");
 
                // Print total pattern elements in document
                //System.out.println("Total pattern: " + patternList.getLength()+"\n");
                
 
                if (patternList != null && patternList.getLength() > 0) {
                    
                    for (int i = 0; i < patternList.getLength(); i++) {
 
                        String idval=null; 
                        Node node = patternList.item(i);
                        List<String> row = null;
                        
                        //code untuk check sama ada pattern ada attribute bernama id. kalau ada att id, cuba dapatkan value dia.
                        if(patternList.item(i).hasAttributes()){
                            NamedNodeMap attributes = patternList.item(i).getAttributes();
                            for (int m = 0; m < attributes.getLength(); m++) {                           
              
                                 Node nodeatt = attributes.item(m);
                                 //System.err.println(attributes.item(m));
                                 if(nodeatt.getNodeName().equals("id")){
                                     row = new ArrayList<String>();
                                     idval = patternList.item(i).getAttributes().getNamedItem("id").getNodeValue();
                                 }
                            }
                                     
                        }
                                               
                        if ((node.getNodeType() == Node.ELEMENT_NODE) && (idval!=null)) {
                           
                            Element e = (Element) node;
                            NodeList nodeList = e.getElementsByTagName("rule");
                            
                            for(int j=0; j < nodeList.getLength(); j++){
                                if(idval.contains("object_")){
                                    //System.out.println("Name: "+ nodeList.item(j).getAttributes().getNamedItem("context").getNodeValue()); 
                                
                                    String ele = nodeList.item(j).getAttributes().getNamedItem("context").getNodeValue();
                                    
                                    String path= getElementXpath(ele );
                                    row.add(path);

                                }
                              
                            }                            
                        }
                        
                        //set kan paths of objectx kedalam arraylist
                        if(row!=null){
                            objpath.add(row);
                        }
                    }
                } else {
                    //System.exit(1);
                }
            }
            
            
        } catch (Exception e) {
            System.out.println(e);
        }
        
    }


    public static String mostCommonAnc(XSLTemplate template, ArrayList al){
        //String path = null;
               //--------------------------------

        String mostcomm="";

        try{
            template.setStyleSheetPath("C:/Documents and Settings/User/My Documents/NetBeansProjects/BaseX.v2/Scripts/commAncs.xsl");

            template.setTemplateName("findComm");
            Map<String, String> params = new HashMap<String, String>();

            for (int i = 0; i < al.size(); i++){
                //System.out.println("top"+i);
                String path = (String) al.get(i);
                String compath=""; 
                if(i>=2){
                    compath = mostcomm;
                }
                else{
                    compath = (String) al.get(i+1);
                }
                if(i==0){i++;}//go to index 2 ==> al[2] for next comparison

                params.put("param1", compath); 
                params.put("param2", path);

                template.setParameters(params);

                //call transformation function using Saxon, this is where is calculate
                //the most common ancestor of the xpath
                mostcomm = template.call();//call the template to calculate the most common ancestor

                //----------

                if(i!=al.size()-1){
                    mostcomm= mostcomm+ "node()"; //trim the xpath for next calculation of most common ancestor
                }
                else{
                    mostcomm = mostcomm.substring(0,mostcomm.length()-1); //trimming to get final most common ancestor
                }
                //System.out.println("Most common ancestor: "+ mostcomm+"\n");

            }
                try{
                    //System.out.println("Most common ancestor: "+ mostcomm+"\n");
                }catch(Exception e) {
                    System.out.println("Some error occured!");
                }

        }catch(Exception e){
            System.out.println("Common ancestor exception");
            
        }
        return mostcomm;
    }
    
    public static List objProcess(String[] args) throws TransformerConfigurationException, FileNotFoundException, TransformerException, IOException, XPathExpressionException, Exception {
        // TODO code application logic here
        
        Transformer xformer = TransformerFactory.newInstance().newTransformer(new StreamSource(new FileInputStream("Scripts/xform.xsl")));
        StringWriter writer = new StringWriter();
        xformer.transform(new StreamSource(new FileInputStream(args[1])), new StreamResult(writer));
        
        
        writeFile(writer.toString()); //re-write schmematon into new file in XML format name schematronObj.xml

        
        //read new XML file and search for patterns with Obj_ id and input all the path upon the original XML file into objpath[][] list
        SchematronXMLParser("schematronOri.xml");
        //objpath[][] adalah mengandungi semua path bagi setiap objects listed dalam Schematron
        for(int i=0; i< objpath.size();i++){
            XSLTemplate template = new XSLTemplate();
            ArrayList<String> al= new ArrayList();
            
            for(int j=0; j< objpath.get(i).size(); j++){
                //System.out.println(objpath.get(i).get(j));
                String toadd= objpath.get(i).get(j);
                al.add(toadd);
            }
            
            //for all paths of each object, get the root(most common ancestor)
            String path= mostCommonAnc(template,al);
            
            //alObj[] adalah root node(most common ancestor) bagi semua objects listed dalam Schematron
            rootObj.add(path);
            
             //for all alObj, get the subtree from XML file and do relabeling
        }
      
        //a call to relabeling method and then the validation
        try {
            processObj(rootObj, objpath);
        } catch (ParserConfigurationException ex) {
            Logger.getLogger(ObjectProcessing.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SAXException ex) {
            Logger.getLogger(ObjectProcessing.class.getName()).log(Level.SEVERE, null, ex);
        }
   
          return objReturn;
        
    }
    

    public static void processObj(ArrayList rootObj, List<List<String>> objpath ) throws XPathExpressionException, ParserConfigurationException, SAXException, IOException, Exception {
        
        
         DocumentBuilderFactory domFactory = 
         DocumentBuilderFactory.newInstance();
         domFactory.setNamespaceAware(true); 
         DocumentBuilder builder = domFactory.newDocumentBuilder();
         Document doc = builder.parse(inputXML);
         XPath xpath = XPathFactory.newInstance().newXPath();
         
         //1. bagi setiap root path dalam alObj, get the subtree dari original XML, copy to new XML document and all xpath of the subtree(xList[])
         //2. yList[] = objPath[x][i]
         //3. compare both
         for(int x=0; x< rootObj.size(); x++){
             Object args[]={rootObj.get(x), objpath.get(x), inputXML,x};
             MainObjectProcessing(args);
         }
         // XPath Query for showing all nodes value
         //XPathExpression expr = xpath.compile("FitnessCenter[1]/Member[1]");

        //Object result = expr.evaluate(doc);
        //System.err.println(result.toString());.
         
       
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
        
        public static void MainObjectProcessing(Object[] args) throws Exception {
		
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
            
            //List tempRootPath = new ArrayList();
            //tempRootPath.add(rootpath);
            
            objReturn = objpathnew;
            objReturn.add(rootpath);
            //objReturn.add(objpathnew);
            //objReturn.add(tempRootPath);
            
            
	}
    
    
}
