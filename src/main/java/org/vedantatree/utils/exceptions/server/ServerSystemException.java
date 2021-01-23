package org.vedantatree.utils.exceptions.server;

import org.vedantatree.utils.exceptions.SystemException;


/**
 * It is used to indicate any unrecoverable state on server like error from database connection, error due to wrong
 * parameters, error due to wrong call etc. Across the layer, we can communicate the error state through these runtime
 * exceptions, unless the error is not related to business logic.
 * 
 * Currently DAOException is inheriting this exception for DAOLayer. A subclass should be used for every new layer.
 * 
 * Be very careful to choose when we should define our new specialized exception, and when should we use the same
 * exception. Generally specialized versions are required if web tier can perform some specific action based on
 * exception type or if application wants to add some more properties/features to the exception. So going by above
 * guidelines, creating a custom exception for each separate application by extending this type can actually help. It
 * will give a scope to add any new feature or property to custom class anytime in future.
 * 
 * @author Mohit Gupta <mohit.gupta@vedantatree.com>
 */
public class ServerSystemException extends SystemException
{

	private static final long serialVersionUID = 2008020901L;

	public ServerSystemException( int errorCode, String message )
	{
		super( errorCode, message );
	}

	public ServerSystemException( int errorCode, String message, Throwable th )
	{
		super( errorCode, message, th );
	}

}
