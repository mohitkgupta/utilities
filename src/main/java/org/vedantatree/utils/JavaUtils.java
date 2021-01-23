/*
 * Created on Oct 15, 2005
 * 
 * Copyright 2005 Ganges - Organization for Research
 */
package org.vedantatree.utils;

import java.util.Hashtable;


/**
 * This class provides the util methods for java type and
 * value conversion related.
 * 
 * @author Mohit Gupta
 * 
 * @version 1.0
 */
public final class JavaUtils
{

	/**
	 * This is the container for primitive and wrapper mappings.
	 */
	private static Hashtable	wrappers	= new Hashtable();
	private static Hashtable	primitives	= new Hashtable();

	/**
	 * Filling the primitive and wraper mapping.
	 */
	static
	{
		wrappers.put( int.class, Integer.class );
		wrappers.put( long.class, Long.class );
		wrappers.put( short.class, Short.class );
		wrappers.put( byte.class, Byte.class );
		wrappers.put( double.class, Double.class );
		wrappers.put( float.class, Float.class );
		wrappers.put( boolean.class, Boolean.class );
		wrappers.put( char.class, Character.class );
		wrappers.put( void.class, Void.class );
		primitives.put( java.lang.Integer.class, Integer.TYPE );
		primitives.put( java.lang.Long.class, Long.TYPE );
		primitives.put( java.lang.Short.class, Short.TYPE );
		primitives.put( java.lang.Byte.class, Byte.TYPE );
		primitives.put( java.lang.Double.class, Double.TYPE );
		primitives.put( java.lang.Float.class, Float.TYPE );
		primitives.put( java.lang.Boolean.class, Boolean.TYPE );
		primitives.put( java.lang.Character.class, Character.TYPE );
		primitives.put( java.lang.Void.class, Void.TYPE );
	}

	/**
	 * Constructs the JavaUtils
	 * This constructor is made private to restrict the use
	 * of singleton class.
	 */
	private JavaUtils()
	{
		/**
		 * Nothing to do here.
		 */
	}

	/**
	 * Converts the primitive class to wrapper class.
	 * 
	 * @param type the class to convert
	 * @return the wrapper class if the class is primitive else returns the
	 *         same class.
	 */
	public final static Class convertToWrapper( Class type )
	{
		if( wrappers.keySet().contains( type ) )
		{
			type = (Class) wrappers.get( type );
		}
		return type;
	}

	public static final Class convertToPrimitive( Class type )
	{
		if( primitives.keySet().contains( type ) )
			type = (Class) primitives.get( type );
		return type;
	}
}