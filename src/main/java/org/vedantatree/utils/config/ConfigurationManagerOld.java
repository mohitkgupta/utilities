/*

 * Created on Jan 1, 2001
 * 
 * Copyright 2005 Ganges - Organization for Research
 */
package org.vedantatree.utils.config;

import java.io.FileInputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.prefs.Preferences;


/**
 * TODO
 * multithreaded concerns
 * exception handling
 * transient value work
 * test case in main method put get put transient value get transient value multiple puts and gets in simultaneous
 * asynchronous threads
 * 
 * Event firing for value changed
 * 
 * @author Mohit Gupta
 */
public class ConfigurationManagerOld
{

	private static final String	CONFIG_ROOT	= "ganges";

	private static final String	NULL_VALUE	= "NULL_VALUE";

	private static Preferences	preferences;

	private static Map			transientValues;

	static
	{
		transientValues = new HashMap();
		preferences = Preferences.systemRoot().node( CONFIG_ROOT );
	}

	private ConfigurationManagerOld()
	{
	}

	public static void setValue( String propertyName, String propertyValue )
	{
		if( !isQualifiedString( propertyName ) )
		{
			throw new IllegalArgumentException(
					"Specified property name is not a valid string! propertyName[" + propertyName + "]" );
		}
		if( propertyValue == null )
		{
			preferences.remove( propertyName );
		}
		else
		{
			preferences.put( propertyName, propertyValue );
		}
	}

	public static String getValue( String propertyName )
	{
		if( !isQualifiedString( propertyName ) )
		{
			throw new IllegalArgumentException(
					"Specified property name is not a valid string! propertyName[" + propertyName + "]" );
		}
		String propertyValue = (String) transientValues.get( propertyName );
		if( propertyValue != null )
		{
			return propertyValue;
		}
		propertyValue = preferences.get( propertyName, NULL_VALUE );
		if( propertyValue.equals( NULL_VALUE ) )
		{
			return null;
		}
		return propertyValue;
	}

	public static void setTransientProperty( String propertyName, String propertyValue )
	{
		if( !isQualifiedString( propertyName ) )
		{
			throw new IllegalArgumentException(
					"Specified property name is not a valid string! propertyName[" + propertyName + "]" );
		}
		if( propertyValue == null )
		{
			transientValues.remove( propertyName );
		}
		else
		{
			transientValues.put( propertyName, propertyValue );
		}
	}

	public static void loadProperties( String file )
	{
		Properties properties = new Properties();
		try
		{
			properties.load( new FileInputStream( file ) );
		}
		catch( Exception e )
		{
			throw new RuntimeException(
					"Problem while accessing specified file for loading the properties. file[" + file + "]" );
		}
		loadProperties( properties );
	}

	public static void loadProperties( Map propertiesMap )
	{
		if( propertiesMap.isEmpty() )
		{
			return;
		}
		Map.Entry entry = null;
		for( Iterator iter = propertiesMap.entrySet().iterator(); iter.hasNext(); )
		{
			entry = (Map.Entry) iter.next();
			setValue( (String) entry.getKey(), (String) entry.getValue() );
		}
	}

	public static boolean isQualifiedString( String str )
	{
		return str != null && str.trim().length() != 0;
	}

	public static void main( String[] args )
	{
		ConfigurationManagerOld.setValue( "prp", "" );
		ConfigurationManagerOld.setTransientProperty( "prp1", "" );
		System.out.println( "prp1: " + ConfigurationManagerOld.getValue( "prp1" ) );
		System.out.println( "prp: " + ConfigurationManagerOld.getValue( "prp" ) );
		System.out.println( "abc: " + ConfigurationManagerOld.getValue( "abc" ) );
	}
}