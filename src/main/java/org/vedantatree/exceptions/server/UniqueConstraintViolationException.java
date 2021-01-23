package org.vedantatree.exceptions.server;

import org.vedantatree.exceptions.db.DAOException;


/**
 * This Exception class will be used for throwing the exception if we have duplicate records in the database
 * 
 * this exception will be thrown by the HibernateUtil class and will be catched by the others who are accessing the
 * method who throws this.
 * 
 * 
 */
public class UniqueConstraintViolationException extends DAOException
{

	/**
	 * 
	 */
	private static final long serialVersionUID = 5875276155612948694L;

	public UniqueConstraintViolationException( int errorCode, String message )
	{
		super( errorCode, message );
	}

	public UniqueConstraintViolationException( int errorCode, String message, Throwable th )
	{
		super( errorCode, message, th );
	}
}
