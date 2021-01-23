package org.vedantatree.utils.xml;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;


/**
 * MTMetadataErrorHandeler is responsible for handling the error occured during validating the XML containing the
 * metadata of master tables.
 * 
 * @author Mohit Gupta
 * 
 */
public class XMLErrorHandler implements ErrorHandler
{

	/** For logging purpose */
	private static Log LOGGER = LogFactory.getLog( XMLErrorHandler.class );

	/**
	 * The error is invoked when ever any error occurs during XML validation.
	 * 
	 */
	public void error( SAXParseException exception ) throws SAXException
	{
		LOGGER.error( "Error occured during parsing", exception );
		throw new SAXException( "Error occured during parsing", exception );
	}

	/**
	 * The fatalError is invoked when ever any fatal error occurs during XML validation.
	 * 
	 */
	public void fatalError( SAXParseException exception ) throws SAXException
	{
		LOGGER.fatal( "Fatal Error occured during parsing", exception );
		throw new SAXException( "Error occured during parsing", exception );
	}

	/**
	 * The warning is invoked when ever any warning occurs during XML validation.
	 */
	public void warning( SAXParseException exception ) throws SAXException
	{
		LOGGER.warn( "Warning call during parsing", exception );
	}

}
