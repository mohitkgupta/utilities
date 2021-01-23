package org.vedantatree.utils.exceptions;

/**
 * 
 * 
 * @author Mohit Gupta <mohit.gupta@vedantatree.com>
 */
public class ValidationFailureException extends SystemException
{

	public ValidationFailureException( int errorCode, String message )
	{
		super( errorCode, message );
	}

	public ValidationFailureException( int errorCode, String message, Throwable th )
	{
		super( errorCode, message, th );
	}

	public ValidationFailureException( ApplicationException ae )
	{
		super( ae );
	}

}
