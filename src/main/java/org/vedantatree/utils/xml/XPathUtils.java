/*
 * Created on Jan 1, 2001
 *
 * Copyright 2005 Ganges - Organization for Research
 */
package org.vedantatree.utils.xml;

import java.io.File;
import java.net.URL;

import javax.xml.transform.TransformerException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xpath.XPathAPI;
import org.apache.xpath.objects.XNodeSet;
import org.apache.xpath.objects.XObject;
import org.vedantatree.utils.Utilities;
import org.vedantatree.utils.exceptions.ErrorCodes;
import org.vedantatree.utils.exceptions.XException;
import org.vedantatree.utils.types.Type;
import org.vedantatree.utils.types.ValueObject;
import org.w3c.dom.Document;


/**
 * @author Mohit Gupta
 */
public class XPathUtils
{

	private static Log LOGGER = LogFactory.getLog( XPathUtils.class );

	public static ValueObject evaluateExpressionToValueObject( Document document, String xPathExpression )
			throws XException
	{
		XObject xObject = evaluateExpression( document, xPathExpression );
		try
		{
			if( xObject.getType() == XObject.CLASS_BOOLEAN )
			{
				return new ValueObject( new Boolean( xObject.bool() ), Type.BOOLEAN );
			}
			else if( xObject.getType() == XObject.CLASS_NUMBER )
			{
				return new ValueObject( new Double( xObject.num() ), Type.DOUBLE );
			}
			else if( xObject.getType() == XObject.CLASS_STRING )
			{
				return new ValueObject( xObject.str(), Type.STRING );
			}
			else if( xObject.getType() == XObject.CLASS_NODESET )
			{
				// passing XNodeSet
				return new ValueObject( xObject.object(), Type.XML );
			}
			else
			{
				return new ValueObject( xObject.object(), Type.ANY_TYPE );
			}
		}
		catch( TransformerException e )
		{
			LOGGER.error( "Problem while getting result from XObject.", e );
			throw new XException( "Problem while getting result from XObject.",
					ErrorCodes.EXPRESSION_EVALUATION_PROBLEM, e );
		}
	}

	public static String getXMLString( Object obj )
	{
		Utilities.assertNotNullArgument( obj );
		if( obj instanceof XNodeSet )
		{
			return ( (XNodeSet) obj ).str();
		}
		return obj.toString();
	}

	public static XObject evaluateExpression( Document document, String xPathExpression ) throws XException
	{
		XObject expressionValue = null;
		try
		{
			expressionValue = XPathAPI.eval( document, xPathExpression );
		}
		catch( TransformerException e )
		{
			LOGGER.error( "Problem while parsing XML.", e );
			throw new XException( "Problem while parsing XML.", ErrorCodes.EXPRESSION_EVALUATION_PROBLEM, e );
		}
		return expressionValue;
	}

	public static void main( String[] args ) throws Exception
	{
		URL url = new URL( "file:\\F:\\temp\\test.xml" );
		System.out.println( url.getFile() );
		File file = new File( url.getFile() );
		System.out.println( file.getPath() );

		Document doc = XMLUtils.parseXML( file, null );

		ValueObject value = XPathUtils.evaluateExpressionToValueObject( doc, "/new1/book/@name" );
		System.out.println( "value[" + value.getValue() + "]" );

		System.out
				.println( "valueXObject[" + XPathUtils.evaluateExpression( doc, "/new1/book/@name" ).toString() + "]" );
	}
}
