package org.vedantatree.utils.exceptions.db;

import org.vedantatree.utils.exceptions.ApplicationException;


// TODO move to server package
public class DAOException extends ApplicationException
{

	private static final long serialVersionUID = 2008020603L;

	public DAOException( int errorCode, String message )
	{
		super( errorCode, message );
	}

	public DAOException( int errorCode, String message, Throwable th )
	{
		super( errorCode, message, th );
	}

}
