package org.vedantatree.utils;

import java.util.Comparator;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.vedantatree.exceptions.IErrorCodes;
import org.vedantatree.exceptions.SystemException;


public class PropertyComparator implements Comparator
{

	private static Log	LOGGER	= LogFactory.getLog( PropertyComparator.class );

	private String		propertyName;

	public PropertyComparator( String propertyName )
	{
		this.propertyName = propertyName;
	}

	public int compare( Object o1, Object o2 )
	{
		try
		{
			Comparable value1 = (Comparable) PropertyUtils.getProperty( o1, propertyName );
			Comparable value2 = (Comparable) PropertyUtils.getProperty( o2, propertyName );

			return ( value1 != null && value2 != null ) ? value1.compareTo( value2 ) : 0;
		}
		catch( Exception e )
		{
			SystemException ae = new SystemException( IErrorCodes.ILLEGAL_STATE_ERROR,
					"Error while accessing the properties. Object1[" + o1 + "] Object2[" + o2 + "] propertyName["
							+ propertyName + "]",
					e );
			LOGGER.error( ae );
			throw ae;
		}
	}
}
