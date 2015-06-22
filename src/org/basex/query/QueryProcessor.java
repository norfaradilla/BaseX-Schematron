package org.basex.query;

import XDCValidator.SchtrnValidator;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.xpath.XPathExpressionException;
import static org.basex.core.Text.*;
import static org.basex.query.util.Err.*;
import static org.basex.util.Token.*;

import java.util.Map.Entry;

import org.basex.core.Context;
import org.basex.core.Progress;
import org.basex.data.Nodes;
import org.basex.data.Result;
import org.basex.io.serial.Serializer;
import org.basex.io.serial.SerializerException;
import org.basex.query.expr.Expr;
import org.basex.query.func.JavaMapping;
import org.basex.query.item.Value;
import org.basex.query.iter.Iter;
import org.basex.io.IO;


import java.io.*;
import org.w3c.dom.*;
import javax.xml.transform.*;

import org.w3c.dom.Document;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.StringWriter;


import javax.xml.namespace.QName;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.xml.sax.SAXException;

import XDCValidator.objconst.ObjectProcessing;
import XDCValidator.objconst.UpdateQProcessing;
import XDCValidator.objconst.ValidateCumulative;
import java.util.ArrayList;


import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import org.basex.BaseXGUI;

import org.w3c.dom.Node;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

//import XDCValidator.objconst.ValidateCumulative;
/**
 * This class is an entry point for evaluating XQuery implementations.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class QueryProcessor extends Progress {
  /** Expression context. */
  public final QueryContext ctx;
  /** Query. */
  private String query;
  /** Parsed flag. */
  private boolean parsed;
  /** Compilation flag. */
  private boolean compiled;
  /** Closed flag. */
  private boolean closed;

   public static ArrayList alDel = new ArrayList();
   public static ArrayList alIns = new ArrayList();
   public static ArrayList alRep = new ArrayList();
   
   public static String rootDel=null;
   public static String rootRep=null;
   
   public static long time1;
   //public static long time2;

  /**
   * Default constructor.
   * @param qu query to process
   * @param cx database context
   */
  public QueryProcessor(final String qu, final Context cx) {
    this(qu, cx.current(), cx);
  }

  /**
   * Constructor with an initial context set.
   * @param qu query
   * @param nodes initial context set
   * @param cx database context
   */
  public QueryProcessor(final String qu, final Nodes nodes, final Context cx) {
    query = qu;
    ctx = new QueryContext(cx);
    ctx.nodes = nodes;
    progress(ctx);
  }

  /**
   * Constructor with an initial context set.
   * @param qu query
   * @param o initial context expression
   * @param cx database context
   * @throws QueryException query exception
   */
  public QueryProcessor(final String qu, final Object o, final Context cx)
      throws QueryException {
    this(qu, o instanceof Expr ? (Expr) o : JavaMapping.toValue(o), cx);
  }

  /**
   * Constructor with an initial context set.
   * @param qu query
   * @param expr initial context expression
   * @param cx database context
   */
  private QueryProcessor(final String qu, final Expr expr, final Context cx) {
    query = qu;
    ctx = new QueryContext(cx);
    ctx.ctxItem = expr;
    progress(ctx);
  }

  /**
   * Parses the query.
   * @throws QueryException query exception
   */
  public void parse() throws QueryException {
    if(parsed) return;
    ctx.parse(query);
    parsed = true;
  }

  /**
   * Compiles the query.
   * @throws QueryException query exception
   */
  public void compile() throws QueryException {
    parse();
    if(compiled) return;
    ctx.compile();
    compiled = true;
  }

  /**
   * Returns a result iterator.
   * @return result iterator
   * @throws QueryException query exception
   */
  public Iter iter() throws QueryException {
    compile();
    return ctx.iter();
  }

  /**
   * Returns a result value.
   * @return result value
   * @throws QueryException query exception
   */
  public Value value() throws QueryException {
    compile();
    return ctx.value();
  }

  /**
   * Evaluates the specified query and returns the result.
   * @return result of query
   * @throws QueryException query exception
   */
  public Result execute() throws QueryException {
      
 
      
    compile();
    
   
    try {
        ///////////////------------           
        //DYNAMIC CONSTRAINT VALIDATION  
        ////////////---------------    
        
        doValidate();
        
                          
    } catch (FileNotFoundException ex) {
        Logger.getLogger(QueryProcessor.class.getName()).log(Level.SEVERE, null, ex);
    } catch (IOException ex) {
        Logger.getLogger(QueryProcessor.class.getName()).log(Level.SEVERE, null, ex);
    } catch (JDOMException ex) {
        Logger.getLogger(QueryProcessor.class.getName()).log(Level.SEVERE, null, ex);
    } catch (XPathExpressionException ex) {
        Logger.getLogger(QueryProcessor.class.getName()).log(Level.SEVERE, null, ex);
    } catch (ParserConfigurationException ex) {
        Logger.getLogger(QueryProcessor.class.getName()).log(Level.SEVERE, null, ex);
    } catch (SAXException ex) {
        Logger.getLogger(QueryProcessor.class.getName()).log(Level.SEVERE, null, ex);
    } catch (TransformerConfigurationException ex) {
        Logger.getLogger(QueryProcessor.class.getName()).log(Level.SEVERE, null, ex);
    } catch (TransformerException ex) {
        Logger.getLogger(QueryProcessor.class.getName()).log(Level.SEVERE, null, ex);
    }


    /*      
    //call Main method in SchtrnValidator 
    String[] args = new String[2];
    args[0]= file_name; //po-bad.xml
    args[1]= "fitness.sch";
	
    SchtrnValidator.Main(args);
     */       
    //--------------
        
      
      //time2= System.currentTimeMillis();
    return ctx.execute();
  }
  
  
/**
   * Transform pre_DYN.txt to dyn_data.xml
   * @param str a line in pre_DYN
   * @param doc pre-created xml document  
   * @param root root of xml document
   */
  private  static void doValidate() throws FileNotFoundException, IOException, JDOMException, XPathExpressionException, ParserConfigurationException, SAXException, TransformerConfigurationException, TransformerException{
      
          time1 = System.currentTimeMillis();
          
          BufferedReader br_ins = new BufferedReader(new FileReader("pre_ins.txt"));
          BufferedReader br_ins2 = new BufferedReader(new FileReader("pre_ins.txt"));

          String str_ins="";
          ArrayList list1 = new ArrayList();
          String [] ar1 = null;

          //System.out.println(new File("pre_ins.txt").exists());
          
          if(br_ins.readLine() != null){
               //System.out.println("Haihaihai");
               
                while((str_ins = br_ins2.readLine()) != null){
                     ar1 = str_ins.split("\\~");
                     list1.add(ar1[1]);                    
                }    
          }          
          BufferedReader br_del = new BufferedReader(new FileReader("pre_del.txt")) ;
          BufferedReader br_del2 = new BufferedReader(new FileReader("pre_del.txt")) ;

          String str_del = null;
          ArrayList list2 = new ArrayList();
          String [] ar2 = null;
          //ar2[1]="";
          
          if(br_del.readLine() != null){
                           
                while((str_del = br_del2.readLine()) != null){
                     ar2 = str_del.split("\\~");
                     
                    list2.add(ar2[1]);
                }     
              
          }
          /*
          for (int j=0;j<list1.size(); j++) {
              System.out.println("list:"+ list1.get(j));
          }*/                 
          
          DelInsPreprocess(list1, list2);
          //PRE-PROCESS TO DYN_DATA.XML
          DocumentBuilderFactory dbfac = DocumentBuilderFactory.newInstance();
          DocumentBuilder docBuilder;
          
          CummConsList();
          
          //start from here, transform pre_DYN.txt to dyn_data.xml
          BufferedReader br = new BufferedReader(new FileReader("pre_DYN.txt"));
          String str = null;
          String file_name = null;

          try {
                docBuilder = dbfac.newDocumentBuilder();
                Document doc = docBuilder.newDocument();

                Element root = doc.createElement("queries");//create the root element and add it to the document
                       
                doc.appendChild(root);
                
                while((str = br.readLine()) != null){
                    
                         String [] ar = str.split("\\~");

                        //create child element, add an attribute, and add to root
                        Element query = doc.createElement("target");
                        query.setAttribute("name", ar[0]);

                        //UNTUK PATH ATTRIBUTE
                        String outputPath = ar[1];
                        if(outputPath.contains("document-node {")){
                            int in1 = outputPath.indexOf("document-node {");
                            int in2 =  outputPath.indexOf("}");      
                            String r_temp = outputPath.substring(in1, in2+1);
                            in1 = r_temp.indexOf("\"");
                            in2 = r_temp.lastIndexOf("\"");
                            file_name = r_temp.substring(in1+1, in2);

                            outputPath= getPathRoot(outputPath, file_name);
                        }
                        query.setAttribute("file_name", file_name);
                        query.setAttribute("path", outputPath);
                        root.appendChild(query);

                        //UNTUK OLVALUE ELEMENT

                        String oldv= getOldValue(outputPath, file_name);

                        Element oldvalue = doc.createElement("oldvalue");
                        query.appendChild(oldvalue);
                        Text text1 = doc.createTextNode(oldv);
                        oldvalue.appendChild(text1);

                        //UNTUK NEW VALUE ELEMENT
                        Element newvalue = doc.createElement("newvalue");
                        query.appendChild(newvalue);
                        Text text2 = doc.createTextNode(ar[2]);
                        newvalue.appendChild(text2);
        
                    }
                
                            /////////////////
                    //Output the XML

                    //set up a transformer
                    TransformerFactory transfac = TransformerFactory.newInstance();
                    Transformer trans = transfac.newTransformer();
                    trans.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
                    trans.setOutputProperty(OutputKeys.INDENT, "yes");

                    //create string from xml tree
                    StringWriter sw = new StringWriter();
                    StreamResult result = new StreamResult(sw);
                    DOMSource source = new DOMSource(doc);
                    trans.transform(source, result);
                    String xmlString = sw.toString();

                    FileWriter fstream = new FileWriter("dyn_data.xml");
                    BufferedWriter out = new BufferedWriter(fstream);

                    //System.out.println("contain"+xmlString.contains("&quot;"));
                    //xmlString = format(xmlString);

                    out.write(xmlString);
                    //Close the output stream
                    out.close();

            } catch (ParserConfigurationException ex) {
                System.err.print("Parser error");
            }
            
            br.close();
            
            //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
            //------------------------------------------------------------------
            //DYNAMIC VALIDATION- USING SCHEMATRON
            
            BaseXGUI obj = new BaseXGUI();
            //System.out.println(obj.getSchPath());
          
            String[] args = new String[2];
            args[0]= "fitness2new.xml"; 
            args[1]= obj.getSchPath();

                                    
            //---------------------------------------
            //Schematron validation
            //System.out.println("\n-----@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@-----");
            SchtrnValidator.Main(args);
            //================================================================== 
             /*
            //----pre-process constraint object------
          
            String[] forcumm = new String[3];
            forcumm[0]= BaseXGUI.inputpath;
            forcumm[1]= obj.getSchPath();
        
            List<String> returnObj = null;
            try {	    
                returnObj = ObjectProcessing.objProcess(forcumm);
            } catch (Exception ex) {
                Logger.getLogger(QueryProcessor.class.getName()).log(Level.SEVERE, null, ex);
            }
            List OList = new ArrayList();
            List tempORoot = null;             
            String ORoot;
            
            //OList = (List) returnObj.get(0);
            //tempORoot = returnObj.get(1);
            ORoot = returnObj.get(returnObj.size()-1);
            OList = returnObj;
            OList.remove(OList.size()-1);
            
            //---------------------------------------
            //query update XML processing 
            List XList = new ArrayList();
            String XRoot = null;
            String Xfilename = null;
            if(rootDel!=null){
                try {
                    //System.out.println(rootDel);
                    //XRoot = confirmXRoot(rootDel, ORoot);
                    
                    XList = UpdateQProcessing.updateQProcess(rootDel, alDel, 0);
                    XRoot = rootDel;
                    Xfilename = "XMLUpdateData/"+"delsubtree.xml";
                } catch (Exception ex) {
                    Logger.getLogger(QueryProcessor.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            if(rootRep!=null){
                try {
                    //XRoot = confirmXRoot(rootRep, ORoot);
                    XList = UpdateQProcessing.updateQProcess(rootRep, alRep, 1);
                    XRoot = rootRep;
                    Xfilename = "XMLUpdateData/"+"repsubtree.xml";
                } catch (Exception ex) {
                    Logger.getLogger(QueryProcessor.class.getName()).log(Level.SEVERE, null, ex);
                }
                
            }
           
            List valResult;
            valResult = ValidateCumulative.Main(XList, XRoot, Xfilename, OList, ORoot);  
                 
            System.err.println(time1 +" "+ ValidateCumulative.time2);
            System.out.println("\n----------@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@-----------");
            System.out.println("\n\nvalResult:                       "+valResult.get(0));
            //System.out.println("input file(no of node)         " +valResult);  //-------------->
            //System.out.println("input file(depth)              " +valResult);  //--------------->		
            //System.out.println("constraint object exist        " +valResult); //--------------->	
            System.out.println("threshold		         " +valResult.get(1));
            System.out.println("runtime:                         " + ((ValidateCumulative.time2 - time1) / 1000.0)+" sec");
            System.out.println("relevant constraint violation    " + valResult.get(2));                       
            if(valResult.get(3).toString() == "false"){
                System.out.println("distance comp requiered?:        n/a");
            }
            else{
                System.out.println("distance comp requiered?:        yes");
            } 
            System.out.println("distance:                        " + valResult.get(4));
            
            
            //System.out.println("recurence steps:      "	+ valResult); //--------------->
            //System.out.println("heavy paths:          "	+ valResult);//--------------->
            
            */
            //---------------------------------------
            //Schematron validation
            //System.out.println("\n-----@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@-----");
            //SchtrnValidator.Main(args);
            //==================================================================
 
}
  public static String confirmXRoot(String rootRep, String ORoot){
      
      String root = null;
      
      
      return root;
  }
  
  
    private static class SaxHandler extends DefaultHandler {
        public void startElement(String uri, String localName, String qName, Attributes attrs) throws SAXParseException,SAXException {
                int length = attrs.getLength();
                //Each attribute
                for (int i=0; i<length; i++) {
                        // Get names and values to each attribute
                        //String name = attrs.getQName(i);
                    if(attrs.getQName(i) == "path"){
                        //System.out.print(attrs.getQName(i));

                        //System.out.println("\t"+attrs.getValue(i));}
                        alIns.add(attrs.getValue(i));                          
                    }
        }
}
}
  
/**
   * get the real path 
   */
    public static String getPathRoot(String in, String file) throws JDOMException{
    
    String root="";
    
    if(in.contains("document-node {")){
       int in1 = in.indexOf("document-node {");
       int in2 =  in.indexOf("}");
       
       //System.out.println(file);    
       
       //get root of file
       File f = new File(file);      
       SAXBuilder builder = new SAXBuilder();
    
        try {
                org.jdom.Document doc = builder.build(f);
                root = doc.getRootElement().getName();
                
                //System.out.println(root);
        } catch (IOException ex) {
            Logger.getLogger(QueryProcessor.class.getName()).log(Level.SEVERE, null, ex);
        }
             
       //get exact Xpath of the file
        String r_temp = in.substring(in1, in2+1);
        //System.out.println(r_temp);
        in= in.replace(r_temp, root);
        //System.out.println(r_temp);
    }
                
    
    return in;
} 

/*
 * get old value
 */

public static String getOldValue(String path, String file) throws JDOMException, FileNotFoundException, XPathExpressionException, 
            ParserConfigurationException, ParserConfigurationException, ParserConfigurationException, IOException, SAXException{
    
    String oldvalue="";
 
    oldvalue = read(path,file, XPathConstants.STRING).toString();
    
    return oldvalue;
}  

public static Object read(String expression, String file, QName returnType) throws ParserConfigurationException, SAXException, IOException{
    
    
    //String xmlFile;
    Document xmlDocument;
    XPath xPath = null;
    try {
        xmlDocument = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(file);            
        xPath =  XPathFactory.newInstance().newXPath();
        
        
        XPathExpression xPathExpression = xPath.compile(expression);
            return xPathExpression.evaluate(xmlDocument, returnType);
    } catch (XPathExpressionException ex) {
        return null;
    }
}

public static void DelInsPreprocess(ArrayList list1, ArrayList list2) throws FileNotFoundException{
    
    //list1 = insert
    //list2 = delete
    //ArrayList ins = new ArrayList();
    BufferedReader br_ins = new BufferedReader(new FileReader("pre_ins.txt"));
    String str_ins = null;
    //String [] ar1 = null;
    
    
    BufferedReader br_del = new BufferedReader(new FileReader("pre_del.txt")) ;
    String str_del = null;
    //String [] ar2 = null;
    
    
    for (int i=0;i<list2.size();i++) {
            for (int j=0;j<list1.size(); j++) {
                if(list2.get(i).equals(list1.get(j))){
                    //System.err.println(list1.get(j));                   
                    int x=0;
                    try {
                        while((str_ins = br_ins.readLine()) != null){
                            if(x==j){
                                Writer out; 
                                out = new BufferedWriter(new FileWriter("pre_DYN.txt", true));
                                out.write(str_ins+"\n\n");
                                out.close();
                            }
                            x++;
                        }
                    } catch (IOException ex) {
                        Logger.getLogger(QueryProcessor.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    
                }
            }           
    }    
                
          
}

//Generate array lists for cummulative constraint checking
public static void CummConsList() throws FileNotFoundException, IOException{
    

    //create array list untuk delete operation
    BufferedReader br_del = new BufferedReader(new FileReader("pre_del.txt"));
    BufferedReader br_del2 = new BufferedReader(new FileReader("pre_del.txt"));

    String str_del="";
    String [] ar2 = null;

    if(br_del.readLine() != null){
         //System.out.println("Haihaihai");

          while((str_del = br_del2.readLine()) != null){
               ar2 = str_del.split("\\~");
               //String pathtarg= ar2[1]+"/"+ar2[0];
               String pathtarg= ar2[1];
               //pathtarg = pathtarg.substring(pathtarg.indexOf("/")+1);
               //pathtarg = pathtarg.replace(")", "");
               alDel.add(pathtarg);
          }    
    }
    
        //create arraylist untuk replace operation
    BufferedReader br_rep = new BufferedReader(new FileReader("pre_DYN.txt"));
    BufferedReader br_rep2 = new BufferedReader(new FileReader("pre_DYN.txt"));

    String str_rep="";
    String [] ar1 = null;

    if(br_rep.readLine() != null){
         //System.out.println("Haihaihai");

          while((str_rep = br_rep2.readLine()) != null){
               ar1 = str_rep.split("\\~");
               //String pathtarg= ar1[1]+"/"+ar1[0];
               String pathtarg= ar1[1];
               pathtarg = pathtarg.substring(pathtarg.indexOf("/")+1);
               pathtarg = pathtarg.replace("(", "");
               pathtarg = pathtarg.replace(")", "");
               alRep.add(pathtarg);
          }    
    }
    
    /*
    //cuba print isi array list
     for (int i = 0; i < alDel.size(); i++){
            String item = (String) alDel.get(i);
            System.out.println("Item " + i + " : " + item);
            
      }
            */                    
      //if arrayList count >=2, panggil method untuk calculate most common ancestor
      if(alDel.size()>=2){
          delCummConsList();
      }    
      else if (alDel.size()==1){
          rootDel =(String) alDel.get(0);
      }
      
      if(alRep.size()>=2){                                                                                             
          repCummConsList();
      }
      else if (alRep.size()==1){
        rootRep =(String) alRep.get(0);
      }
    
        
}

public static void delCummConsList(){
            //--------------------------------------
    //Set saxon as transformer.
    System.setProperty("javax.xml.transform.TransformerFactory",
                         "net.sf.saxon.TransformerFactoryImpl");
    XSLTemplate template = new XSLTemplate();

      
      //mencari comm ancestor dari array list-del dan array list replace
      //--------------------------------

    String mostcomm="";

    if(!alDel.isEmpty()){
        try{
            template.setStyleSheetPath("C:/Documents and Settings/User/My Documents/NetBeansProjects/BaseX.v2/Scripts/commAncs.xsl");

            template.setTemplateName("findComm");
            Map<String, String> params = new HashMap<String, String>();

            if (!alDel.isEmpty()){
                for (int i = 0; i < alDel.size(); i++){
                    //System.out.println("top"+i);
                    String path = (String) alDel.get(i);
                    String compath=""; 
                    if(i>=2){
                        compath = mostcomm;
                    }
                    else{
                        compath = (String) alDel.get(i+1);
                    }
                    if(i==0){i++;}//go to index 2 ==> al[2] for next comparison

                    params.put("param1", compath); 
                    params.put("param2", path);

                    template.setParameters(params);

                    //call transformation function using Saxon, this is where is calculate
                    //the most common ancestor of the xpath
                    mostcomm = template.call();//call the template to calculate the most common ancestor

                    //----------

                    if(i!=alDel.size()-1){
                        mostcomm= mostcomm+ "node()"; //trim the xpath for next calculation of most common ancestor
                    }
                    else{
                        mostcomm = mostcomm.substring(0,mostcomm.length()-1); //trimming to get final most common ancestor
                    }
                    //System.out.println("Most common ancestor: "+ mostcomm+"\n");
                }
                
                rootDel = mostcomm;

                /*
                try{
                    
                    System.out.println("Most common ancestor-delete: "+ mostcomm+"\n");
                }catch(Exception e) {
                    System.out.println("Some error occured!");
                }*/

            }

        }catch(Exception e){
            System.out.println("Delete comm ancestor exception");
        }
    }
}

public static void repCummConsList(){
            //--------------------------------------
    //Set saxon as transformer.
    System.setProperty("javax.xml.transform.TransformerFactory",
                         "net.sf.saxon.TransformerFactoryImpl");
    XSLTemplate template = new XSLTemplate();

      
      //mencari comm ancestor dari array list-del dan array list replace
      //--------------------------------

    String mostcomm="";

    if(!alRep.isEmpty()){
        try{
            template.setStyleSheetPath("C:/Documents and Settings/User/My Documents/NetBeansProjects/BaseX.v2/Scripts/commAncs.xsl");

            template.setTemplateName("findComm");
            Map<String, String> params = new HashMap<String, String>();

            if (!alRep.isEmpty()){
                for (int i = 0; i < alRep.size(); i++){
                    //System.out.println("top"+i);
                    String path = (String) alRep.get(i);
                    String compath=""; 
                    if(i>=2){
                        compath = mostcomm;
                    }
                    else{
                        compath = (String) alRep.get(i+1);
                    }
                    if(i==0){i++;}//go to index 2 ==> al[2] for next comparison

                    params.put("param1", compath); 
                    params.put("param2", path);

                    template.setParameters(params);

                    //call transformation function using Saxon, this is where is calculate
                    //the most common ancestor of the xpath
                    mostcomm = template.call();//call the template to calculate the most common ancestor

                    //----------

                    if(i!=alRep.size()-1){
                        mostcomm= mostcomm+ "node()"; //trim the xpath for next calculation of most common ancestor
                    }
                    else{
                        mostcomm = mostcomm.substring(0,mostcomm.length()-1); //trimming to get final most common ancestor
                    }
                    //System.out.println("Most common ancestor: "+ mostcomm+"\n");
                }
                rootRep = mostcomm;

                /*
                try{
                    System.out.println("Most common ancestor-replace: "+ mostcomm+"\n");
                }catch(Exception e) {
                    System.out.println("Some error occured!");
                }*/

            }

        }catch(Exception e){
            System.out.println("Replace comm ancestor exception");
            rootRep =null;
        }
    }
}

//--------------------------
/**
   * pretty printing
   */

public void serialize(Document doc, OutputStream out)  {

  TransformerFactory tfactory = TransformerFactory.newInstance();
  Transformer serializer;
  try {
   serializer = tfactory.newTransformer();
   //Setup indenting to "pretty print"
   serializer.setOutputProperty(OutputKeys.INDENT, "yes");
   serializer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");

   DOMSource xmlSource = new DOMSource(doc);
   StreamResult outputTarget = new StreamResult(out);
   serializer.transform(xmlSource, outputTarget);
  } catch (TransformerException e) {
   // this is fatal, just dump the stack and throw a runtime exception
   e.printStackTrace();

   throw new RuntimeException(e);
  }
 }


  /**
   * Binds a value to a global variable. If the value is an {@link Expr}
   * instance, it is directly assigned. Otherwise, it is first cast to the
   * appropriate XQuery type. If {@code "json"} is specified as data type,
   * the value is interpreted according to the rules specified in
   * {@link JsonMapConverter}.
   * @param name name of variable
   * @param value value to be bound
   * @param type data type
   * @return self reference
   * @throws QueryException query exception
   */
  public QueryProcessor bind(final String name, final Object value,
      final String type) throws QueryException {
    ctx.bind(name, value, type);
    return this;
  }

  /**
   * Binds a value to a global variable. If the value is an {@link Expr}
   * instance, it is directly assigned. Otherwise, it is first cast to the
   * appropriate XQuery type.
   * @param name name of variable
   * @param value value to be bound
   * @return self reference
   * @throws QueryException query exception
   */
  public QueryProcessor bind(final String name, final Object value)
      throws QueryException {
    ctx.bind(name, value);
    return this;
  }

  /**
   * Sets a value as context item. If the value is an {@link Expr}
   * instance, it is directly assigned. Otherwise, it is first cast to the
   * appropriate XQuery type.
   * @param value value to be bound
   * @return self reference
   * @throws QueryException query exception
   */
  public QueryProcessor context(final Object value) throws QueryException {
    ctx.ctxItem = value instanceof Expr ? (Expr) value :
      JavaMapping.toValue(value);
    return this;
  }

  /**
   * Declares a namespace.
   * A namespace is undeclared if the {@code uri} is an empty string.
   * The default element namespaces is set if the {@code prefix} is empty.
   * @param prefix namespace prefix
   * @param uri namespace uri
   * @return self reference
   * @throws QueryException query exception
   */
  public QueryProcessor namespace(final String prefix, final String uri)
      throws QueryException {
    ctx.sc.namespace(prefix, uri);
    return this;
  }

  /**
   * Returns a serializer for the given output stream.
   * Optional output declarations within the query will be included in the
   * serializer instance.
   * @param os output stream
   * @return serializer instance
   * @throws IOException query exception
   * @throws QueryException query exception
   */
  public Serializer getSerializer(final OutputStream os) throws IOException,
      QueryException {

    compile();
    try {
      return Serializer.get(os, ctx.serParams(true));
    } catch(final SerializerException ex) {
      throw ex.getCause();
    }
  }

  /**
   * Evaluates the specified query and returns the result nodes.
   * @return result nodes
   * @throws QueryException query exception
   */
  public Nodes queryNodes() throws QueryException{
    final Result res = execute();
    if(!(res instanceof Nodes)) {
      // convert empty result to node set
      if(res.size() == 0) return new Nodes(ctx.nodes.data);
      // otherwise, throw error
      QUERYNODES.thrw(null);
    }
    return (Nodes) res;
  }

  /**
   * Adds a module reference.
   * @param uri module uri
   * @param file file name
   */
  public void module(final String uri, final String file) {
    ctx.modDeclared.add(token(uri), token(file));
  }

  /**
   * Sets a new query. Should be called before parsing the query.
   * @param qu query
   */
  public void query(final String qu) {
    query = qu;
    parsed = false;
    compiled = false;
  }

  /**
   * Returns the query string.
   * @return query
   */
  public String query() {
    return query;
  }

  /**
   * Closes the processor.
   * @throws QueryException query exception
   */
  public void close() throws QueryException {
    // close only once
    if(closed) return;
    closed = true;

    // reset database properties to initial value
    for(final Entry<String, Object> e : ctx.globalOpt.entrySet()) {
      ctx.context.prop.set(e.getKey(), e.getValue());
    }
    // close database connections
    ctx.resource.close();
    // close JDBC connections
    if(ctx.jdbc != null) ctx.jdbc.close();
    // close dynamically loaded JAR files
    if(ctx.jars != null) ctx.jars.close();
  }

  /**
   * Returns the number of performed updates.
   * @return number of updates
   */
  public int updates() {
    return ctx.updating() ? ctx.updates.size() : 0;
  }

  /**
   * Returns query background information.
   * @return background information
   */
  public String info() {
    return ctx.info();
  }

  /**
   * Removes comments from the specified string.
   * @param qu query string
   * @param max maximum string length
   * @return result
   */
  public static String removeComments(final String qu, final int max) {
    final StringBuilder sb = new StringBuilder();
    int m = 0;
    boolean s = false;
    final int cl = qu.length();
    for(int c = 0; c < cl && sb.length() < max; ++c) {
      final char ch = qu.charAt(c);
      if(ch == 0x0d) continue;
      if(ch == '(' && c + 1 < cl && qu.charAt(c + 1) == ':') {
        if(m == 0 && !s) {
          sb.append(' ');
          s = true;
        }
        ++m;
        ++c;
      } else if(m != 0 && ch == ':' && c + 1 < cl && qu.charAt(c + 1) == ')') {
        --m;
        ++c;
      } else if(m == 0) {
        if(ch > ' ') sb.append(ch);
        else if(!s) sb.append(' ');
        s = ch <= ' ';
      }
    }
    if(sb.length() >= max) sb.append("...");
    return sb.toString().trim();
  }

  /**
   * Returns the query plan in the dot notation.
   * @param ser serializer
   * @throws IOException I/O exception
   */
  public void plan(final Serializer ser) throws IOException {
    ctx.plan(ser);
  }

  @Override
  public String tit() {
    return EVALUATING_C;
  }

  @Override
  public String det() {
    return EVALUATING_C;
  }
}
