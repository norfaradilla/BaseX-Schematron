/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package XDCValidator.objconst;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.basex.util.Array;
import org.joox.Match;
import org.w3c.dom.Document;
import static org.joox.JOOX.*;
import org.joox.Match;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import XDCValidator.util.*;
import XDCValidator.objconst.*;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.xml.sax.InputSource;


/**
 *
 * @author DillaWahid
 */
public class ValidateCumulative {
    static List XL, OL; static String XR, OR; static String Xf;
    public static String inputXML= System.getProperty("user.dir")+"/XMLdata/fitness1.xml";

    public static  String valresult;
    public static double threshold =0.2;
    public static int constviol;
    public static double distance;
    public static  boolean distReq;
    public static  long time2;
    /*
     * XList : list of XML path
     * XRoot : root of XList
     * Xfilename : XML subtree file name(only related) with update
     */
    public static List Main(List XList, String XRoot, String Xfilename , List OList, String ORoot) throws SAXException, IOException{
        
        List res = new ArrayList();
        XL= XList; XR= XRoot; Xf= Xfilename; OL =  OList; OR = ORoot;      
        
        XRoot= "/"+XRoot;
        ORoot= "/"+ORoot;
       
        
        //-----------------------------------------
        File file = new File(Xfilename);
        Document document = $(file).document();   // Wrap the document with the jOOX API
        Match x1;
        //Match x1 = $(document).xpath("/FitnessCenter[1]/Member[1]");
        //(for int x=0; x<list.size(); x++){
        x1 = $(document).xpath(XRoot);
       
        String n= $(x1).tag();
        
        //System.out.println(n);
        
        String O=null;
        if(Xfilename.contains("delsubtree")){
            O= "delete";
        }
        else if(Xfilename.contains("repsubtree")){
            O= "replace";
        }
        
        //checking whether isLeaf or isNotLeaf
        if(n.toString() =="[]"){
            String path =XRoot;
            //System.out.println("is Leaf");
            processIsLeaf(O, path); //if root is a leaf node--> getSOS() ans evaluateSOS()
                                    //result yes/no/depends
        }
        else{
            //System.out.println("is Not Leaf"); 
            processIsNotLeaf(O);  //if root is not a leaf node
        }     
        time2 = System.currentTimeMillis();
        res.add(valresult);
        res.add(threshold);
        res.add(constviol);
        res.add(distReq);
        res.add(distance);
        res.add(time2);
        
        return res;
  
    }
    
    public static double processIsLeaf(String O, String path) throws SAXException, IOException{
        
        double sDist=0;
        String[] SOS = getSOS(O, path); //calling for method getSOS()
        
        String SOSresult;
        String S = SOS[0], S_ = SOS[2];
        O= SOS[1];
        
        if(SOS ==null){
            //System.out.println("No match constraint pattern");
        }
        else{
            SOSresult= evaluateSOS(S,O,S_);
            
            if(SOSresult =="yes"){
                //System.out.println("No match constraint pattern\nACCEPT UPDATE");
                valresult = "PASSED";
                distReq = false;
                distance = -1;
            }
            else if(SOSresult =="no"){
                //System.out.println("Match constraint pattern\nREJECT UPDATE");
                valresult = "FAILED";
                distReq = false;
                distance = -1;
            }
            else if(SOSresult =="depends"){
                 
                if(O == "replace"){
                    try {
                            valresult = "PASSED";
                            distReq = true;
                            distance = -1;
                            
                            String s1 = getLeafValue(path); //s1 = xml path get value from original document
                                                            //s2 = xml path get new replacing value from delta file
                            if(!s1.equals("empty")){
                                 //***process compare string function
                            //s1 and s2 adalah xpath+value
                            //------LevenshteinDistance.computeDistance(s1, s2);
                                //double strdis =
                            }                           

                        } catch (XPathExpressionException ex) {
                            Logger.getLogger(ValidateCumulative.class.getName()).log(Level.SEVERE, null, ex);
                        } catch (FileNotFoundException ex) {
                            Logger.getLogger(ValidateCumulative.class.getName()).log(Level.SEVERE, null, ex);
                        }//do string comparison function
                    
                }
                else{
                    //System.out.println("REJECT UPDATE");
                    valresult = "FAILED";
                    distReq = false;
                    distance = -1;
                }
                    
                //***********************************
            }
        }
        return sDist;
    }
    public static String getLeafValue(String path) throws XPathExpressionException, FileNotFoundException {
      XPathFactory factory = XPathFactory.newInstance();
      XPath xPath = factory.newXPath();

      //String path = "/FitnessCenter/Member[1]/Name";

      Element result = (Element) xPath.evaluate(path , new InputSource(
                    new FileReader(Xf)), XPathConstants.NODE);
      String value= null;
      value = result.getFirstChild().getNodeValue();
      if(value.contains("      ")){
          path ="empty";
      }
      else{
          path = path + "/"+ value;
          //System.out.println(path);
      }

      return path;
      

    }
  
    
    public static double processIsNotLeaf(String O) throws SAXException, IOException{
        //1.define the same root of both subtree , XML and constraint - XYAH KOT
        //2.get all label of both subtree
        //3.for all the label i, getSOS() -->evaluateSOS()
        //4. result same as in isLeaf() method(except for depends)
        //5. if depends calculateDistace()
        
        //XL= XList; XR= XRoot; Xf= Xfilename; OL =  OList; OR = ORoot;
        
        //2. get label
        NodeList list = getLabel("objsubtree.xml");
        List<String> olabel = new <String>ArrayList();
        for (int i=0; i<list.getLength(); i++) {
            // Get element
            Element element = (Element)list.item(i);
            
            olabel.add(element.getNodeName().toString());
            //System.out.println(element.getNodeName());
         }
        
        double sDist =0;
        double childDist=0;
        
        //System.out.println("\n----------------------------\nLABEL:");
        
        //3/4/5. for all xpath of label , search xpath in XML document 'Xf'
        int x=0;
        
        outerloop:
        while( x < olabel.size()){
            //
            //System.out.println("\n----------------------------\nLABEL:"+olabel.get(x));

            File fileO = new File("objsubtree.xml");
            Document documentO = $(fileO).document();                          
            //List<String> pathO = $(documentO).find().children(olabel.get(x).toString()).xpaths();  
            List<String> pathO =$(documentO).find().matchTag(olabel.get(x).toString()).xpaths();
            
            File file = new File(Xf);
            Document documentX = $(file).document();                           
            List<String> pathX = $(documentX).find().matchTag(olabel.get(x).toString()).xpaths();

            //double[][] childDist = new double[pathO.size()][pathX.size()];
            for(int y=0; y<pathO.size();y++ ){
                for(int z=0; z< pathX.size();z++){
                    
                    //System.out.println(pathO.size()+" "+pathX.size());
                    
                    String SOSresult;
                    String[] SOS = getSOS(O, pathX.get(z)); //calling for method getSOS()
                    String S = SOS[0], S_ = SOS[2];
                    O= SOS[1];

                    if(SOS[0]==null||SOS[2]==null){
                        S = null;  S_= null;
                    }
                    
                    if(S!= null && S_!=null){
                        SOSresult= evaluateSOS(S,O,S_);
                        //System.out.println(SOSresult);
                    
                        if(SOSresult =="yes"){
                            //System.out.println(pathX.get(y));
                            //System.out.println("No match constraint pattern\nACCEPT\n");
                            
                            sDist=100; //BAIKI!!!!
                            
                            valresult = "PASSED";
                            distReq = false;
                            distance = -1;
                            break outerloop;
                        }
                        else if(SOSresult =="no"){
                            //System.out.println(pathX.get(y));

                            //System.out.println("Match constraint pattern\nREJECT\n");
                            
                            valresult = "FAILED";
                            distReq = true;
                            distance = -1;
                            sDist=-1;//BAIKI!!!!
                            break outerloop;
                        }
                        else if(SOSresult =="depends"){
                        
                            //System.out.println("depends");

                            String[] args = new String[4];
                            
                            //****if leaf node, compute string distance
                            //s1 and s2 adalah xpath+value
                            //---------LevenshteinDistance.computeDistance(s1, s2);
                            
                            //args adalah current xpath and filename for both xml and object(to be compared)
                            args[0] = Xf; 
                            args[1] = pathX.get(z);
                            args[2] = "objsubtree.xml"; 
                            args[3] = pathO.get(y);

                            try{
                                childDist =RTEDCommandLine.Main(args);
                                sDist =childDist+sDist;
             
                                //distReq = true;
                                

                            }catch(Exception e){
                                System.err.print("Error");
                            }
                        }
                    }//end if 
                    
                    else{
                    //BUAT STRUCTURAL DISTANCE KAT SINI JUGA!!!!!!!!!!!!!!!!
                   String[] args = new String[4];
                   args[0] = Xf; 
                   args[1] = pathX.get(z);
                   args[2] = "objsubtree.xml"; 
                   args[3] = pathO.get(y);


                   try{
                       childDist =RTEDCommandLine.Main(args);
                       sDist =childDist+sDist;
                       distance = sDist;
                       
                       if(distance >= threshold){
                           valresult = "FAILED";
                       }
                       else{
                           valresult = "PASSED";
                       }
                       
                       distReq = true;
                       
                       //childDist[y][z] =sDist;
                       //System.out.println("sDist:"+sDist);
                       //System.out.println("Distance:"+sDist);

                   }catch(Exception e){
                       System.err.print("Error");
                   }
                        
                   }//end else
                    
                }//end z loop
            }//end y loop
            
             
            //HungarianAlgo

            x++;
            
        }
        
        //System.out.println("\nSDIST:"+sDist);
        
        //System.out.println("----------------------------------\nEND OF CUMULATIVE CONSTRAINT CHECKING");
        
        return sDist;    
                      
    }   
    
    public  static NodeList getLabel(String Of) throws SAXException, IOException{
        
         List label = new ArrayList();
          //element;
         NodeList list = null;
         try {

            String xmlFile = Of;
            File file = new File(xmlFile);
            if(file.exists()){
                // Create a factory
                DocumentBuilderFactory factory = 
                  DocumentBuilderFactory.newInstance();
                // Use the factory to create a builder
                DocumentBuilder builder = factory.newDocumentBuilder();
                Document doc = builder.parse(xmlFile);
                // Get a list of all elements in the document
                list = doc.getElementsByTagName("*");
                //System.out.println("XML Elements: ");
               
            }
            else{
                System.out.print("File not found!");
            }
        }
        catch (Exception e) {
            System.exit(1);
        }
         
        //label = (List) list;
        
        return list;
    }
     
     
   
    public static String[] getSOS(String O, String path) throws SAXException, IOException{
        //get the matching SOS, match from update list and constraint list
        
        //System.err.println(path);
        //String[] SOS = new String[3];
        //String SOS=null;
        String S=null, S_= null;
        
        File file = new File(Xf);
        Document document = $(file).document();   
        Match x1 = $(document).xpath(path);
        String x2 = $(x1).tag();
        
        //find S_
        File objfile = new File("schematronOri.xml");
        Document objdocument = $(objfile).document();
        
        String str = "context=\""+x2+"\"";
        
        Match y1= $(objdocument).find().children("rule");
              
        StringTokenizer st = new StringTokenizer(y1.toString(),",");
        while(st.hasMoreTokens()) { 
            String token =st.nextToken();
            
            if(token.contains(str)&&(token.contains("role"))){
                String tokennew= token.substring(token.indexOf("role"));        
                S_= (String) tokennew.subSequence(tokennew.indexOf("\"")+1, tokennew.lastIndexOf("\""));
            }
                
        }
        
        //find S
        S = $(objdocument).children().content(x2).attr("id");

        //**********OBJECT OVERLAPPING BELOM HANDLE LAGI!!!!!!!!!!!!!!************

        
        //return SOS
        String SOS[] = {S, O, S_ };
        return SOS;
        
    }
    
    public static String evaluateSOS(String SS, String O, String S_){
        String result = null;
        String S = null;
        //System.out.println(SS+">"+O+">"+S_);
        
        if(SS.equals("object_low")){
            S= "iii";
        }
        else if(SS.equals("object_med")){
            S="ii";
        }
        else if(SS.equals("object_high")){
            S="i";
        }
        //System.err.println(S+">"+O+">"+S_);
        
        if(O.equals("replace")){
            if((S_.equals("mandatory")) &&(S.equals("i"))){result="no"; }
            else if((S_.equals("mandatory"))&&(S.equals("ii"))){result="yes";} //nanti betolkan balik semula
            else if((S_.equals("mandatory"))&&(S.equals("iii"))){result="no"; }
            else if((S_.equals("optional"))&&(S.equals("i"))){result="no"; }
            else if((S_.equals("optional"))&&(S.equals("ii"))){result="depends"; }
            else if((S_.equals("optional"))&&(S.equals("iii"))){result="no"; }
                        
        }
        else if(O.equals("delete")){
            if((S_.equals("mandatory"))&&(S.equals("i"))){result="no"; }
            else if((S_.equals("mandatory"))&&(S.equals("ii"))){result="no"; }
            else if((S_.equals("mandatory"))&&(S.equals("iii"))){result="yes"; }
            else if((S_.equals("optional"))&&(S.equals("i"))){result="no"; }
            else if((S_.equals("optional"))&&(S.equals("ii"))){result="no";}
            else if((S_.equals("optional"))&&(S.equals("iii"))){result="depends"; }
        }
  
        //System.out.println(result);
        return result;   
    }

    
}
