package org.vedantatree.utils.db.orm;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.vedantatree.utils.StringUtils;
import org.vedantatree.utils.Utilities;
import org.vedantatree.utils.exceptions.ObjectNotFoundException;
import org.vedantatree.utils.exceptions.db.ConstraintViolationException;
import org.vedantatree.utils.exceptions.db.DAOException;
import org.vedantatree.utils.exceptions.db.RelationExistException;


public abstract class HibernateDAO
{

	public static Object getObjectById( Serializable id, Class clazz ) throws DAOException, ObjectNotFoundException
	{
		Utilities.assertNotNullArgument( id );
		Utilities.assertNotNullArgument( clazz );

		return HibernateUtil.getObjectById( id, clazz );
	}

	public static Object getObjectById( Serializable id, String className ) throws DAOException, ObjectNotFoundException
	{
		Utilities.assertNotNullArgument( id );
		StringUtils.assertQualifiedArgument( className );

		return HibernateUtil.getObjectById( id, className );
	}

	/**
	 * deletes object based on id and class name
	 * 
	 * @param className String
	 * @param Id long
	 * @throws RelationExistException
	 */
	public static void deleteObjectById( Serializable id, Class clazz ) throws DAOException, RelationExistException
	{
		HibernateUtil.deleteObjectById( clazz, id );
	}

	public static void deleteObject( Object objToDelete ) throws DAOException, RelationExistException
	{
		HibernateUtil.deleteObject( objToDelete );
	}

	public static Object saveObject( Object obj ) throws DAOException, ConstraintViolationException
	{
		return HibernateUtil.saveNewObject( obj );
	}

	// TODO: Need to find out some method using which we can get the id of a
	// persistent object from hibernate itself and hence can avoid to pass the id
	// here
	public static Object updateObject( Object obj ) throws DAOException
	{
		return HibernateUtil.updateObject( obj );
	}

	public static Object saveOrUpdateObject( Object obj ) throws DAOException, ConstraintViolationException
	{
		return HibernateUtil.saveOrUpdateObject( obj );
	}

	public Collection getAllObjectByClass( String className, String whereClause, String orderBy, boolean descending )
			throws DAOException
	{
		return HibernateUtil.getAllObjectsByClassName( className, orderBy, descending ? "true" : "false", whereClause );
	}

	public static Collection getAllObjectByQuery( String className, String searchQueryString ) throws DAOException
	{
		return HibernateUtil.getAllObjectsByClassName( className, searchQueryString );
	}

	public List getObjectsByQuery( String queryString, Map<Object, Object> parameters ) throws DAOException
	{
		return HibernateUtil.getObjectsByQuery( queryString, parameters );
	}

	public static int executeSQLUpdateByPreparedStatement( String sqlQuery, List parameters ) throws DAOException
	{
		return HibernateUtil.executeSQLUpdateByPreparedStatement( sqlQuery, parameters );
	}

	public static boolean executeSQLStoredProcedure( String sqlStoredProcedureQuery, List parameters )
			throws DAOException
	{
		return HibernateUtil.executeSQLStoredProcedure( sqlStoredProcedureQuery, parameters );
	}

}
