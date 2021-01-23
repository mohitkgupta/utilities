package org.vedantatree.utils.exceptions;

/**
 * This exception is designed to be used by all components under PBComponents.
 * 
 * <p>
 * Currently it is not extending any functionality, but just giving a specific presentation to exception specific to
 * components. However, in future, it can be extended for any requirements specific to Components.
 * 
 * @author Mohit Gupta [mohit.gupta@vedantatree.com]
 */
public class ComponentException extends ApplicationException
{

	private static final long serialVersionUID = 2008021901L;

	public ComponentException( int errorCode, String message )
	{
		super( errorCode, message );
	}

	public ComponentException( int errorCode, String message, Throwable th )
	{
		super( errorCode, message, th );
	}

}
