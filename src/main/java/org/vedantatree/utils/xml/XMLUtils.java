package org.vedantatree.utils.xml;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xerces.parsers.DOMParser;
import org.vedantatree.exceptions.ApplicationException;
import org.vedantatree.exceptions.IErrorCodes;
import org.vedantatree.utils.Utilities;
import org.w3c.dom.Document;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;


public class XMLUtils
{

	private static Log LOGGER = LogFactory.getLog( XMLUtils.class );

	public static Document parseXML( InputStream is, String xsdPath ) throws ApplicationException
	{
		LOGGER.debug( "entering : parseXML" );
		Utilities.assertNotNullArgument( is );

		DOMParser parser = new DOMParser();
		try
		{
			if( xsdPath != null )
			{
				// Validating
				parser.setFeature( "http://xml.org/sax/features/validation", true );
				parser.setProperty( "http://apache.org/xml/properties/schema/external-noNamespaceSchemaLocation",
						xsdPath );
			}

			// Providing error handeler
			ErrorHandler errorHandler = new XMLParserErrorHandler();
			parser.setErrorHandler( errorHandler );
			parser.parse( new InputSource( is ) );
			return parser.getDocument();
		}
		catch( Exception e )
		{
			LOGGER.error( "Problem while parsing XML.", e );
			throw new ApplicationException( IErrorCodes.XML_PARSING_ERROR, "Problem while parsing XML.", e );
		}
	}

	public static Document parseXML( File file, String xsdPath ) throws ApplicationException
	{
		Utilities.assertNotNullArgument( file );

		try
		{
			return parseXML( new FileInputStream( file ), xsdPath );
		}
		catch( FileNotFoundException e )
		{
			LOGGER.error( "File not found while parsing the xml. file[" + file.getAbsolutePath() + "]" );
			throw new ApplicationException( IErrorCodes.RESOURCE_NOT_FOUND,
					"File not found while parsing the xml. file[" + file.getAbsolutePath() + "]" );
		}

		// DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		//
		// try {
		// DocumentBuilder builder = factory.newDocumentBuilder();
		// return builder.parse( file );
		// }
		// catch( Exception e ) {
		// LOGGER.error( "Problem while parsing XML.", e );
		// throw new ApplicationException( IErrorCodes.XML_PARSING_ERROR, "Problem while parsing XML.", e );
		// }
	}

}
