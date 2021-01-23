package org.vedantatree.utils.exceptions.db;

/**
 * This class explain the scenarion when anyone want to delete some object and the object is being refered by any other
 * object then while deleting this exception will be thrown by the method.
 * 
 * this exception will be thrown by the HibernateUtil class and will be catched by the others who are accessing the
 * method who throws this.
 * 
 * 
 */
public class RelationExistException extends DAOException
{

	private static final long serialVersionUID = 200802126L;

	public RelationExistException( int errorCode, String message )
	{
		super( errorCode, message );
	}

	public RelationExistException( int errorCode, String message, Throwable th )
	{
		super( errorCode, message, th );
	}
}
