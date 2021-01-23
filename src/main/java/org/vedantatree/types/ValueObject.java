/*
 * Created on Jan 1, 2001
 *
 * Copyright 2005 Ganges - Organization for Research
 */
package org.vedantatree.types;

/**
 * This is the value wrapper for any object.
 * Also contains the type information for the ibject.
 */
public class ValueObject
{

	/**
	 * This is the actual value object.
	 */
	private Object	value;

	/**
	 * This is the type of the object.
	 */
	private Type	valueType;

	/**
	 * Constructs the ValueObject
	 * 
	 * @param value
	 * @param valueType
	 */
	public ValueObject( Object value, Type valueType )
	{
		if( valueType == null )
		{
			throw new IllegalArgumentException( "Value type is not valid." );
		}
		this.value = value;
		this.valueType = valueType;
	}

	/**
	 * Gets the value
	 * 
	 * @return the value
	 */
	public Object getValue()
	{
		return value;
	}

	/**
	 * Gets the value type.
	 * 
	 * @return the value type.
	 */
	public Type getValueType()
	{
		return valueType;
	}
}