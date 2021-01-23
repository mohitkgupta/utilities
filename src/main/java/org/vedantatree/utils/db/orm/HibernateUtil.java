package org.vedantatree.utils.db.orm;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Criteria;
import org.hibernate.EntityMode;
import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.LockMode;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.metadata.ClassMetadata;
import org.hibernate.proxy.HibernateProxy;
import org.hibernate.type.Type;
import org.vedantatree.utils.BeanUtils;
import org.vedantatree.utils.StringUtils;
import org.vedantatree.utils.Utilities;
import org.vedantatree.utils.config.ConfigurationManager;
import org.vedantatree.utils.exceptions.IErrorCodes;
import org.vedantatree.utils.exceptions.ObjectNotFoundException;
import org.vedantatree.utils.exceptions.SystemException;
import org.vedantatree.utils.exceptions.db.ConstraintViolationException;
import org.vedantatree.utils.exceptions.db.DAOException;
import org.vedantatree.utils.exceptions.db.RelationExistException;
import org.vedantatree.utils.exceptions.server.ServerSystemException;


/**
 * Utility Class to perform various Hibernate Related Operations. It is a kind of Hibernate DAO which provides all
 * functionalities to interact with database using Hibernate
 * 
 * It provides methods for pagination too (to support value list handler pattern)
 * 
 * @author Mohit Gupta [mohit.gupta@vedantatree.com]
 */

public class HibernateUtil
{

	private static Log					LOGGER						= LogFactory.getLog( HibernateUtil.class );

	public final static String			PROPERTY_NAME				= "PropertyName";
	public final static String			PROPERTY_TYPE				= "PropertyType";
	public final static String			PROPERTY_IS_NULL_ALLOWED	= "PropertyIsNullAllowed";
	private final static String			PROPERTY_VALUE				= "PropertyValue";
	private final static String			POJO_BASE_PATH_PRP			= "hibernate.pojo.basepath";

	/**
	 * It indicates the base path of POJO, where HibernateUtils expects all the Data objects. So developers just need to
	 * pass the class name in most of the methods and HibernateUtils itself append the base path, if required, to object
	 * class name to get the data.
	 * 
	 * However this assumption has been changed now, and developers are encouraged to pass the fully qualified name of
	 * the class.
	 * 
	 * Drawback of previous approach was that HibernateUtils was assuming all data objects in same
	 * package/path, which is practically false.
	 */
	private static String				POJO_PATH;

	private final static String			J2EE_ENV					= "hibernate_j2ee_env";
	private final static String			CONFIG_PATH					= "hibernate.config.path";

	/**
	 * True if we are working in j2ee environment. In j2ee environment, HibernateUtils always ask session factory for
	 * the current opened session. As it is assumed that j2ee environment is integrated with Hibernate and hence listed
	 * session factory will be having an opened session if any transaction is opened at j2ee/beans level
	 * 
	 * If false, it is assumed that application is working in simple non-j2ee environment. Hence every new thread
	 * request is served with new session from session factory. For this, we are maintaining a session cache using
	 * thread local so that same session can be used for multiple calls from same thread.
	 */
	private static boolean				j2eeEnv;
	/**
	 * Cache for sessions. Please refer to details of j2eeEnv instance variable for more details.
	 */
	private static final ThreadLocal	SESSION_CACHE				= new ThreadLocal();

	// private static final String SESSION_FACTORY_JNDI_REF = "hibernate.sessionfactory.jndiref";

	private static final SessionFactory	sessionFactory;

	static
	{
		try
		{
			ConfigurationManager.ensurePropertiesLoaded( "hibernate.properties" );
			LOGGER.debug( "hibernate properties initialized" );

			String j2eeEnvStr = ConfigurationManager.getSharedInstance().getPropertyValue( J2EE_ENV );
			LOGGER.info( "j2eeEnvStr[" + j2eeEnvStr + "]" );

			if( j2eeEnvStr != null && "true".equalsIgnoreCase( j2eeEnvStr ) )
			{
				j2eeEnv = true;
				// sessionFactory = (SessionFactory) JNDILookupManager.lookup( ConfigurationManager.getSharedInstance()
				// .getPropertyValue( SESSION_FACTORY_JNDI_REF ) );
			}
			String configPath = ConfigurationManager.getSharedInstance().getPropertyValue( CONFIG_PATH );
			LOGGER.debug( "configPath[" + configPath + "]" );

			Configuration cfg = new Configuration();
			cfg.configure( configPath );
			sessionFactory = cfg.buildSessionFactory();

			POJO_PATH = ConfigurationManager.getSharedInstance().getPropertyValue( POJO_BASE_PATH_PRP, false );
			LOGGER.info( "pojo_base_path[" + POJO_PATH + "]" );

			sessionFactory.getStatistics().setStatisticsEnabled( true );
			LOGGER.info( "HibernateUtil: J2EE Environement >> " + j2eeEnvStr );

			// can run a dameon thread to print the summary of session factory on log using logsummary method

			// MBean service registration for all SessionFactory's - Need to deploy hibernate as jmx service on server
			// first
			// refer to
			// http://docs.jboss.org/hibernate/core/3.3/reference/en/html/session-configuration.html#configuration-j2ee
			// Hashtable tb = new Hashtable();
			// tb.put("type", "statistics");
			// tb.put("sessionFactory", "all");
			// ObjectName on = new ObjectName("hibernate", tb); // MBean object name
			//
			// StatisticsService stats = new StatisticsService(); // MBean implementation
			// server.registerMBean(stats, on); // Register the MBean on the server
		}
		catch( Throwable th )
		{
			LOGGER.fatal( "problem while initializing the Hiberante Utils.", th );

			throw new SystemException( IErrorCodes.COMPONENT_INITIALIZATION_ERROR,
					"problem while initializing the Hiberante Utils.", th );
		}
	}

	private HibernateUtil()
	{
	}

	// ---------------------------- Session Management -------------------------

	/**
	 * Return the current session.
	 * 
	 * This method should be used only if we are working in J2EE managed environment and are using CMT.
	 * 
	 * @return current session binded with container for current request
	 */
	private static Session getCurrentJ2EESession()
	{
		if( !j2eeEnv )
		{
			throw new SystemException( IErrorCodes.ILLEGAL_STATE_ERROR,
					"This method is made only for j2EE environment. Either you are not working in J2EE env or configuration has not been set properly." );
		}
		return sessionFactory.getCurrentSession();
	}

	/**
	 * Method to get the Hibernate session. User need to close the session.
	 * 
	 * @return hibernate session.
	 * @throws DAOException
	 */

	private static Session getCurrentExplicitSession() throws DAOException
	{
		try
		{
			Session session = (Session) SESSION_CACHE.get();
			if( session == null )
			{
				session = sessionFactory.openSession();
				SESSION_CACHE.set( session );
			}
			return session;
		}
		catch( HibernateException e )
		{
			LOGGER.error( "There is an error in getting session : ", e );
			throw new DAOException( IErrorCodes.SESSION_OPEN_ERROR, "There is an error in getting session : ", e );
		}
	}

	/**
	 * To close the hibernate session.
	 * 
	 * @throws DAOException
	 * 
	 */
	private static void closeCurrentExplicitSession() throws DAOException
	{
		try
		{
			LOGGER.debug( "close session called " );
			Session s = (Session) SESSION_CACHE.get();
			SESSION_CACHE.set( null );
			if( s != null )
			{
				s.close();
			}
		}
		catch( HibernateException e )
		{
			LOGGER.error( "There is an error in close session : ", e );
			throw new DAOException( IErrorCodes.SESSION_CLOSE_ERROR, "There is an error in close session : ", e );
		}
	}

	private static Session getCurrentSession() throws DAOException
	{
		return j2eeEnv ? getCurrentJ2EESession() : getCurrentExplicitSession();
	}

	public static void closeCurrentSession() throws DAOException
	{
		if( !j2eeEnv )
		{
			closeCurrentExplicitSession();
		}
	}

	/**
	 * It flushes the data from current session to database
	 * 
	 * @throws DAOException if there is any problem
	 */
	public static void flush() throws DAOException
	{
		getCurrentSession().flush();
	}

	// ----------------------------- Object Management -------------------------

	public static Serializable getObjectIdentifier( Object obj )
	{
		Utilities.assertNotNullArgument( obj );
		/*
		 * If object is proxy, then get the actual implementation and then use it to get class name.
		 */
		if( obj instanceof HibernateProxy )
		{
			obj = ( (HibernateProxy) obj ).getHibernateLazyInitializer().getImplementation();
		}
		return sessionFactory.getClassMetadata( obj.getClass() ).getIdentifier( obj, EntityMode.POJO );
	}

	/**
	 * returns identity column name for the particular className
	 * 
	 * @param className
	 * @return String
	 * @throws DAOException
	 */
	public static String getIdentifierPropertyName( String className ) throws DAOException
	{
		LOGGER.trace( "getIdentifierPropertyName: className[" + className + "]" );

		className = validateClassName( className );
		ClassMetadata classMetaData = sessionFactory.getClassMetadata( className.trim() );
		String identityName = classMetaData.getIdentifierPropertyName();

		return identityName;
	}

	/**
	 * returns identity column type for the particular className
	 * 
	 * @param className
	 * @return String
	 * @throws DAOException
	 */
	public static String getIdentifierPropertyType( String className ) throws DAOException
	{
		LOGGER.debug( "getIdentifierPropertyType: className[" + className + "]" );

		className = validateClassName( className );
		ClassMetadata classMetaData = sessionFactory.getClassMetadata( className.trim() );

		Type identityType = classMetaData.getIdentifierType();
		return StringUtils.getSimpleClassName( identityType.getReturnedClass().getName() );
	}

	// TODO: Remove the unqualified class name, it should be qualified, that is much easier to implement
	public static Object getObjectById( Serializable id, String className ) throws DAOException, ObjectNotFoundException
	{

		Utilities.assertNotNullArgument( id );
		StringUtils.assertQualifiedArgument( className );

		Class cls = getClassByClassName( className );
		return getObjectById( id, cls );
	}

	public static Object getObjectById( Serializable id, Class clazz ) throws ObjectNotFoundException, DAOException
	{

		LOGGER.trace( "getObjectById: id[ " + id + "] class[ " + clazz + " ]" );
		Utilities.assertNotNullArgument( id );
		Utilities.assertNotNullArgument( clazz );

		try
		{
			Object obj = getCurrentSession().load( clazz, id );
			LOGGER.debug( "obj[" + obj + "]" );
			return obj;
		}
		catch( org.hibernate.ObjectNotFoundException e )
		{

			LOGGER.debug( "Requested object of class[ " + clazz + " ]not found id[ " + id + " ]" );
			throw new ObjectNotFoundException( IErrorCodes.RESOURCE_NOT_FOUND,
					"Requested object of class[ " + clazz + " ]not found id[ " + id + " ]" );
			// , e );
		}
	}

	/**
	 * deletes object based on id and class name
	 * 
	 * @param className String
	 * @param objectId long
	 * @throws DAOException
	 * @throws RelationExistException
	 * @throws HibernateException
	 */
	public static void deleteObject( String className, Long objectId ) throws DAOException, RelationExistException
	{
		LOGGER.debug( "deleteObject: className[ " + className + " ] Id[ " + objectId + " ]" );

		StringUtils.assertQualifiedArgument( className );
		Utilities.assertNotNullArgument( objectId );

		Class cls = getClassByClassName( className );
		deleteObjectById( cls, objectId );
	}

	public static void deleteObjectById( Class clazz, Serializable id ) throws DAOException, RelationExistException
	{
		Object pojoObject = getCurrentSession().load( clazz, id );
		deleteObject( pojoObject );
	}

	/**
	 * deletes object based on id and class name
	 * 
	 * @param className String
	 * @param Id long
	 * @throws DAOException
	 * @throws RelationExistException
	 * @throws HibernateException
	 */
	public static void deleteObject( Object objToDelete ) throws DAOException, RelationExistException
	{
		try
		{
			getCurrentSession().delete( objToDelete );

			/*
			 * Because we were facing the problem of ConstraintViolationException while server was committing the
			 * transaction and flushing the session so we are explicitly flushing the session,so that this exception
			 * will be throw at the time of flushing. and the appropriate action could be taken
			 */
			getCurrentSession().flush();
		}
		catch( ConstraintViolationException e )
		{

			LOGGER.error( "Object: " + objToDelete + " could not be deleted since it is referenced by another object",
					e );
			throw new RelationExistException( IErrorCodes.CHILD_RECORD_FOUND,
					"Object: " + objToDelete + " could not be deleted since it is referenced by another object", e );
		}

	}

	/**
	 * To save given object in the DataBase.
	 * 
	 * @param obj object of POJO class to save in Database.
	 * @throws DAOException
	 * @throws ConstraintViolationException
	 * @throws HibernateException
	 * 
	 */
	public static Object saveNewObject( Object obj ) throws DAOException, ConstraintViolationException
	{
		LOGGER.trace( "entered saveNewObject: obj[" + obj + "]" );
		Utilities.assertNotNullArgument( obj );

		try
		{
			Long id = (Long) getCurrentSession().save( obj );
			Object object = getCurrentSession().load( obj.getClass(), id );
			getCurrentSession().flush();
			return object;
		}
		catch( ConstraintViolationException cve )
		{
			ConstraintViolationException localCVE = new ConstraintViolationException(
					"Constraint Violation exception is faced while saving the object. error-message[" + cve.getMessage()
							+ "]",
					cve, null );
			// , cve.getSQLException() );
			LOGGER.error( localCVE );
			throw localCVE;
		}
		catch( Exception ex )
		{
			DAOException daoEx = new DAOException( IErrorCodes.DAO_ERROR,
					"Error while saving new object. error-message[" + ex.getMessage() + "]", ex );
			LOGGER.error( daoEx );
			throw daoEx;
		}
	}

	/**
	 * To update the given object in the DataBase.
	 * 
	 * @param obj Object
	 * @param id long
	 * @throws DAOException
	 * @throws HibernateException
	 */
	public static Object updateObject( Object obj ) throws DAOException
	{
		LOGGER.trace( "updateObject: obj[" + obj + "]" );
		Utilities.assertNotNullArgument( obj );

		getCurrentSession().lock( obj, LockMode.UPGRADE );
		// getSession().update( obj );

		Serializable id = HibernateUtil.getObjectIdentifier( obj );
		Object object = getCurrentSession().load( obj.getClass(), id );
		return object;
	}

	/**
	 * saves or updates the object values.
	 * 
	 * @param className String
	 * @param listOfObjects List<HashMap<String, String>>
	 * @throws DAOException
	 * @throws ConstraintViolationException
	 * @throws HibernateException
	 */

	public static Object saveOrUpdateObject( Object pojoObject ) throws DAOException, ConstraintViolationException
	{
		LOGGER.trace( "saveOrUpdateObject: pojoObject[ " + pojoObject + " ]" );

		Utilities.assertNotNullArgument( pojoObject );

		Serializable id = getObjectIdentifier( pojoObject );

		if( id == null )
		{
			LOGGER.debug( "newObject-save" );
			return saveNewObject( pojoObject );
		}
		else
		{
			// LOGGER.debug( "persistentObject-Merge" );
			pojoObject = getCurrentSession().merge( pojoObject );
			getCurrentSession().saveOrUpdate( pojoObject );
			return pojoObject;
			// getSession().lock( pojoObject, LockMode.NONE );
		}
	}

	// ------------------------------ Query Management -------------------------

	public static Criteria createCriteria( String criteria ) throws DAOException
	{
		try
		{
			return getCurrentSession().createCriteria( criteria );
		}
		catch( Exception e )
		{
			LOGGER.error( "Error while creating criteria. criteria-specified[" + criteria + "]", e );
			throw new DAOException( IErrorCodes.DAO_ERROR,
					"Error while creating criteria. criteria-specified[" + criteria + "]", e );
		}

	}

	// integer sql type and value
	public static boolean executeSQLStoredProcedure( String sqlStoredProcedureQuery, List parameters )
			throws DAOException
	{
		LOGGER.debug( "executeSQLStoredProcedure: sqlStoredProcedureQuery[" + sqlStoredProcedureQuery + "] parameters["
				+ parameters + "]" );

		StringUtils.assertQualifiedArgument( sqlStoredProcedureQuery );
		CallableStatement callableStmt = null;
		try
		{
			callableStmt = getCurrentSession().connection().prepareCall( sqlStoredProcedureQuery );
			if( parameters != null && parameters.size() > 0 )
			{
				int parameterIndex = 0;
				for( Iterator iterator = parameters.iterator(); iterator.hasNext(); )
				{
					++parameterIndex;
					int parameterType = ( (Integer) iterator.next() ).intValue();
					Object parameterValue = iterator.next();
					// only name based or index based cases are handled
					if( parameterValue == null )
					{
						callableStmt.setNull( parameterIndex, parameterType );
					}
					else if( Types.VARCHAR == parameterType )
					{
						LOGGER.debug( "setting String Prameter. value[" + parameterValue + "] class["
								+ parameterValue.getClass() + "]" );
						if( !( parameterValue instanceof String ) )
						{
							IllegalArgumentException iae = new IllegalArgumentException( "Parameter type is "
									+ parameterType + ", however parameter value is of different type. parameterValue["
									+ parameterValue + "]" );
							LOGGER.error( iae );
							throw iae;
						}
						callableStmt.setString( parameterIndex, (String) parameterValue );
					}
					else if( Types.INTEGER == parameterType )
					{
						LOGGER.debug( "setting Integer Prameter. value[" + parameterValue + "] class["
								+ parameterValue.getClass() + "]" );
						if( !( parameterValue instanceof Integer ) && !( parameterValue instanceof Long ) )
						{
							IllegalArgumentException iae = new IllegalArgumentException( "Parameter type is "
									+ parameterType + ", however parameter value is of different type. parameterValue["
									+ parameterValue + "]" );
							LOGGER.error( iae );
							throw iae;
						}
						if( parameterValue instanceof Integer )
						{
							callableStmt.setInt( parameterIndex, ( (Integer) parameterValue ).intValue() );
						}
						else if( parameterValue instanceof Long )
						{
							callableStmt.setLong( parameterIndex, ( (Long) parameterValue ).longValue() );
						}
					}
					else
					{
						UnsupportedOperationException use = new UnsupportedOperationException(
								"Only Varchar, Integer and Long type parameter is handled as of now. Mohit, please implement others. parameters["
										+ parameters + "]" );
						LOGGER.error( use );
						throw use;
					}
				}
			}

			// callableStmt.registerOutParameter( 1, java.sql.Types.DECIMAL );
			// callableStmt.getBigDecimal( 1 );

			return callableStmt.execute();
		}
		catch( Exception e )
		{
			LOGGER.error( "Error while executing sql update query. sqlStoredProcedureQuery[" + sqlStoredProcedureQuery
					+ "] parameters[" + parameters + "]", e );
			throw new DAOException( IErrorCodes.DAO_ERROR,
					"Error while executing sql update query. sqlStoredProcedureQuery[" + sqlStoredProcedureQuery
							+ "] parameters[" + parameters + "]",
					e );
		}
		finally
		{
			if( callableStmt != null )
			{
				try
				{
					callableStmt.close();
				}
				catch( SQLException e )
				{
					LOGGER.error( "Error while executing sql update query. sqlStoredProcedureQuery["
							+ sqlStoredProcedureQuery + "] parameters[" + parameters + "]", e );
					throw new DAOException( IErrorCodes.DAO_ERROR,
							"Error while executing sql update query. sqlStoredProcedureQuery[" + sqlStoredProcedureQuery
									+ "] parameters[" + parameters + "]",
							e );
				}
			}
		}

	}

	/**
	 * Executes the specified prepared statement sql using the given parameters and return the update count
	 * 
	 * @param sqlQuery SQLQuery for prepared statement
	 * @param parameters Parameters to set in query
	 * @return update count
	 * @throws DAOException If there is any problem in query execution
	 * 
	 * @Deprecated Using prepared statement for one time is not recommended. Use executeSQLUpdate or executeSQLSelect
	 *             instead.
	 */
	public static int executeSQLUpdateByPreparedStatement( String sqlQuery, List parameters ) throws DAOException
	{
		LOGGER.trace( "executeSQLUpdate: sqlQuery[" + sqlQuery + "] parameters[" + parameters + "]" );
		StringUtils.assertQualifiedArgument( sqlQuery );

		PreparedStatement pStmt = null;
		try
		{
			pStmt = getCurrentSession().connection().prepareStatement( sqlQuery );

			if( parameters != null && parameters.size() > 0 )
			{
				int parameterIndex = 0;
				for( Iterator iterator = parameters.iterator(); iterator.hasNext(); )
				{

					++parameterIndex;
					Class parameterType = (Class) iterator.next();
					Object parameterValue = iterator.next();

					// only name based or index based cases are handled
					if( parameterType.isAssignableFrom( String.class ) )
					{
						if( !( parameterValue instanceof String ) )
						{
							IllegalArgumentException iae = new IllegalArgumentException(
									"Parameter type is String, however parameter value is of different type. parameterValue["
											+ parameterValue + "]" );
							LOGGER.error( iae );
							throw iae;
						}
						pStmt.setString( parameterIndex, (String) parameterValue );
					}
					else
					{
						UnsupportedOperationException use = new UnsupportedOperationException(
								"Only String type parameter is handled as of now. Mohit, please implement others. parameters["
										+ parameters + "]" );
						LOGGER.error( use );
						throw use;
					}
				}
			}
			return pStmt.executeUpdate();
		}
		catch( Exception e )
		{
			LOGGER.error( "Error while executing sql update query. queryString[" + sqlQuery + "] parameters["
					+ parameters + "]", e );
			throw new DAOException( IErrorCodes.DAO_ERROR,
					"Error while executing sql update query. queryString[" + sqlQuery + "]", e );
		}
		finally
		{
			if( pStmt != null )
			{
				try
				{
					pStmt.close();
				}
				catch( Exception e )
				{
					LOGGER.error( "Error while executing sql update query. queryString[" + sqlQuery + "] parameters["
							+ parameters + "]", e );
					throw new DAOException( IErrorCodes.DAO_ERROR,
							"Error while executing sql update query. queryString[" + sqlQuery + "]", e );
				}
			}
		}
	}

	/**
	 * Executes the specified sql query and return the list of results
	 * 
	 * @param sqlQuery SQL query
	 * @return List of result. It might be list of objects, or list of array of objects or any other result according to
	 *         query
	 * @throws DAOException if there is any problem in query execution
	 */
	public static List executeSQLSelect( String sqlQuery ) throws DAOException
	{
		LOGGER.trace( "executeSQLSelect: sqlQuery[" + sqlQuery + "]" );
		StringUtils.assertQualifiedArgument( sqlQuery );

		try
		{
			return getCurrentSession().createSQLQuery( sqlQuery ).list();
		}
		catch( HibernateException he )
		{
			DAOException daoEx = new DAOException( IErrorCodes.DAO_ERROR,
					"HibernateException while executing the sql query for selecting the records", he );
			LOGGER.error( daoEx );
			throw daoEx;
		}
	}

	/**
	 * Executes the specified sql query and return the update count if any
	 * 
	 * @param sqlQuery SQL query
	 * @return update count for the query, zero if it does not affect any record
	 * @throws DAOException if there is any problem in query execution
	 */
	public static int executeSQLUpdate( String sqlQuery ) throws DAOException
	{
		LOGGER.trace( "executeSQLUpdate: sqlQuery[" + sqlQuery + "]" );
		StringUtils.assertQualifiedArgument( sqlQuery );

		try
		{
			return getCurrentSession().createSQLQuery( sqlQuery ).executeUpdate();
		}
		catch( HibernateException he )
		{
			DAOException daoEx = new DAOException( IErrorCodes.DAO_ERROR,
					"HibernateException while executing the sql query for updating the records", he );
			LOGGER.error( daoEx );
			throw daoEx;
		}
	}

	public static List getObjectsByQuery( String queryString ) throws DAOException
	{

		LOGGER.trace( "getObjectsByQuery: queryString[" + queryString + "]" );
		return getObjectsByQuery( queryString, null );
	}

	public static List getObjectsByQuery( String queryString, Map<Object, Object> parameters ) throws DAOException
	{

		LOGGER.trace( "getObjectsByQuery: queryString[" + queryString + "]" );
		StringUtils.assertQualifiedArgument( queryString );

		try
		{
			Query query = getCurrentSession().createQuery( queryString );

			if( parameters != null && parameters.size() > 0 )
			{
				for( Object element : parameters.entrySet() )
				{
					Map.Entry<Object, Object> parameterEntry = (Map.Entry<Object, Object>) element;
					// only name based or index based cases are handled
					if( parameterEntry.getKey() instanceof Integer )
					{
						query.setParameter( ( (Integer) parameterEntry.getKey() ).intValue(),
								parameterEntry.getValue() );
					}
					else if( parameterEntry.getKey() instanceof String )
					{
						query.setParameter( ( (String) parameterEntry.getKey() ), parameterEntry.getValue() );
					}
					else
					{
						IllegalArgumentException iae = new IllegalArgumentException(
								"Only name based and index based parameters can be passed, however the parameter keys are neither integer nor String. queryString["
										+ queryString + "] parameters[" + parameters + "]" );
						LOGGER.error(
								"Only name based and index based parameters can be passed, however the parameter keys are neither integer nor String. queryString["
										+ queryString + "] parameters[" + parameters + "]",
								iae );
						throw iae;
					}

				}
			}

			List listObjects = query.list();
			LOGGER.debug( "objectsCount[" + ( listObjects == null ? 0 : listObjects.size() ) + "]" );

			return listObjects;
		}
		catch( Exception e )
		{
			LOGGER.error( "Error while getting list of objects. queryString[" + queryString + "]", e );
			throw new DAOException( IErrorCodes.DAO_ERROR,
					"Error while getting list of objects. queryString[" + queryString + "]", e );
		}

	}

	/**
	 * This method is only for backward compatability, otherwise now we are using fully qualified class name
	 * 
	 * @return
	 * @throws ClassNotFoundException
	 */
	public static Class getClassByClassName( String className ) throws DAOException
	{
		LOGGER.debug( "getClassByClassName: unqualifiedName[ " + className + " ]" );
		StringUtils.assertQualifiedArgument( className );

		Class cls = null;

		try
		{
			// try if it is a qualified name
			cls = Class.forName( className );
		}
		catch( ClassNotFoundException cnfe )
		{
			if( StringUtils.isQualifiedString( POJO_PATH ) )
			{
				// try if pojo path helps
				try
				{
					cls = Class.forName( POJO_PATH + "." + className.trim() );
				}
				catch( ClassNotFoundException e )
				{
					DAOException daoException = new DAOException( IErrorCodes.RESOURCE_NOT_FOUND,
							"No class found for given className[" + className + "]", e );
					LOGGER.error( daoException );
					throw daoException;
				}
			}
			else
			{
				DAOException daoException = new DAOException( IErrorCodes.RESOURCE_NOT_FOUND,
						"No class found for given className[" + className + "]", cnfe );
				LOGGER.error( daoException );
				throw daoException;
			}

		}
		LOGGER.debug( "class[ " + cls.getCanonicalName() + " ]" );
		return cls;
	}

	/**
	 * This method is only for backward compatability, otherwise now we are using fully qualified class name
	 * 
	 * @return
	 * @throws ClassNotFoundException
	 */
	public static String validateClassName( String className ) throws DAOException
	{
		LOGGER.debug( "validateClassName: className[ " + className + " ]" );

		StringUtils.assertQualifiedArgument( className );
		Class cls = getClassByClassName( className );

		return cls.getName();
	}

	public static List getAllObjectsByClassName( String className, String orderBy, String isDescending,
			String completeWhereClause ) throws DAOException
	{
		LOGGER.trace( "getAllObjectsByClassName: className[" + className + "] orderBy[" + orderBy + "] isDescending["
				+ isDescending + "] whereClause[" + completeWhereClause + "]" );

		className = StringUtils.getSimpleClassName( className );
		String queryString = "from " + className + " a";

		if( StringUtils.isQualifiedString( completeWhereClause ) )
		{
			queryString = queryString + completeWhereClause;
		}

		if( StringUtils.isQualifiedString( orderBy ) )
		{
			if( StringUtils.isQualifiedString( isDescending ) && isDescending.equalsIgnoreCase( "true" ) )
			{
				queryString = queryString + " order by a." + orderBy.trim() + " desc";
			}
			else
			{
				queryString = queryString + " order by a." + orderBy.trim();
			}
		}

		return getObjectsByQuery( queryString );
	}

	public static List getAllObjectsByClassName( String className, String searchString ) throws DAOException
	{

		LOGGER.trace( "getAllObjectsByClassName: className[" + className + "] searchString[" + searchString + "]" );

		String whereClause = "";

		if( StringUtils.isQualifiedString( searchString ) )
		{
			whereClause = whereClause + " where " + searchString;
			LOGGER.debug( "WHERE CLAUSE ::::" + whereClause );
		}

		return getAllObjectsByClassName( className, null, null, whereClause );
	}

	public static List getAllObjectsByColumnName( String className, String columnName, String orderBy,
			String isDescending, String completeWhereClause ) throws DAOException
	{

		LOGGER.trace( ( new StringBuilder() ).append( "getAllObjectsByColumnName: className[" ).append( className )
				.append( "] orderBy[" ).append( orderBy ).append( "] isDescending[" ).append( isDescending )
				.append( "] whereClause[" ).append( completeWhereClause ).append( "]" ).toString() );

		String queryString = ( new StringBuilder() ).append( "select a." ).append( columnName ).append( " from " )
				.append( className ).append( " a" ).toString();

		if( StringUtils.isQualifiedString( completeWhereClause ) )
		{
			queryString = ( new StringBuilder() ).append( queryString ).append( completeWhereClause ).toString();
		}

		if( StringUtils.isQualifiedString( orderBy ) )
		{
			if( StringUtils.isQualifiedString( isDescending ) && isDescending.equalsIgnoreCase( "true" ) )
			{
				queryString = ( new StringBuilder() ).append( queryString ).append( " order by a." )
						.append( orderBy.trim() ).append( " desc" ).toString();
			}
			else
			{
				queryString = ( new StringBuilder() ).append( queryString ).append( " order by a." )
						.append( orderBy.trim() ).toString();
			}
		}
		return getObjectsByQuery( queryString );
	}

	// --------------------------- Metadata Management -------------------------

	/**
	 * Returns properties name and type for the particular className
	 * 
	 * @param className
	 * @return List
	 */
	public static List<HashMap<String, String>> getClassMetadataInformation( String className ) throws DAOException
	{

		LOGGER.trace( "getClassMetadataInformation: className[" + className + "]" );

		className = validateClassName( className );
		LOGGER.debug( "qualified-classname[" + className + "]" );

		ClassMetadata classMetaData = sessionFactory.getClassMetadata( className.trim() );

		String[] names = classMetaData.getPropertyNames();
		Type[] types = classMetaData.getPropertyTypes();
		boolean[] nulls = classMetaData.getPropertyNullability();

		List<HashMap<String, String>> properties = new ArrayList<HashMap<String, String>>();

		HashMap<String, String> property;
		for( int i = 0; i < names.length; i++ )
		{

			property = new HashMap<String, String>();
			property.put( PROPERTY_IS_NULL_ALLOWED, "" + nulls[i] );
			property.put( PROPERTY_NAME, names[i] );
			property.put( PROPERTY_TYPE, types[i].getClass().getCanonicalName() );

			properties.add( property );
		}

		return properties;
	}

	// ---------------------------- Pagination Management ----------------------

	/**
	 * Function will return all the object with pagination
	 * 
	 * @param className
	 * @param selectString[] -db names of fields needed from database
	 * @param whereClause -where clause
	 * @param start -Page no. which you want to show if you give 3 then it will show records of 3rd page
	 * 
	 * @param pageSize -Number of records needed on page
	 * @param order By -Order by
	 * @return List of objcets
	 * @throws DAOException
	 */
	public static List getPaginatedData( String className, String[] selectString, String whereClause,
			int pageStartIndex, int pageSize, String orderBy ) throws DAOException
	{
		return getPaginatedData( className, selectString, whereClause, pageStartIndex, pageSize, orderBy,
				Integer.MIN_VALUE );
	}

	/**
	 * Function will return all the object with pagination
	 * 
	 * @param className
	 * @param selectString[] -db names of fields needed from database
	 * @param whereClause -where clause
	 * @param start -Page no. which you want to show if you give 3 then it will show records of 3rd page
	 * 
	 * @param pageSize -Number of records needed on page
	 * @param order By -Order by
	 * @param totalRecords -Total number of records
	 * @return List of objcets
	 * @throws DAOException
	 * @Deprecated This method is deprecated. Please use the one which does not take totalRecords as parameter
	 */
	public static List getPaginatedData( String className, String[] selectString, String whereClause,
			int pageStartIndex, int pageSize, String orderBy, int totalRecords ) throws DAOException
	{

		LOGGER.trace( " getPaginatedData   className[" + className + "] pageSize[" + pageSize + "] totalRecords["
				+ totalRecords + "]" );

		StringUtils.assertQualifiedArgument( className );

		String queryString = "Select ";
		if( selectString != null && selectString.length > 0 )
		{
			for( int i = 0; i < selectString.length; i++ )
			{
				String field = selectString[i];
				LOGGER.debug( "select-field[" + field + "]" );
				if( i == 0 )
				{
					queryString += " a." + field.trim();
				}
				else
				{
					queryString += " ,a." + field.trim();
				}
			}
		}

		queryString = " from " + className.trim() + " a ";

		if( StringUtils.isQualifiedString( whereClause ) )
		{
			queryString += " where " + whereClause;
		}

		if( StringUtils.isQualifiedString( orderBy ) )
		{
			queryString += " order by a." + orderBy.trim();
		}

		LOGGER.debug( "finalQuery[" + queryString + "]" );

		return getPaginatedData( pageStartIndex, pageSize, queryString, totalRecords );
	}

	public static List getPaginatedData( int pageStartIndex, int pageSize, String completeHQL, int totalRecords )
			throws DAOException
	{
		LOGGER.trace( " getPaginatedData:   pageStartIndex[" + pageStartIndex + "] pageSize[" + pageSize + "] hql["
				+ completeHQL + "] totalRecords[" + totalRecords + "]" );

		Query query = getCurrentSession().createQuery( completeHQL );
		int firstResultIndex = 0;

		/*
		 * last page case - Removing the special case of max value. Now pagination manager will pass the last page index
		 * itself
		 */
		// if( pageStartIndex == Integer.MAX_VALUE )
		// {
		// LOGGER.debug( "lastPage-Case" );
		//
		// if( totalRecords != 0 && totalRecords > pageSize )
		// {
		// int totalPage = totalRecords / pageSize;
		// if( ( totalRecords % pageSize ) > 0 )
		// {
		// totalPage++;
		// }
		// LOGGER.debug( "totalPages[" + totalPage + "]" );
		//
		// firstResultIndex = ( pageSize * totalPage ) - pageSize;
		// }
		// LOGGER.debug( "firstResultIndex-lastPage[" + firstResultIndex + "]" );
		// }
		// else
		// {
		if( pageStartIndex == 0 )
		{
			pageStartIndex = 1;
		}

		firstResultIndex = ( pageStartIndex * pageSize ) - pageSize;
		LOGGER.debug( "firstResultIndex-normal[" + firstResultIndex + "]" );
		// }
		query.setFirstResult( firstResultIndex );
		query.setMaxResults( pageSize );
		List listObjects = query.list();

		LOGGER.debug( "paginatedDataSize[" + ( listObjects == null ? 0 : listObjects.size() ) + "]" );
		return listObjects;
	}

	/**
	 * It will return the pagination data as per given page size, start page index using specified SQL query.
	 * 
	 * @param pageStartIndex Start index of page
	 * @param pageSize Number of records for a page
	 * @param completeSQL Complete SQL Query
	 * @return List of objects for specified page number
	 * @throws DAOException If there is any probelm
	 */
	public static List getPaginatedDataBySQL( String completeSQL, int pageStartIndex, int pageSize ) throws DAOException
	{
		LOGGER.trace( "getPaginatedDataBySQL: pageStartIndex[" + pageStartIndex + "] pageSize[" + pageSize + "] hql["
				+ completeSQL + "]" );

		Query query = getCurrentSession().createSQLQuery( completeSQL );
		int firstResultIndex = 0;

		if( pageStartIndex == 0 )
		{
			pageStartIndex = 1;
		}

		firstResultIndex = ( pageStartIndex * pageSize ) - pageSize;
		LOGGER.debug( "firstResultIndex-normal[" + firstResultIndex + "]" );

		query.setFirstResult( firstResultIndex );
		query.setMaxResults( pageSize );
		List listObjects = query.list();

		LOGGER.debug( "paginatedDataSize[" + ( listObjects == null ? 0 : listObjects.size() ) + "]" );
		return listObjects;
	}

	/**
	 * 
	 * @param className
	 * @param whereClause
	 * @return Total number of records
	 * @throws DAOException
	 * @throws HibernateException
	 */
	public static int getTotalNumberOfRecords( String className, String whereClause ) throws DAOException
	{

		LOGGER.trace( "getTotalNumberOfRecords: className[" + className + "] whereClause[" + whereClause + "]" );
		StringUtils.assertQualifiedArgument( className );

		String queryString = "Select count(*) from " + className.trim() + " a ";
		if( StringUtils.isQualifiedString( whereClause ) )
		{
			queryString += " where " + whereClause;
		}

		LOGGER.debug( "query-with-where[" + queryString + "]" );

		Query query = getCurrentSession().createQuery( queryString );

		List listObjects = query.list();
		LOGGER.debug( "resultList[" + listObjects + "]" );

		if( listObjects != null && listObjects.size() > 0 )
		{
			Object resultObject = listObjects.get( 0 );
			LOGGER.debug( "listFirstObject[" + listObjects.get( 0 ) + "]" );

			if( resultObject instanceof Integer )
			{
				LOGGER.debug( "Returning[" + resultObject + "]" );
				return ( (Integer) resultObject ).intValue();

			}
			else if( resultObject instanceof Long )
			{
				LOGGER.debug( "Returning[" + resultObject + "]" );
				return ( (Long) resultObject ).intValue();
			}

			else
			{
				LOGGER.fatal( "First element is not found as integer or Long while getting count of objects" );
				throw new ServerSystemException( IErrorCodes.ILLEGAL_STATE_ERROR,
						"First element is not found as integer while getting count of objects" );
			}
		}
		else
		{
			LOGGER.debug( "Returning 0" );
			return 0;
		}
	}

	/**
	 * This method is used to initialize a hibernate object upto desired depth in object graph
	 * 
	 * @param obj Object to initialize
	 * @param desiredDepth Level up to which we need to initialize the object -1 if no limit 0 means only specified
	 *        object 1...n depth up to which we want to initialize the objects in object graph
	 * 
	 * @throws DAOException
	 */
	public static void initializeObject( Object obj ) throws DAOException
	{
		initializeObject( obj, 0 );
	}

	public static void initializeObject( Object obj, int desiredDepth ) throws DAOException
	{
		Utilities.assertNotNullArgument( obj );
		initializeObjectInternal( obj, new HashSet(), desiredDepth, 0 );
	}

	private static void initializeObjectInternal( Object obj, Set objectsSet, int desiredDepth, int achievedDepth )
			throws DAOException
	{
		LOGGER.debug( "initializing-obj[" + obj + "]" );
		if( objectsSet.contains( obj ) )
		{
			LOGGER.debug( "returning as object already initialized." );
			return;
		}
		Hibernate.initialize( obj );
		if( desiredDepth != -1 && desiredDepth == achievedDepth )
		{
			LOGGER.debug( "returning as desired depth has been achieved. desiredDepth[" + desiredDepth
					+ "] achievedDepth[" + achievedDepth + "]" );
			return;
		}
		achievedDepth++;
		objectsSet.add( obj );
		try
		{
			Class clazz = obj.getClass();
			Field[] fields = clazz.getFields();
			if( fields == null || fields.length == 0 )
			{
				LOGGER.debug( "no field found with object" );
				return;
			}
			for( Field field : fields )
			{
				String propertyName = field.getName();
				LOGGER.debug( "propertyName[" + propertyName + "]" );
				Object propertyValue = BeanUtils.getPropertyValue( obj, propertyName );
				LOGGER.debug( "propertyValue[" + propertyValue + "]" );
				if( propertyValue != null )
				{
					initializeObjectInternal( obj, objectsSet, desiredDepth, achievedDepth );
				}
			}
		}
		catch( Exception ex )
		{
			LOGGER.error( "Error while getting list of objects. obj[" + obj + "]", ex );
			throw new DAOException( IErrorCodes.DAO_ERROR, "Error while initializing the object. obj[" + obj + "]",
					ex );
		}
	}

	// -------------------------- End ------------------------------------------

	public static void main( String[] args )
	{
		String completeHql = "  Select a.purchaseRequisitionItemId,a.item.itemCode,"
				+ "a.unitPrice,a.item.unit,a.quantity,a.supplier.supplierName from PurchaseRequisitionItem  a"
				+ " left join a.supplier as Supplier ";

		// String completeHql="Select a.purchaseRequisitionItemId,a.item.itemCode,a.unitPrice,a.item.unit," +
		// "a.quantity,s.supplierName from PurchaseRequisitionItem as a left join Supplier as s" +
		// " with a.supplier.id=s.id ";

		List list = sessionFactory.openSession().createQuery( completeHql ).list();

		System.out.println( "list   " + list.size() );

	}
}

// -------------------------------------------------------------------------------------
//
// * SQL State Value for Constraint Violation -
// * ftp://ftp.software.ibm.com/ps/products/db2/info/vr6/htm/db2m0/db2state.htm#HDRSTTMSG
// * Table 12. Class Code 23: Constraint Violation
// * SQLSTATE Value Meaning
// * 23001 The update or delete of a parent key is prevented by a RESTRICT update or delete rule.
// * 23502 An insert or update value is null, but the column cannot contain null values.
// * 23503 The insert or update value of a foreign key is invalid.
// * 23504 The update or delete of a parent key is prevented by a NO ACTION update or delete rule.
// * 23505 A violation of the constraint imposed by a unique index or a unique constraint occurred.
// * 23510 A violation of a constraint on the use of the command imposed by the RLST table occurred.
// * 23511 A parent row cannot be deleted, because the check constraint restricts the deletion.
// * 23512 The check constraint cannot be added, because the table contains rows that do not satisfy the constraint
// * definition.
// * 23513 The resulting row of the INSERT or UPDATE does not conform to the check constraint definition.
// * 23514 Check data processing has found constraint violations.
// * 23515 The unique index could not be created or unique constraint added, because the table contains duplicate values
// * of the specified key.
// * 23520 The foreign key cannot be defined, because all of its values are not equal to a parent key of the parent
// table.
// * 23521 The update of a catalog table violates an internal constraint.
// *
