/*
 * Created on Jan 1, 2001
 *
 * Copyright 2005 Ganges - Organization for Research
 */
package org.vedantatree.utils.types;

import java.util.HashMap;

import org.vedantatree.utils.JavaUtils;
import org.vedantatree.utils.StringUtils;


/**
 * This class represents the type for value object. Its
 * default implemenation is for java classes.
 */
public class Type
{

	/**
	 * This is the map which contains the generated types.
	 */
	private static final HashMap	TYPE_CACHE	= new HashMap();

	/**
	 * This is the integer type
	 */
	public static final Type		INTEGER		= new Type( (byte) 1 );

	/**
	 * This is the short type.
	 */
	public static final Type		SHORT		= new Type( (byte) 2 );

	/**
	 * This is the long type.
	 */
	public static final Type		LONG		= new Type( (byte) 3 );

	/**
	 * This is the double type.
	 */
	public static final Type		DOUBLE		= new Type( (byte) 4 );

	/**
	 * This is the float type.
	 */
	public static final Type		FLOAT		= new Type( (byte) 5 );

	/**
	 * This is the character type.
	 */
	public static final Type		CHARACTER	= new Type( (byte) 6 );

	/**
	 * This is the boolean type.
	 */
	public static final Type		BOOLEAN		= new Type( (byte) 7 );

	/**
	 * This is the byte type.
	 */
	public static final Type		BYTE		= new Type( (byte) 8 );

	/**
	 * This is the string type.
	 */
	public static final Type		STRING		= new Type( (byte) 9 );

	/**
	 * This is the XML type.
	 */
	public static final Type		XML			= new Type( (byte) 10 );

	/**
	 * This is the objec type (for nulls)
	 */
	public static final Type		OBJECT		= new Type( (byte) 11 );

	/**
	 * This is the type for custome types.
	 */
	public static final Type		ANY_TYPE	= new Type( Byte.MAX_VALUE );

	/**
	 * This is the type for custome types.
	 */
	private static final byte		CUSTOM_TYPE	= Byte.MIN_VALUE;

	/**
	 * This is the type name for this type wrapper.
	 */
	private String					typeName;

	/**
	 * This is the type class for this type wrapper.
	 */
	private Class					typeClass;

	/**
	 * This is the numeric type for this type wrapper.
	 */
	private byte					type;

	/**
	 * Constructs the Type. Used only for the
	 * creation of default types.
	 * 
	 * @param type
	 */
	private Type( byte type )
	{
		this( type, getDefaultName( type ) );
	}

	/**
	 * Constructs the Type
	 * 
	 * @param type
	 * @param typeName
	 */
	protected Type( byte type, String typeName )
	{
		if( !StringUtils.isQualifiedString( typeName ) )
		{
			throw new IllegalArgumentException(
					"Type name is not valid. typeCode[" + type + "] typeName[" + typeName + "]" );
		}
		// Check for default type. Default types can't be overriden.
		// Also it ensures that no duplicate type can be recreated.
		// One can freely use the type by reference check.
		if( TYPE_CACHE.containsKey( typeName ) )
		{
			throw new IllegalArgumentException( "This type is already created." );
		}
		this.type = type;
		this.typeName = typeName;
		TYPE_CACHE.put( typeName, this );

		try
		{
			this.typeClass = Class.forName( typeName );
			if( typeClass.isPrimitive() )
			{
				Class wrapperClass = JavaUtils.convertToWrapper( typeClass );
				new Type( type, wrapperClass.getName() );
			}
		}
		catch( ClassNotFoundException e )
		{
			// do nothing. Some type names are not class names (e.g. Type.XML).
		}

	}

	/**
	 * Gets the type name for default type.
	 * 
	 * @return the default type name
	 */
	private static final String getDefaultName( byte type )
	{
		String typeName = null;
		switch( type )
		{
			case 1:
				typeName = int.class.getName();
				break;
			case 2:
				typeName = short.class.getName();
				break;
			case 3:
				typeName = long.class.getName();
				break;
			case 4:
				typeName = double.class.getName();
				break;
			case 5:
				typeName = float.class.getName();
				break;
			case 6:
				typeName = char.class.getName();
				break;
			case 7:
				typeName = boolean.class.getName();
				break;
			case 8:
				typeName = byte.class.getName();
				break;
			case 9:
				typeName = String.class.getName();
				break;
			case 10:
				typeName = "Type.XML";
				break;
			case 11:
				typeName = Object.class.getName();
				break;
			case Byte.MAX_VALUE:
				typeName = "Type.ANY";
				break;
		}
		return typeName;
	}

	/**
	 * Creates the type for given class
	 * 
	 * @param clazz
	 * @return the type.
	 */
	public static final Type createType( Class clazz )
	{
		return clazz == null ? null : createType( clazz.getName() );
	}

	/**
	 * Creates the type for given type name
	 * 
	 * @param typeName
	 * @return the type.
	 */
	public static final Type createType( String typeName )
	{
		if( !StringUtils.isQualifiedString( typeName ) )
		{
			throw new IllegalArgumentException( "Type name is not valid." );
		}
		// Search from cache.
		Type type = (Type) TYPE_CACHE.get( typeName );
		if( type == null )
		{
			synchronized( TYPE_CACHE )
			{
				type = (Type) TYPE_CACHE.get( typeName );
				if( type == null )
				{
					type = new Type( CUSTOM_TYPE, typeName );
					TYPE_CACHE.put( typeName, type );
				}
			}
		}
		return type;
	}

	/**
	 * Checks whether the type id array type or not.
	 * 
	 * @return <code>true</code> if the type is array type <code>false</code> otherwise
	 */
	public final boolean isArray()
	{
		return getComponentType() != null;
	}

	/**
	 * Gets the component type for this type.
	 * 
	 * @return the component type of this type, valid for
	 *         only array type.
	 */
	public final Type getComponentType()
	{
		Type componentType = null;
		try
		{
			Class clazz = typeClass.getComponentType();
			if( clazz != null )
			{
				componentType = createType( clazz );
			}
		}
		catch( Throwable ex )
		{
		}
		return componentType;
	}

	public final boolean isAssignableFrom( Type type )
	{
		boolean result = false;
		if( type != null && type.getTypeClass() != null && typeClass != null )
			try
			{
				Class thisClass = typeClass;
				Class thatClass = type.getTypeClass();
				result = thatClass.isAssignableFrom( thisClass );
			}
			catch( Exception e )
			{
				throw new RuntimeException( e );
			}
		return result;
	}

	/**
	 * Gets the typeClass
	 * 
	 * @return Returns the typeClass.
	 */
	public final Class getTypeClass()
	{
		return typeClass;
	}

	/**
	 * Gets the typeName
	 * 
	 * @return Returns the typeName.
	 */
	public final String getTypeName()
	{
		return typeName;
	}

	/**
	 * Performs the type equality.
	 */
	@Override
	public boolean equals( Object arg )
	{
		boolean result = false;
		if( arg instanceof Type )
		{
			Type type = (Type) arg;
			result = type.type == this.type;
			if( result )
			{
				result = type.typeName.equals( typeName );
				if( !result )
				{
					if( getTypeClass() != null && getTypeClass().isPrimitive() )
					{
						String newTypeClass = JavaUtils.convertToWrapper( getTypeClass() ).getName();
						result = type.typeName.equals( newTypeClass );
					}
					else if( type.getTypeClass() != null && type.getTypeClass().isPrimitive() )
					{
						String newTypeClass = JavaUtils.convertToWrapper( type.getTypeClass() ).getName();
						result = typeName.equals( newTypeClass );
					}
				}

			}

		}
		return result;
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		return getTypeName();
	}
}