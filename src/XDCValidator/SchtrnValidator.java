/*
 * SchtrnValidator.java - Perform Schematron validation
 * Copyright (C) 2002 Eddie Robertsson
 *
 * You may use and modify this package for any purpose. Redistribution is
 * permitted, in both source and binary form, provided that this notice
 * remains intact in all source distributions of this package.
 */

/*This program has been modified for experimentation purpose, by:
 * Norfaradilla Wahid
 * PhD Candidate, La Trobe University,
 * Victoria, Australia.
 * nwahid@students.latrobe.edu.au OR faradila@uthm.edu.my
 */

package XDCValidator;

import XDCValidator.SchtrnParams;
import java.io.File;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;
//import java.util.Enumeration;
import java.util.Iterator;
import java.util.Map;

import javax.swing.JOptionPane;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;



/**
 * <p>A simple java implementation of an XSLT based Schematron validator.</p>
 *
 * <p>This class is provided as a model for how Schematron validation can
 * be implemented using two XSLT transformations. The implementations
 * use the JAXP interface and should work with all XSLT processors that
 * implements JAXP. This implementation uses Michael Kay's <code>Saxon</code>
 *  6.3.</p>
 *
 * <p>The implementation can be used both as an API (by using the <code>validate(...)</code>
 * methods) and from the commandline (the <code>main(...)</code> method).</p>
 *
 * @see: http://users.iclway.co.uk/mhkay/saxon/
 * @author: Eddie Robertsson
 */
public class SchtrnValidator {

    //public double USPrice_new= 50.00;
	/**
	* The Schematron parameters that should be used when creating
	* the validation stylesheet
	*/
	private SchtrnParams params;
	/**
	* The <code>TransformerFactory</code> used to the get the
	* <code>Transformer</code> objects.
	*/
	private TransformerFactory theFactory;
	/**
	* The XSLT stylesheet (as a StreamSource) that is used to generate the
	* validating XSLT stylesheet from Schematron schema
	*/
	private StreamSource engineStylesheet = null;
	/**
	* True if relative paths in the created validation stylesheet should be
	* resolved against the XML instance document. If false relative paths will
	* be resolved against the location of the Schematron schema.
	*/
	private boolean baseIsXML = true;
	/**
	 * Create a Schematron validator object.
	 */
	public SchtrnValidator() {
		theFactory = TransformerFactory.newInstance();
	}
	/**
	 * Set the Schematron parameters that should be used when creating
	 * the validation stylesheet.
	 * @param: sp The SchtrnParams object with the specified
	 * parameters
	 */
	public void setParams(SchtrnParams sp) {
		params = sp;
	}
	/**
	 * Run the Schematron validation from the commandline.
	 *
	 * <pre>
	 * Usage: SchtrnValidator 'xml' 'schema' {param=value}..."
	 *
	 * 'xml'       : The location of the XML instance document
	 * 'schema'    : The location of the Schematron schema
	 *
	 * Supported parameters:
	 *    diagnose  : 'yes'|'no'  If yes then diagnostics will be
	 *                 included in the result if specified in the Schematron
	 *                 schema and supported by the engine stylesheet.
	 *    phase     :  The name of the phase that should be used for validation.
	 *                 If unspecified then all phases will be used unless
	 *                 the schema specifies the 'defaultPhase' attribute.
	 * </pre>
	 *
	 * @param: args The arguments sent to the application
	 */
	public static void Main(String[] args) {

        //String path="C:\\Documents and Settings\\nwahid\\My Documents\\NetBeansProjects\\SchtrnValidator\\Data\\";

 	// There must be at least two arguments
		if (args.length < 2) {
			System.out.println("Not enough arguments.");
			printUsage();
			System.exit(0);
		}
		// The xml instance is the first argument
		String xml = args[0];
		// The Schematron schema is the second argument
		String schema = args[1];
                //System.err.println(xml+"\n"+schema);
		// Create the Schematron validator
		SchtrnValidator validator = new SchtrnValidator();
		// Set the engine stylesheet that should be used. In this case we use
		// the schematron-diagnose.xsl stylesheet that is located in the scripts
		// directory in the distribution.
		validator.setEngineStylesheet("Scripts" + File.separator + "schematron-diagnose.xsl");
		// Set the parameters that should be used
		validator.setParams(parseArgs(args));
		// Specify that relative paths should be resolved against the XML
		// instance document
		validator.setBaseXML(true);
                
                
		// Print the result to System.out
		try {
                        String out = validator.validate(xml, schema);
                        
                        if(out.length() < 40){
                            System.err.println("\n\nPassed dynamic constraint validation");
                            JOptionPane.showConfirmDialog(null, "Passed dynamic constraint validation\n\nUPDATE DATABASE NOW?", "Update Database", JOptionPane.ERROR_MESSAGE);
                        }
                        else{
                            System.out.println(validator.validate(xml, schema));
                        
                            JOptionPane.showConfirmDialog(null, validator.validate(xml, schema) + "\n\nPROCEED DATABASE UPDATE?", "Dynamic Constraint Validator", JOptionPane.ERROR_MESSAGE);
                        }
                                                       
		} catch (TransformerConfigurationException tce) {
			System.out.println("Configuration error: " + tce.getMessageAndLocation());
		} catch (TransformerException te) {
			System.out.println("Transformation error: " + te.getMessageAndLocation());
		} catch (Exception e) {
			System.out.println("An error occurred: " + e.getMessage() + "\n" + e);
		}
	}
	/**
	 * Parse the commansline arguments and return the Schematron parameters
	 * that are specified.
	 * @param: args The commandline arguments
	 * @return: The SchtrnParams object with the specified parameters
	 */
	static SchtrnParams parseArgs(String[] args) {
		SchtrnParams sp = new SchtrnParams();
		// Start at the third parameter
		for (int i = 2; i < args.length; i++) {
			int eq = args[i].indexOf('=');
			if (eq < 0) {
				// We don't have an '=' so the argument is invalid
				System.out.println("In illegal argument was specified.");
				printUsage();
				System.exit(0);
			}
			sp.setParam(args[i].substring(0, eq), args[i].substring(eq + 1));
		}
		return sp;
	}
	/**
	 * Print a message to System.out with the options for running validation
	 * from the commandline
	 */
	private static void printUsage() {
		System.out.println(
			"Usage: SchtrnValidator <xml> <schema> {param=value}...\n\n"
				+ "<xml>       : The location of the XML instance document\n"
				+ "<schema>    : The location of the Schematron schema\n\n"
				+ "Supported parameters:\n"
				+ "  diagnose  : <yes>|<no>  If yes then diagnostics will be \n"
				+ "               included in the result if specified in the Schematron\n"
				+ "               schema and supported by the engine stylesheet.\n"
				+ "  phase     :  The name of the phase that should be used for validation.\n"
				+ "               If unspecified then all phases will be used unless\n"
				+ "               the schema specifies the 'defaultPhase' attribute.");
	}
	/**
	 * <p>Validate an XML instance document against a Schematron schema.</p>
	 * <p>The method takes the paths to the XML instance document and the
	 * Schematron schema as parameters.</p>
	 * <p>If the paths starts with 'file:' it is assumed that the location is
	 * a valid URL and it will be used as such. Otherwise the path provided will
	 * be converted to a URL before validation</p>
	 * @param: xml The path to the XML instance document
	 * @param: schema The path to the Schematron schema
	 * @return: The results from Schematorn validation as a <code>String</code>
	 */
	public String validate(String xml, String schema) throws TransformerConfigurationException, TransformerException, Exception {
		String xmlURL = system_To_URL(xml);
		if (xmlURL == null) {
			throw new Exception("The xml file location is not valid: " + xml);
		}
		String schemaURL = system_To_URL(schema);
		if (schemaURL == null) {
			throw new Exception("The Schematron schema location is not valid: " + schema);
		}
		return validate(new StreamSource(xmlURL), new StreamSource(schemaURL));
	}
	/**
	 * <p>Validate an XML instance document against a Schematron schema.</p>
	 * <p>The method takes a <code>StreamSource</code> representation of the
	 * XML instance document and the Schematron schema as parameters.</p>
	 * @param: xml The XML instance document provided as a
	 * <code>StreamSource</code>
	 * @param: schema The Schematron schema provided as a
	 * <code>StreamSource</code>
	 * @return: The results from Schematorn validation as a <code>String</code>
	 */
	public String validate(StreamSource xml, StreamSource schema) throws TransformerConfigurationException, TransformerException {
		// Create the validating XSLT stylesheet. This stylesheet can be cached
		// and reused if multiple XML document should be validated against the
		// same schema using the validateCached(...) method.
		StreamSource validationStylesheet = getValidationStylesheet(schema, getEngineStylesheet());
		// Set the correct systemId on the validationStylesheet
		if (baseIsXML) {
			validationStylesheet.setSystemId(xml.getSystemId());
		} else {
			validationStylesheet.setSystemId(schema.getSystemId());
		}
		// Do the tranformation that performs the validation
		return transform(xml, validationStylesheet, null);
	}
	/**
	 * <p>Validate an XML instance document against a cached XSLT stylesheet
	 * that has been created from the Schematron schema. This is useful if
	 * multiple XML document should be validated against the same schema.</p>
	 * <p>The method takes a <code>StreamSource</code> representation of the
	 * XML instance document, the Schematron schema and the validating XSLT
	 * stylesheet as parameters.</p>
	 * @param: xml The XML instance document provided as a
	 * <code>StreamSource</code>
	 * @param: schema The Schematron schema provided as a
	 * <code>StreamSource</code>
	 * @param: validator The validating XSLT stylesheet provided as a
	 * <code>StreamSource</code>
	 * @return: The results from Schematorn validation as a <code>String</code>
	 */
	public String validate(StreamSource xml, StreamSource schema, StreamSource validator) throws TransformerConfigurationException, TransformerException {
		// Set the correct systemId on the validationStylesheet
		if (baseIsXML) {
			validator.setSystemId(xml.getSystemId());
		} else {
			validator.setSystemId(schema.getSystemId());
		}
		// Do the tranformation that performs the validation
		return transform(xml, validator, null);
	}
	/**
	 * Create the validating XSLT stylesheet from the Schematron schema,
	 * the Schematron parameters and the specified Schematron engine
	 * stylesheet.
	 * @param: schema The Schematron schema as a <code>StreamSource</code>
	 * @param: engine The Schematron engine stylesheet as a
	 * <code>StreamSource</code>
	 * @return: The validating XSLT stylesheet as a <code>StreamSource</code>
	 */
	public StreamSource getValidationStylesheet(StreamSource schema, StreamSource engine) throws TransformerConfigurationException, TransformerException {
		// Get the validation stylesheet
		return new StreamSource(new StringReader(transform(schema, engine, params)));
	}
	/**
	 * <p>Transform a target document with the provided XSLT stylesheet and
	 * some optional parameters.</p>
	 * The target and stylesheet are provided as a <code>StreamSource</code>
	 * objects while the parameters are specified as a <code>Map</code> where
	 * the key is the name of the parameter and the value is the parameter value.</p>
	 * If no paramters should be sent then just pass <code>null</code>.
	 * @param: target The document that should be transformed as a
	 * <code>StreamSource</code>
	 * @param: stylesheet The XSLT stylesheet used in the transformation
	 * <code>StreamSource</code>
	 * @param: parameters The parameters sent to the stylesheet.
	 * @return: The result of the transformation as a <code>String</code>
	 */
	private String transform(StreamSource target, StreamSource stylesheet, Map parameters) throws TransformerConfigurationException, TransformerException {
		// Create the transformer with the specified stylesheet
		Transformer transformer = theFactory.newTransformer(stylesheet);
		// Set the parameters if we have any
		if (parameters != null) {
			Iterator iter = parameters.keySet().iterator();
			while (iter.hasNext()) {
				String param = (String) iter.next();
				transformer.setParameter(param, (String) params.get(param));
			}
		}
		StringWriter result = new StringWriter();
		transformer.transform(target, new StreamResult(result));
		return result.toString().trim();
	}
	/**
	 * <p>Specify if relative paths should be resolved against the XML instance
	 * document or against the Schematron schema.</p>
	 * <p>For example, if the document() function is used in the Schematron schema
	 * and it specifies a relative path this method is used the change the base
	 * from which the relative path is resolved.</p>
	 * @param: useXML True if the XML instance document should be used (default)
	 * If false then the Schematorn schema location will be used as the base.
	 */
	public void setBaseXML(boolean useXML) {
		baseIsXML = useXML;
	}
	/**
	 * Convert a system dependent path to a URL.
	 * NOTE: This is a very simple convert method and if the path already
	 * starts with 'file:' no extra check will be performed to make sure that
	 * it's actually a valid URL.
	 * Creation date: (2/5/2002 12:18:20 PM)
	 * @param: path The system path to convert to a URL
	 * @return: The URL as a <code>String</code> if successful otherwise <code>null</code>
	 */
	static String system_To_URL(String path) {
		if (path.startsWith("file:")) {
			// If the path starts with file we assume that it's a valid URL...
			return path;
		}
		try {
			// Use Java's File class to convert the path to a URL
			URL url = new File(path).toURL();
			return url.toString();
		} catch (MalformedURLException e) {
			return null;
		}
	}
	/**
	 * Get the engine stylesheet
	 * @return StreamSource
	 */
	public StreamSource getEngineStylesheet() {
		return engineStylesheet;
	}
	/**
	 * Sets the engine stylesheet by providing a path to the location of the
	 * stylesheet file.
	 * @param enginePath The path to the stylesheet
	 */
	public void setEngineStylesheet(String enginePath) {
		if (enginePath != null) {
			engineStylesheet = new StreamSource(system_To_URL(enginePath));
		}
	}
}
