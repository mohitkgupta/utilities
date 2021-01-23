/*
 * Created on Dec 24, 2005
 * 
 * Copyright 2005 Ganges - Organization for Research
 */
package org.vedantatree.utils;

import java.io.File;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.vedantatree.utils.exceptions.server.ServerBusinessException;


/**
 * This class provides the util methods for string operations.
 * 
 * @author Mohit Gupta
 * @version 1.0
 * 
 */
public final class StringUtils
{

	private static Log LOGGER = LogFactory.getLog( StringUtils.class );

	/**
	 * Constructs the StringUtils
	 * This is made private to restrict its use to
	 * singleton class.
	 */
	private StringUtils()
	{
		/**
		 * Nothing to do here.
		 */
	}

	/**
	 * Creates an empty string of given length.
	 * 
	 * @param length the length of string.
	 * @return the empty string.
	 */
	public final static String blankString( int length )
	{
		StringBuffer buffer = new StringBuffer( length );
		for( int i = 0; i < length; i++ )
		{
			buffer.append( ' ' );
		}
		return buffer.toString();
	}

	/**
	 * Gets the last token for spcefied dlimiter and string.
	 * 
	 * @param value the string to parse
	 * @param delimiter the delimiter used for parsing.
	 * @return
	 */
	public final static String getLastToken( String value, String delimiter )
	{
		String result = null;
		StringTokenizer tokenizer = new StringTokenizer( value, delimiter );
		while( tokenizer.hasMoreElements() )
		{
			result = (String) tokenizer.nextElement();
		}
		return result;
	}

	public static void assertQualifiedArgument( String attribute, String attributeName )
	{
		if( !isQualifiedString( attribute ) )
		{
			IllegalArgumentException iae = new IllegalArgumentException(
					"Null or zero length value found. attribute-name-message[" + attributeName + "] value[" + attribute
							+ "]" );
			LOGGER.error( iae );
			throw iae;
		}
	}

	public static void assertQualifiedArgument( String attribute )
	{
		if( !isQualifiedString( attribute ) )
		{
			IllegalArgumentException iae = new IllegalArgumentException(
					"Null or zero length string found. str[" + attribute + "]" );
			LOGGER.error( iae );
			throw iae;
		}
	}

	/**
	 * This method will check for the null string
	 * 
	 * @param str to be check
	 * @return true if string is not null
	 */
	public static boolean isQualifiedString( String str )
	{
		return str != null && str.trim().length() > 0;
	}

	public static List<String> getTokenizedString( String strToTokenize, String delimiter )
	{
		assertQualifiedArgument( strToTokenize );
		Utilities.assertNotNullArgument( delimiter );

		StringTokenizer st = new StringTokenizer( strToTokenize, delimiter );
		if( st.countTokens() <= 0 )
		{
			return null;
		}
		List<String> tokens = new ArrayList<String>( st.countTokens() );
		while( st.hasMoreTokens() )
		{
			tokens.add( st.nextToken() );
		}
		return tokens;
	}

	/**
	 * To get the String after "." symbol.
	 * 
	 * @param str String with "."
	 * @return string after Dot"."
	 */
	public static String getSimpleClassName( String str )
	{
		int index = str.lastIndexOf( "." );
		if( index < 0 )
		{
			return str;
		}
		str = str.substring( index + 1, str.length() );
		return str;
	}

	/**
	 * toGenericString(Object) is a general toString method,
	 * 
	 * @author saundaraya gupta
	 * @param obj is the object for which toString method is called
	 * @return String toString representation
	 */
	public static String toGenericString( Object obj )
	{
		/*
		 * getting the class object of the obj object
		 */
		Class cl = obj.getClass();

		/*
		 * declaring a StringBuffer object, that will hold the toString representatino of the object
		 */
		StringBuffer sb = new StringBuffer();

		/*
		 * appending the name of the class represented by obj
		 */
		sb.append( cl.getName() );

		/*
		 * appending initial opening bracket in toString representation
		 */
		sb.append( "[" );

		/*
		 * getting the array of all declared fields in the class
		 */
		Field[] fields = cl.getDeclaredFields();

		/*
		 * setting the accessibility flag to true for fields array
		 */
		AccessibleObject.setAccessible( fields, true );

		/*
		 * iterating the fields array elements
		 */
		label1: for( Field field : fields )
		{

			/*
			 * if the field is static we do not include it in toString representation
			 */
			if( !Modifier.isStatic( field.getModifiers() ) )
			{

				/*
				 * if field is not the first field
				 */
				if( !sb.toString().endsWith( "[" ) )
					sb.append( "," );
				/*
				 * getting the name of the field
				 */
				sb.append( field.getName() + "=" );
				try
				{
					/*
					 * getting the type of the field
					 */
					Class fieldType = field.getType();

					/*
					 * checking if instance field is an array , if so appending its size
					 */
					if( fieldType.isArray() )
					{

						sb.append( fieldType.getComponentType() + "[" + Array.getLength( field.get( obj ) ) + "]" );
						/*
						 * continue with the loop if match occurs, skiping rest of the statement in the loop
						 */
						continue;
					}

					/*
					 * getting all the interfaces of Field obejct
					 */
					Class[] interfaces = fieldType.getInterfaces();

					/*
					 * iterating through the interfaces array
					 */
					for( Class interfaceType : interfaces )
					{

						/*
						 * checking if field object implements Collection interface
						 */
						if( interfaceType.equals( Collection.class ) )
						{
							/*
							 * getting the size() method of filed object
							 */
							Method method = fieldType.getMethod( "size" );
							/*
							 * appending the size of Collection
							 */
							sb.append( fieldType.toString() + "[" + method.invoke( field.get( obj ) ) + "]" );

							/*
							 * continuing with outer for loop
							 */
							continue label1;
						}
					}

					/*
					 * if the field is nither array or Collection type than appending its value, if the field happens to
					 * be another object, then its toString() function is called
					 */
					Object val = field.get( obj );
					sb.append( val );

				}
				catch( Exception e )
				{
					LOGGER.error( e );
					throw new IllegalStateException( e.getMessage(), e );
				}

			}
		}

		/*
		 * concatenating the last closing bracket in the toString
		 */
		sb.append( "]" );
		return sb.toString();
	}

	public static String getFileSystemPathFromCommaSeparatedString( String commaSeparatedPath )
	{
		assertQualifiedArgument( commaSeparatedPath );
		List<String> tokens = getTokenizedString( commaSeparatedPath, "," );
		StringBuffer fileSystemPath = new StringBuffer( File.separator );
		for( String string : tokens )
		{
			String systemPathToken = string;
			fileSystemPath.append( systemPathToken );
			fileSystemPath.append( File.separator );
		}
		return fileSystemPath.toString();
	}

	public static StringBuffer collectionToString( Collection collection )
	{
		Utilities.assertNotNullArgument( collection, "collection" );
		Iterator ite = collection.iterator();
		StringBuffer stringBuffger = new StringBuffer();
		int counter = 0;
		while( ite.hasNext() )
		{
			Object object = ite.next();
			if( counter != 0 )
			{
				stringBuffger = stringBuffger.append( ":" );
			}
			if( object == null )
			{
				stringBuffger = stringBuffger.append( ( "null" ) );
			}
			else
			{
				stringBuffger = stringBuffger.append( ( object ) );
			}
			counter++;
		}
		return stringBuffger;
	}

	public static StringBuffer arrayToString( Object[] objectArray )
	{
		StringBuffer sb = new StringBuffer();
		if( objectArray == null )
		{
			sb.append( ( "Specified Array is null" ) );
		}
		else if( objectArray.length == 0 )
		{
			sb.append( ( "Specified Array is empty" ) );
		}
		else
		{
			for( int i = 0; i < objectArray.length; i++ )
			{
				Object object = objectArray[i];
				if( i != 0 )
				{
					sb = sb.append( ", " );
				}
				sb.append( i + 1 );
				sb.append( " > " );
				if( object == null )
				{
					sb = sb.append( ( "null" ) );
				}
				else
				{
					sb = sb.append( ( object ) );
				}
			}
		}
		return sb;
	}

	/*
	 * Received long value and return in Word for e.g 123 return one hundred twenty three
	 */
	public static String NumericToWord( long number ) throws RemoteException, ServerBusinessException
	{

		final String[] tensNames =
		{ "", "", "twenty", "thirty", "forty", "fifty", "sixty", "seventy", "eighty", "ninety" };
		final String[] onesNames =
		{ "", "one", "two", "three", "four", "five", "six", "seven", "eight", "nine", "ten", "eleven", "twelve",
				"thirteen", "fourteen", "fifteen", "sixteen", "seventeen", "eighteen", "nineteen" };

		long temp = number;
		long billions = temp / 1000000000;
		temp %= 1000000000;
		long millions = temp / 1000000;
		temp %= 1000000;
		long thousands = temp / 1000;
		temp %= 1000;
		long hundreds = temp / 100;
		temp %= 100;

		StringBuffer result = new StringBuffer( 30 );
		if( billions > 0 )
			result.append( NumericToWord( billions ) + " billon " );
		if( millions > 0 )
			result.append( NumericToWord( millions ) + " million " );
		if( thousands > 0 )
			result.append( NumericToWord( thousands ) + " thousand " );
		if( hundreds > 0 )
			result.append( NumericToWord( hundreds ) + " hundred " );
		if( temp != 0 )
		{
			if( number >= 100 )
				result.append( "and " );
			if( 0 < temp && temp <= 19 )
				result.append( onesNames[(int) temp] );
			else
			{
				long tens = temp / 10;
				long ones = temp % 10;
				result.append( tensNames[(int) tens] + " " );
				result.append( onesNames[(int) ones] );
			}
		}
		String resultant = "";
		if( result != null )
		{
			resultant = result.toString().toUpperCase();
		}
		return resultant;
	}

	public static void main( String[] args )
	{
		System.out.println( StringUtils.getTokenizedString( "hello,h", "," ) );
		Object[] objectArray = new Object[]
		{ 1, 5, "abc", 7 };
		System.out.println( arrayToString( objectArray ) );
	}

}

// public static void assertQualifiedArgument( String attribute, String attributeName )
// {
// if( !isQualifiedString( attribute ) )
// {
// IllegalArgumentException iae = new IllegalArgumentException(
// "Null or zero length value found. attribute-name-message[" + attributeName + "] value[" + attribute
// + "]" );
// LOGGER.error( iae );
// throw iae;
// }
// }
//
// public static void assertQualifiedArgument( String attribute )
// {
// if( !isQualifiedString( attribute ) )
// {
// IllegalArgumentException iae = new IllegalArgumentException(
// "Null or zero length string found. str[" + attribute + "]" );
// LOGGER.error( iae );
// throw iae;
// }
// }
//
/// **
// * Checks whether the string is a qualified string or not.
// * A string is a qualified string if it is not null and contains
// * at least one charcter other than blank character.
// *
// * @param str the string to inspect.
// * @return Returns <code>true</code> is the string is qualified <code>false</code> otherwise.
// */
// public final static boolean isQualifiedString( String str )
// {
// return str != null && !str.trim().equals( "" );
// }
//
