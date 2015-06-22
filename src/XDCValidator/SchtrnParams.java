package XDCValidator;



/*
 * SchtrnParams.java - Stores Schematron parameteres
 * Copyright (C) 2002 Eddie Robertsson
 *
 * You may use and modify this package for any purpose. Redistribution is
 * permitted, in both source and binary form, provided that this notice
 * remains intact in all source distributions of this package.
 */
 
import java.util.Hashtable;

/**
 * Store a set of Schematron parameters that can be used when creating the 
 * validating XSLT stylesheet.<br>
 * Each entry has a key that is equal to the name of the parameter that should
 * be passed to the stylesheet (e.g. 'phase'). The value for the entry is the
 * actual value of the parameter (e.g. '#ALL')
 *
 * @author: Eddie Robertsson
 */
public class SchtrnParams extends Hashtable {

	/**
	* The predefined parameter "phase"
	*/
	public final static String PHASE = "phase";
	/**
	* The predefined parameter "diagnose"
	*/
	public final static String DIAGNOSE = "diagnose";
	/**
	* The predefined value "#ALL" for the parameter "phase"
	*/
	public final static String ALL_PHASES = "#ALL";
	/**
	 * Create the <code>SchtrnParams</code> object with an initial <code>Hashtable</code>
	 * size of 5.
	 */
	public SchtrnParams() {
		super(5);
	}
	/**
	 * Set the parameter with name "name" to the value "value"
	 * @param: name The name of the parameter to set
	 * @param: value The value of the parameter
	 */	
	public void setParam(String name, String value) {
		put(name, value);
	}
}
