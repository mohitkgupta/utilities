package org.vedantatree.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import javax.servlet.ServletContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.xml.DOMConfigurator;
import org.vedantatree.exceptions.ApplicationException;
import org.vedantatree.exceptions.IErrorCodes;
import org.vedantatree.exceptions.SystemException;
import org.vedantatree.utils.ResourceFinder;
import org.vedantatree.utils.StringUtils;
import org.vedantatree.utils.xml.XMLUtils;
import org.w3c.dom.Document;


/**
 * Configures the application with the given properties from the log4j and PublicBooks property files
 * 
 * TODO can put config files with webinf/class in web application
 * Logger removed as sometime pagination manager kind of class may load before initilaization and can call for
 * loadconfigfile
 * 
 * It should keep track of all properties files loaded
 * 
 * @author Mohit Gupta
 */
public class ConfigurationManager
{

	private static final ConfigurationManager	sharedInstance	= new ConfigurationManager();

	private static Properties					properties		= new Properties();

	private static Log							LOGGER;

	private static boolean						initialized;

	private static ServletContext				servletContext;

	private static final int					INFO			= 0;
	private static final int					DEBUG			= 1;
	private static final int					ERROR			= 2;
	private static final int					WARN			= 3;

	/**
	 * Default Constructor.
	 * 
	 */
	private ConfigurationManager()
	{
	}

	/**
	 * ConfigurationManager object and initialises the properties.
	 * 
	 * 
	 * @return ConfigurationManager object
	 * 
	 */
	public static ConfigurationManager getSharedInstance()
	{
		if( !initialized )
		{
			logMessage( "Configuraiton Manager has not been initialized yet.", null, ERROR );
		}
		return sharedInstance;
	}

	public static void initializeWebApplication( ServletContext servletContextParam, String log4jPropertyFileName,
			String configFileNames )
	{

		logMessage( "Initializing configuraiton manager", null, INFO );

		servletContext = servletContextParam;

		StringUtils.assertQualifiedArgument( log4jPropertyFileName );
		StringUtils.assertQualifiedArgument( configFileNames );

		try
		{
			loadLog4jProperties( log4jPropertyFileName );
			ensurePropertiesLoaded( configFileNames );
		}
		catch( Throwable th )
		{
			logMessage( "Failed to initialize configuraiton manager. " + th.getMessage(), th, ERROR );
			throw new SystemException( IErrorCodes.COMPONENT_INITIALIZATION_ERROR,
					"Problem while initializing the ConfigurationManager", th );
		}
		initialized = true;
		logMessage( "Configuraiton manager initialized", null, INFO );
	}

	/**
	 * This method will initialize the configuration manager with log4j and other project configuration properties
	 * which are required before loading any other component.
	 * 
	 * @param log4jPropertyFileName file path of log4j configuration. It is required to be loaded initially,
	 *        otherwise logger will not be initialized and many of the prints won't come
	 * @param configFileNames Paths for various other configuration files which we want to load initially. Path can be
	 *        simple file name, if files are placed in class path or relative to project root.
	 */
	public static void initialize( String log4jPropertyFileName, String configFileNames )
	{

		logMessage( "Initializing configuraiton manager", null, INFO );

		StringUtils.assertQualifiedArgument( log4jPropertyFileName );
		if( !StringUtils.isQualifiedString( configFileNames ) )
		{
			SystemException se = new SystemException( IErrorCodes.COMPONENT_INITIALIZATION_ERROR,
					"No configuration file has been mentioned while initializing the ConfigurationManager" );
			logMessage( se.getMessage(), se, ERROR );
			throw se;
		}

		try
		{
			loadLog4jProperties( log4jPropertyFileName );
			ensurePropertiesLoaded( configFileNames );
		}
		catch( Throwable th )
		{
			SystemException se = new SystemException( IErrorCodes.COMPONENT_INITIALIZATION_ERROR,
					"Problem while initializing the ConfigurationManager", th );
			logMessage( se.getMessage(), se, ERROR );
			throw se;
		}
		initialized = true;
		logMessage( "Configuraiton manager initialized", null, INFO );
	}

	/**
	 * Loads the Log4j.properties.
	 * 
	 * @throws ApplicationException
	 * @throws IOException
	 * 
	 */
	public static void loadLog4jProperties( Object propertyFileInfo ) throws ApplicationException, IOException
	{

		if( propertyFileInfo == null )
		{
			logMessage( "Explicitly setting log4j file path as config/log4j.xml", null, INFO );
			propertyFileInfo = "config/log4j.xml";
		}

		InputStream is = null;
		if( propertyFileInfo instanceof String )
		{

			is = loadConfigurationFile( (String) propertyFileInfo );
			if( is == null )
			{
				logMessage( "Log4j configuraiton file has not been found.", null, ERROR );
				System.err.println( "Log4j configuraiton file has not been found." );
				System.err.println( "Log4j configuraiton file has not been found." );
				System.err.println( "Log4j configuraiton file has not been found." );
				System.err.println( "Log4j configuraiton file has not been found." );
				return;
			}
		}
		else if( propertyFileInfo instanceof InputStream )
		{
			is = (InputStream) propertyFileInfo;
		}
		else
		{
			SystemException se = new SystemException( IErrorCodes.ILLEGAL_ARGUMENT_ERROR,
					"Wrong type of argument type. arg[" + propertyFileInfo + "]" );
			logMessage( se.getMessage(), se, ERROR );
			throw se;
		}

		Document log4jPropertiesDocument = XMLUtils.parseXML( is, null );
		DOMConfigurator.configure( log4jPropertiesDocument.getDocumentElement() );

		LOGGER = LogFactory.getLog( ConfigurationManager.class );
		logMessage( "Log4j properties initializes sucessfully", null, INFO );
	}

	/**
	 * Loads PublicBooks properties.
	 * 
	 * @throws ApplicationException
	 * @throws IOException
	 * 
	 */
	public static void ensurePropertiesLoaded( Object configFilesInfo ) throws ApplicationException
	{
		logMessage( "ensurePropertiesLoaded: configFilesInfo[" + configFilesInfo + "]", null, INFO );
		try
		{
			if( configFilesInfo instanceof String )
			{
				String configFileNames = (String) configFilesInfo;
				List fileNamesEntries = StringUtils.getTokenizedString( configFileNames, "," );
				if( fileNamesEntries == null || fileNamesEntries.size() == 0 )
				{
					SystemException se = new SystemException( IErrorCodes.COMPONENT_INITIALIZATION_ERROR,
							"No file name has been mentioned while requesting to load the properties from the file" );
					logMessage( se, null, ERROR );
					throw se;
				}

				String configFileEntry;
				InputStream is = null;

				for( Iterator iter = fileNamesEntries.iterator(); iter.hasNext(); )
				{
					configFileEntry = (String) iter.next();

					logMessage( "loading-property-file[" + configFileEntry + "]", null, DEBUG );
					is = loadConfigurationFile( configFileEntry );

					if( is == null )
					{
						logMessage(
								"No resource found for specified properties/configuration file > " + configFileEntry,
								null, INFO );
						continue;
					}

					initialized = true;
					properties.load( is );
					logMessage( "loaded-property-file[" + configFileEntry + "]", null, INFO );
				}

			}
			else if( configFilesInfo instanceof Collection )
			{
				Collection configFilesStreams = (Collection) configFilesInfo;
				Object info = null;
				for( Iterator iter = configFilesStreams.iterator(); iter.hasNext(); )
				{
					info = iter.next();
					logMessage( "inputStream-for-properties[" + info + "]", null, DEBUG );
					if( !( info instanceof InputStream ) )
					{
						SystemException se = new SystemException( IErrorCodes.ILLEGAL_ARGUMENT_ERROR,
								"Wrong parameter specified. Expected InputStream in case if we pass config file info as Collection, found["
										+ info + "]" );
						logMessage( se.getMessage(), se, ERROR );
						throw se;
					}
					initialized = true;
					properties.load( (InputStream) info );
				}
			}
			else
			{
				SystemException se = new SystemException( IErrorCodes.ILLEGAL_ARGUMENT_ERROR,
						"The specified parameters are not as expected. Expected type is either a comma separated string of file names, or a collection of input streams. Found["
								+ configFilesInfo + "]" );
				logMessage( se.getMessage(), se, ERROR );
				throw se;
			}
		}
		catch( IOException ioe )
		{
			ApplicationException ae = new ApplicationException( IErrorCodes.IO_ERROR,
					"Problem while loading config file", ioe );
			logMessage( ae.getMessage(), ae, ERROR );
			throw ae;
		}

		logMessage( "properties-loaded[" + properties + "]", null, INFO );
	}

	public static InputStream loadConfigurationFile( String configFileName ) throws ApplicationException
	{
		InputStream is = null;
		logMessage( "servletContext[" + servletContext + "]", null, DEBUG );
		if( servletContext != null )
		{
			is = servletContext.getResourceAsStream( configFileName );
		}

		if( is == null )
		{
			try
			{
				is = new FileInputStream( configFileName );
				logMessage( "file found at [" + configFileName + "]", null, DEBUG );
			}
			catch( FileNotFoundException e )
			{
				logMessage( "File not found at path[" + configFileName + "]", null, INFO );
				URL url = Thread.currentThread().getContextClassLoader().getResource( "" );
				if( url != null )
				{
					String path = url.getPath() + configFileName;
					path = path.substring( 1 );

					/*
					 * The following decoding and path redefined was needed as there were problems in
					 * file path on weblogic. The final path without this fix was carrying /\ on windows.
					 */
					try
					{
						path = URLDecoder.decode( path, "utf-8" );
					}
					catch( UnsupportedEncodingException ex )
					{
						logMessage( "The path has some encoding which is not supported[" + path + "]", null, INFO );
					}
					path = new File( path ).getPath();
					System.out.println( "The modified path is == " + path );

					logMessage( "Trying to find path at [" + path + "]", null, DEBUG );
					try
					{
						is = new FileInputStream( path );
						logMessage( "file found at [" + path + "]", null, DEBUG );
					}
					catch( FileNotFoundException fe )
					{
						logMessage( "File not found at path[" + path + "]", null, INFO );
					}
				}
			}
		}
		if( is == null )
		{
			is = ResourceFinder.findResource( configFileName );
		}
		if( is == null )
		{
			logMessage( "Configuraiton file has not been found at any of the expected path. configFile["
					+ configFileName + "]", null, ERROR );
		}
		else
		{
			logMessage( configFileName + "File found by resource finder", null, DEBUG );
		}

		return is;
	}

	/**
	 * It checks whether the specified property value has been existed with Configuration Manager or not
	 * 
	 * @param propertyName Name of the property to check
	 * @return true if exist, false otherwise
	 */
	public boolean containsProperty( String propertyName )
	{
		return getPropertyValue( propertyName, false ) != null;
	}

	/**
	 * Returns the value for the given property value.
	 * 
	 * 
	 * @param propertyName
	 * 
	 * @return propertyValue
	 */
	public String getPropertyValue( String propertyName )
	{
		return getPropertyValue( propertyName, true );
	}

	public Integer getPropertyValueAsInteger( String propertyName )
	{
		String property = getPropertyValue( propertyName );
		return Integer.parseInt( property );
	}

	public String getPropertyValueAsSystemPath( String propertyName )
	{
		String property = getPropertyValue( propertyName );
		return StringUtils.getFileSystemPathFromCommaSeparatedString( property );
	}

	public String getPropertyValue( String propertyName, boolean assertExist )
	{
		if( !initialized && assertExist )
		{
			SystemException se = new SystemException( IErrorCodes.COMPONENT_INITIALIZATION_ERROR,
					"Please initialize the Configuration Manager first." );
			logMessage( se.getMessage(), se, ERROR );
			throw se;
		}

		if( !properties.containsKey( propertyName ) && assertExist )
		{
			SystemException se = new SystemException( IErrorCodes.PROPERTY_NOT_FOUND,
					"No Property has been found for given name. propertyName[" + propertyName + "]" );
			logMessage( se.getMessage(), se, ERROR );
			throw se;
		}

		return properties.getProperty( propertyName );
	}

	/**
	 * Checks for test mode in PublicBooks properties file.
	 * 
	 * @return boolean: true for testMode = true else false
	 */
	public boolean isApplicationRunningInTestingMode()
	{
		if( "true".equalsIgnoreCase( getPropertyValue( "App.TestingMode", false ) ) )
		{
			return true;
		}
		else
		{
			return false;
		}
	}

	private static void logMessage( Object message, Throwable th, int mode )
	{
		if( LOGGER != null )
		{
			switch( mode )
			{
				case INFO:
					if( th != null )
					{
						LOGGER.info( message, th );
					}
					else
					{
						LOGGER.info( message );
					}
					break;
				case DEBUG:
					if( th != null )
					{
						LOGGER.debug( message, th );
					}
					else
					{
						LOGGER.debug( message );
					}
					break;
				case WARN:
					if( th != null )
					{
						LOGGER.warn( message, th );
					}
					else
					{
						LOGGER.warn( message );
					}
					break;
				case ERROR:
					if( th != null )
					{
						LOGGER.error( message, th );
					}
					else
					{
						LOGGER.error( message );
					}
					break;
				default:
					throw new IllegalArgumentException( "Wrong mode has been specified for logging the message" );
			}
		}
		else
		{
			if( mode == ERROR )
			{
				System.err.println( message );
			}
			else
			{
				System.out.println( message );
			}
			if( th != null )
			{
				th.printStackTrace();
			}
		}

	}

	/*
	 * A method to print all the loaded properties
	 */
	public void printProperties()
	{
		if( !initialized )
		{
			logMessage( "Configuration Manager is not initialized yet", null, WARN );
		}
		else
		{
			logMessage( properties, null, DEBUG );
		}
	}
}
