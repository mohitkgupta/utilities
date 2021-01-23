/*
 * Created on Oct 5, 2005
 * 
 * Copyright 2005 Ganges - Organization for Research
 */
package org.vedantatree.utils;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.vedantatree.exceptions.ApplicationException;
import org.vedantatree.exceptions.IErrorCodes;
import org.vedantatree.exceptions.SystemException;


/**
 * This class provides the util methods for bean management.
 * One can handle bean using these util method like method invocation by name
 * property accessing by the name and many more.
 * 
 * @author Mohit Gupta
 * @version 1.0
 */
public final class BeanUtils
{

	public static Log LOGGER = LogFactory.getLog( BeanUtils.class );

	/**
	 * Constructs the BeanUtils
	 * This is made private to restrict the use to singlton object
	 * of this class.
	 */
	private BeanUtils()
	{
		/**
		 * Nothing to do here.
		 */
	}

	/**
	 * Checks whether the property is editable or not. Generally it checks
	 * whether the mutator is available for this property or not. A property is
	 * editable iff the property mutator is available.
	 * 
	 * @param beanClass the class of bean whose property is being inspecting.
	 * @param property the name of property to inspect.
	 * @return Returns <code>true</code> if the property is editable else <code>false</code>
	 * @throws IntrospectionException
	 */
	public static final boolean isPropertyEditable( Class beanClass, String property ) throws IntrospectionException
	{
		// Gets the property descriptor for given property
		PropertyDescriptor descriptor = getPropertyDescriptor( beanClass, property );
		return descriptor == null ? false : descriptor.getWriteMethod() != null;
	}

	/**
	 * Gets the return type of property.
	 * 
	 * @param beanClass the class of bean whose property is being inspecting.
	 * @param property the name of property
	 * @return the type of property. Returns <code>null</code> if the property name
	 *         if not found in bean class.
	 * @throws IntrospectionException
	 */
	public static final Class getPropertyType( Class beanClass, String property ) throws IntrospectionException
	{
		// Gets the property descriptor for property name.
		PropertyDescriptor descriptor = getPropertyDescriptor( beanClass, property );
		return descriptor == null ? null : descriptor.getPropertyType();
	}

	/**
	 * Gets the value of property from the bean object.
	 * 
	 * @param bean the object of bean class whose property is to access.
	 * @param property the name of property.
	 * @return the value of property.
	 * @throws IntrospectionException
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 */
	public static final Object getPropertyValue( Object bean, String property )
			throws IntrospectionException, IllegalArgumentException, IllegalAccessException, InvocationTargetException
	{
		Object propertyValue = null;
		PropertyDescriptor descriptor = getPropertyDescriptor( bean.getClass(), property );
		if( descriptor != null && descriptor.getReadMethod() != null )
		{
			propertyValue = descriptor.getReadMethod().invoke( bean, null );
		}
		return propertyValue;
	}

	/**
	 * Sets the value of property to the object of bean
	 * 
	 * @param bean the bean object whose proeprty is to set.
	 * @param property the name of property.
	 * @param value the value to set for the property.
	 * @throws IntrospectionException
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 */
	public static final void setPropertyValue( Object bean, String property, Object value )
			throws IntrospectionException, IllegalArgumentException, IllegalAccessException, InvocationTargetException
	{
		PropertyDescriptor descriptor = getPropertyDescriptor( bean.getClass(), property );
		if( descriptor != null && descriptor.getWriteMethod() != null )
		{
			descriptor.getWriteMethod().invoke( bean, new Object[]
			{ value } );
		}
	}

	/**
	 * Gets the property descriptor for the property name from given class.
	 * 
	 * @param beanClass the class of bean
	 * @param property the name of property.
	 * @return the property descriptor if property name found else return <code>null</code>.
	 * @throws IntrospectionException
	 */
	public static final PropertyDescriptor getPropertyDescriptor( Class beanClass, String property )
			throws IntrospectionException
	{
		BeanInfo beanInfo = Introspector.getBeanInfo( beanClass );
		PropertyDescriptor[] descriptors = beanInfo.getPropertyDescriptors();
		int length = descriptors == null ? 0 : descriptors.length;
		for( int i = 0; i < length; i++ )
		{
			if( descriptors[i].getDisplayName().equals( property ) )
			{
				return descriptors[i];
			}
		}
		return null;
	}

	/**
	 * returns method with methodName from class
	 * 
	 * @param cls
	 * @param methodName
	 * @return Method
	 */
	public static Method getMethod( Class cls, String methodName )
	{

		Method[] methods = cls.getMethods();

		for( Method method : methods )
		{
			if( method.getName().equalsIgnoreCase( methodName ) )
			{
				LOGGER.debug( "Method Names " + method.getName() );
				return method;
			}
		}

		return null;
	}

	/**
	 * Invokes given method for the object and given parameters
	 * 
	 * @param object
	 * @param methodName
	 * @param argumentsList
	 * @return Object
	 */
	public static Object invokeMethod( Object object, String methodName, Object[] argumentsList )
			throws ApplicationException
	{
		LOGGER.trace( "invokeMethod: object[" + object + "] methodName[" + methodName + "] argumentsList["
				+ argumentsList + "]" );

		Class classObject = object.getClass();
		LOGGER.debug( "objectClass [" + classObject + "]" );

		Method method = getMethod( classObject, methodName );
		LOGGER.debug( "method [" + method + "]" );

		Object objectValue = null;
		try
		{
			if( argumentsList != null )
			{
				objectValue = method.invoke( object, argumentsList );
				LOGGER.debug( "objectValue [" + objectValue + "]" );
			}
			else
			{
				objectValue = method.invoke( object, new Object[] {} );
				LOGGER.debug( "objectValue2 [" + objectValue + "]" );
			}
		}
		catch( IllegalArgumentException e )
		{
			LOGGER.error( "Error while invoking method in BeanUtils.invokeMethod", e );
			throw new ApplicationException( IErrorCodes.ILLEGAL_ARGUMENT_ERROR,
					"Error while invoking method in BeanUtils.invokeMethod", e );
		}
		catch( IllegalAccessException e )
		{
			LOGGER.error( "Error while invoking method in BeanUtils.invokeMethod", e );
			throw new ApplicationException( IErrorCodes.ILLEGAL_ACCESS_ERROR,
					"Error while invoking method in BeanUtils.invokeMethod", e );
		}
		catch( InvocationTargetException e )
		{
			LOGGER.error( "Error while invoking method in BeanUtils.invokeMethod", e );
			throw new ApplicationException( IErrorCodes.ILLEGAL_STATE_ERROR,
					"Error while invoking method in BeanUtils.invokeMethod", e );
		}

		LOGGER.trace( "exiting : invokeMethod." );
		return objectValue;
	}

	public static Object newInstance( String className, Class[] argTypes, Object[] args ) throws ApplicationException
	{
		LOGGER.trace( "newInstance: className [" + className + "] argTypes [" + argTypes + "] args [" + args + "]" );
		StringUtils.assertQualifiedArgument( className );
		try
		{
			Class clazz = Class.forName( className );
			LOGGER.debug( "Got Class [" + clazz + "]" );
			Object newInstance = null;
			if( argTypes == null || argTypes.length == 0 )
			{
				newInstance = clazz.newInstance();
			}
			else
			{
				Constructor constructor = clazz.getConstructor( argTypes );
				newInstance = constructor.newInstance( args );
			}

			return newInstance;
		}
		catch( Exception exception )
		{
			LOGGER.error( "Problem while creating new instance for " + className, exception );
			throw new ApplicationException( IErrorCodes.ILLEGAL_STATE_ERROR,
					"Problem while creating new instance for " + className, exception );
		}
	}

	public static Object classSharedInstance( String className )
	{
		LOGGER.trace( "Entering : new classSharedInstance Instance with className [" + className + "]" );
		StringUtils.assertQualifiedArgument( className );
		try
		{
			Class clazz = Class.forName( className );
			LOGGER.debug( "Got Class [" + clazz + "]" );

			Method sharedInstancedMethodName = getMethod( clazz, "getSharedInstance" );
			if( sharedInstancedMethodName == null )
			{
				throw new IllegalStateException( "No method found with name getSharedInstance()" );
			}

			return sharedInstancedMethodName.invoke( null, null );
		}
		catch( Exception exception )
		{
			LOGGER.error( "Problem while get shared instance for " + className, exception );
			throw new SystemException( IErrorCodes.ILLEGAL_STATE_ERROR,
					"Problem while get shared instance for " + className, exception );
		}
	}

}

// public static Object getPropertyValue( Object object, String propertyName ) throws ApplicationException
// {
// Object value = null;
// LOGGER.debug( "Object " + object );
// LOGGER.debug( "propertyName " + propertyName );
// Object returnValue = object;
// try
// {
// Class cls = object.getClass();
// LOGGER.debug( "CLASS NAME[" + cls + "]" );
// String names[] = null;
// Method method = null;
// String tempPropName = "";
// if( propertyName.contains( "." ) )
// {
// LOGGER.debug( "in if of contains with split" );
// tempPropName = propertyName.replace( '.', ':' );
// names = tempPropName.split( ":" );
// LOGGER.debug( "length of names :::" + names.length );
// }
//
// if( names != null )
// {
// for( String name : names )
// {
// propertyName = name;
// LOGGER.debug( "Returned Property Name is :::::" + propertyName );
// method = getMethod( cls, "get" + propertyName );
// returnValue = method.invoke( returnValue, new Object[] {} );
// LOGGER.debug( "returnValue[ " + returnValue + " ]" );
// if( returnValue == null )
// {
// return null;
// }
// else
// {
// cls = returnValue.getClass();
// returnValue = cls.cast( returnValue );
// }
// }
//
// }
// else
// {
// method = getMethod( cls, "get" + propertyName );
// LOGGER.debug( "Got method::::::::[" + method + "]" );
// returnValue = method.invoke( returnValue, new Object[] {} );
//
// if( returnValue == null )
// {
// return null;
// }
//
// }
//
// // LOGGER.debug( "Returned Value " + returnValue );
// if( returnValue instanceof Date )
// {
// value = DateUtils.convertDateToString( (Date) returnValue );
// }
// else
// {
// value = returnValue;
// }
//
// }
// catch( IllegalArgumentException e )
// {
// LOGGER.error( "Illegal Argument Exception Occured in getPropertyValue method" );
// throw new ApplicationException( IErrorCodes.ILLEGAL_ARGUMENT_ERROR,
// "Illegal Argument Exception Occured in getPropertyValue method", e );
// }
// catch( IllegalAccessException e )
// {
// LOGGER.error( "Illegal Acess Exception Occured in getPropertyValue method" );
// throw new ApplicationException( IErrorCodes.ILLEGAL_ACCESS_ERROR,
// "Illegal Argument Exception Occured in getPropertyValue method", e );
// }
// catch( InvocationTargetException e )
// {
// LOGGER.error( "Invocation Target Exception Occured in getPropertyValue method" );
// throw new ApplicationException( IErrorCodes.ILLEGAL_STATE_ERROR,
// "Illegal Argument Exception Occured in getPropertyValue method", e );
// }
//
// // LOGGER.debug( "Value Returned " + value );
// return value;
// }
//
