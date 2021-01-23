package org.vedantatree.utils.exceptions.server;

import org.vedantatree.utils.exceptions.ApplicationException;


/**
 * It is used to indicate any business exception from server.
 * 
 * Any business logic method, implemented with session bean, can through either this error directly or more specific
 * version i.e. extended exception of this class to represent any special case. Like TenderNoValidForLaunching.
 * 
 * Be very careful to choose when we should define our new specialized exception, and when should we use the same
 * exception. Generally specialized versions are required if web tier can perform some specific action based on
 * exception type or if application wants to add some more properties/features to the exception. So going by above
 * guidelines, creating a custom exception for each separate application by extending this type can actually help. It
 * will give a scope to add any new feature or property to custom class anytime in future.
 * 
 * 
 * @author Mohit Gupta <mohit.gupta@vedantatree.com>
 * 
 */
public class ServerBusinessException extends ApplicationException
{

	private static final long serialVersionUID = 2008020902L;

	public ServerBusinessException( int errorCode, String message )
	{
		super( errorCode, message );
	}

	public ServerBusinessException( int errorCode, String message, Throwable th )
	{
		super( errorCode, message, th );
	}

}
