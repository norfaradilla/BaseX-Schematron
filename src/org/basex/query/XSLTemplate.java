package org.basex.query;

import java.io.File;
import java.io.StringReader;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.stream.StreamSource;
import org.basex.BaseXGUI;
import org.basex.query.expr.Expr;
import org.basex.query.func.FNDb;

import org.basex.query.func.FNDb.*;
import org.basex.query.item.QNm;
import org.basex.query.item.QNm.*;


/**
* To Call XSLT template from Java Code
*
* @author mohammed hewedy <br />
* mhewedy@gmail.com
* 
* edited by norfaradilla wahid
* nwahid@students.latrobe.edu.au
*/
public class XSLTemplate {

  private String styleSheetPath;
  private String templateName;
  private Map<String, String> parameters;

  private Logger logger = Logger.getLogger(XSLTemplate.class.getSimpleName());

  public XSLTemplate() {
  }

  public XSLTemplate(String styleSheetPath, String templateName,
          Map<String, String> parameters) {
      this.styleSheetPath = styleSheetPath;
      this.templateName = templateName;
      this.parameters = parameters;
  }
  public XSLTemplate(String styleSheetPath, String templateName) {
      super();
      this.styleSheetPath = styleSheetPath;
      this.templateName = templateName;
  }

  public String getStyleSheetPath() {
      return styleSheetPath;
  }
  public void setStyleSheetPath(String styleSheetPath) {
      this.styleSheetPath = styleSheetPath;
  }
  public String getTemplateName() {
      return templateName;
  }
  public void setTemplateName(String templateName) {
      this.templateName = templateName;
  }
  public Map<String, String> getParameters() {
      return parameters;
  }
  public void setParameters(Map<String, String> parameters) {
      this.parameters = parameters;
  }

  /**
   * Call this method to execute the template
   * @return Node object by the resulting document to parse and obtain the result,
   * returns null in the following cases:
   * <ul>
   * <li>StyleSheetPath property is null<li>
   * <li>TemplateName property is null<li>
   * </ul>
   * @throws TransformerConfigurationException
   * @throws TransformerException
   */
  public String call() throws TransformerConfigurationException,  TransformerException{
      Transformer transformer = null;
      
      //change the URI accordingly

      //xslSource.getClass().getClassLoader().getResource("converter.xsl").getPath();
      Source xslSource = constructStyleSheet();
      if (xslSource== null)
          return null;

      transformer = TransformerFactory.newInstance().newTransformer(xslSource);
      
      //Source XMLIn = new StreamSource(new File("C:/Documents and Settings/User/My Documents/NetBeansProjects/BaseX.v2/fitness2.xml"));
      
      Source XMLIn = new StreamSource(new File(System.getProperty("user.dir")+"/XMLdata/fitness1.xml" ));
      //FNDb.PATH
      //FNDb obj;
       // obj = new FNDb();
      //BaseXGUI pathobj= new BaseXGUI();
      //System.err.println(BaseXGUI.inputpath);
      DOMResult result = new DOMResult();
      transformer.transform(XMLIn, result);

      //return result;
      return result.getNode().getChildNodes().item(0).getTextContent().toString();
  }

  /**
   * Returns Source of type StreamSource , or null if any of XSLTemplete.StyleSheetPath or
   * XSLTemplete.TemplateName is null
   * @return
   */
  Source constructStyleSheet() {
      if (!validate())
          return null;

      Source src = null;
      StringBuffer sb = new StringBuffer();
      sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
      sb.append("<xsl:stylesheet version=\"1.0\" xmlns:xsl=\"http://www.w3.org/1999/XSL/Transform\">");
      sb.append("<xsl:include href=\"" + getStyleSheetPath() + "\" /> ");
      sb.append("<xsl:template match=\"/\">");
      sb.append("<xsl:call-template name=\"" + getTemplateName()+  "\">");

      Map<String, String> params = getParameters();
      if ( params != null) {
            Iterator<String> keyIter = params.keySet().iterator();
            while (keyIter.hasNext()) {
                String name = keyIter.next();
                String value = params.get(name);
                sb.append("<xsl:with-param name=\"" + name + "\" select=\"'" + value + "'\" />");
            }
      }
      sb.append("</xsl:call-template>");
      sb.append("</xsl:template>");
      sb.append("</xsl:stylesheet>");
      logger.log(Level.FINE, sb.toString());
      src = new StreamSource(new StringReader(sb.toString()));
      return src;
  }

  boolean validate() {
      if (getStyleSheetPath()==null || getTemplateName()==null)
          return false;
      // TODO: validate TemplateName to make sure it is a valid XSLT identifier
      return true;
  }
}