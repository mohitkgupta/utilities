package org.vedantatree.exceptions;

/**
 * Exception of this type will be used if a request is being made for an object, and object is not found.
 * 
 * This exception can be used in context like, some data object is not found in database for some id or some xml object
 * is not found with XML Repository.
 * 
 * @author Mohit Gupta <mohit.gupta@vedantatree.com>
 */

public class ObjectNotFoundException extends ApplicationException
{

	private static final long serialVersionUID = 200802102L;

	public ObjectNotFoundException( int errorCode, String message )
	{
		super( errorCode, message );
	}

	public ObjectNotFoundException( int errorCode, String message, Throwable th )
	{
		super( errorCode, message, th );
	}

}
