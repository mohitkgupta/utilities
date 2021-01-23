package org.vedantatree.utils;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.StringTokenizer;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.vedantatree.utils.exceptions.ApplicationException;
import org.vedantatree.utils.exceptions.IErrorCodes;


/**
 * 
 * @author Mohit Gupta
 * 
 */
public class ResourceFinder
{

	private static final Log LOGGER = LogFactory.getLog( ResourceFinder.class );

	/**
	 * Get CLASSPATH entries.
	 * 
	 * @return List of all entries in the current CLASSPATH.
	 */
	public static Collection getClassPathEntries()
	{
		String classPath = System.getProperty( "java.class.path" );
		logMessage( "classPath[" + classPath + "]" );

		char pathSeparator = ';'; // default
		String ps = System.getProperty( "path.separator" );
		logMessage( "sysPS[" + ps + "]" );
		if( StringUtils.isQualifiedString( ps ) )
		{
			pathSeparator = ps.charAt( 0 );
		}

		logMessage( "pathSeparator[" + pathSeparator + "]" );
		StringTokenizer st = new StringTokenizer( classPath, pathSeparator + "" );
		HashSet<String> result = new HashSet<String>();
		while( st.hasMoreTokens() )
		{
			result.add( st.nextToken() );
		}

		return result;
	}

	/**
	 * Try to open a valid InputStream from various sources (CLASSPATH, Filesystem, Jar, etc.)
	 * 
	 * @param resourceName Name of file to open.
	 * @return InputStream or null if not found/not possible.
	 */
	public static InputStream findResource( String resourceName ) throws ApplicationException
	{
		logMessage( "entered: findResource. resourceName[" + resourceName + "]" );
		StringUtils.assertQualifiedArgument( resourceName );
		try
		{
			URL resourceURL = Thread.currentThread().getContextClassLoader().getResource( resourceName );
			logMessage( "thread-classloader-root[" + Thread.currentThread().getContextClassLoader().getResource( "" )
					+ "]" );
			if( resourceURL != null )
			{
				logMessage(
						resourceName + "found from thread context class loader. url[" + resourceURL.getPath() + "]" );
				return resourceURL.openStream();
			}
			logMessage( resourceName + " not found with context class loader" );

			// try to get from system class loader:
			resourceURL = ClassLoader.getSystemResource( resourceName );
			logMessage( "system-classloader-root[" + ClassLoader.getSystemResource( "" ) + "]" );
			if( resourceURL != null )
			{
				logMessage( resourceName + "found from system class loader. url[" + resourceURL.getPath() + "]" );
				return resourceURL.openStream();
			}
			logMessage( resourceName + " not found with system class loader" );

			// try to load from URL:
			resourceURL = ResourceFinder.class.getResource( resourceName );
			logMessage( "ResourceFinder-class-root[" + ResourceFinder.class.getResource( "" ) );
			if( resourceURL != null )
			{
				logMessage( resourceName + "found from ResourceFinder class. url[" + resourceURL.getPath() + "]" );
				return resourceURL.openStream();
			}
			logMessage( resourceName + " not found with resource finder class" );

			resourceURL = ResourceFinder.class.getClassLoader().getResource( resourceName );
			logMessage( "ResourceFinder-class-classloader-root["
					+ ResourceFinder.class.getClassLoader().getResource( "" ) );
			if( resourceURL != null )
			{
				logMessage(
						resourceName + "found from ResourceFinder class loader. url[" + resourceURL.getPath() + "]" );
				return resourceURL.openStream();
			}
			logMessage( resourceName + " not found with ResourceFinder class loader" );

			// try to load from current working directory:
			File dirFile = new File( resourceName );
			if( dirFile.exists() )
			{
				logMessage( resourceName + "found with working directory. url[" + dirFile.getPath() + "]" );
				return new FileInputStream( resourceName );
			}
			logMessage( resourceName + " not found with current working directory[" + new File( "" ).getPath() + "]" );

			// try to load from user home directory:
			File homeDirFile = new File( System.getProperty( "user.dir" ) + File.separatorChar + resourceName );
			if( homeDirFile.exists() )
			{
				logMessage( resourceName + "found from home directory. url[" + homeDirFile.getPath() + "]" );
				return new FileInputStream( homeDirFile );
			}
			logMessage( resourceName + " not found with user home directory and hence no success." );

			// try to load from CLASSPATH:
			logMessage( resourceName + ": Looking into classpath" );

			Collection classPath = getClassPathEntries();
			logMessage( "going to search in classPath[" + classPath + "]" );
			Iterator iterator = classPath.iterator();
			int i = 0;
			while( iterator.hasNext() )
			{
				++i;
				String classPathFileName = (String) iterator.next();
				logMessage( i + "). classPathEntry[" + classPathFileName + "]" );

				File file = new File( classPathFileName );
				if( file.exists() )
				{
					InputStream is = findResourceFromFileSystem( file, resourceName );
					if( is != null )
					{
						logMessage( resourceName + "found from class path. classPathEntry[" + classPathFileName + "]" );
						return is;
					}
				} // else: file does not exist
			} // next CLASSPATH entry
			logMessage( resourceName + " not found in class path" );

			return null;

		}
		catch( Exception th )
		{ /* ignore */
			LOGGER.error( "Error while loading resource", th );
			throw new ApplicationException( IErrorCodes.RESOURCE_NOT_FOUND, "Error while loading the resource", th );
		}
	}// openInputStream()

	public static InputStream findResourceFromFileSystem( File systemFile, String resourceName )
			throws ApplicationException
	{
		LOGGER.trace( "entered: finding-with-filesystem. systemFile[" + systemFile.getAbsolutePath() + "] resourceName["
				+ resourceName + "]" );
		try
		{
			if( systemFile.isDirectory() )
			{
				// if class path is directory, check if file
				// found along that path
				logMessage( resourceName + ": Looking into directory > " + systemFile.getName() );

				File[] files = systemFile.listFiles();
				File fileEntry;
				InputStream is;
				for( File file : files )
				{
					fileEntry = file;
					is = findResourceFromFileSystem( fileEntry, resourceName );
					if( is != null )
					{
						logMessage( "file-found-from[" + fileEntry.getAbsolutePath() + "]" );
						return is;
					}
				}
				logMessage( resourceName + " not found with directory. [" + systemFile.getAbsolutePath() + "]" );
			}
			else if( systemFile.getName().equals( resourceName ) )
			{
				logMessage( resourceName + "found the same as specified file." );
				return new FileInputStream( systemFile );
			}
			else
			{
				return findResourceFromZip( systemFile, resourceName );
			}
		}
		catch( Throwable th )
		{
			LOGGER.error( "Error while finding resource in files", th );
			throw new ApplicationException( IErrorCodes.RESOURCE_NOT_FOUND, "Error while finding resource in files",
					th );
		}
		return null;
	}

	public static InputStream findResourceFromZip( File compressedFile, String resourceName )
			throws ApplicationException
	{
		logMessage( resourceName + ": Looking into compressed file > " + compressedFile );
		ZipInputStream zipFile = null;
		try
		{
			try
			{
				zipFile = new ZipInputStream( new FileInputStream( compressedFile ) );
			}
			catch( Exception ze )
			{
				LOGGER.info( "Zip file format error while finding the resouce. ", ze );
				return null;
			}

			ZipEntry entry = null;
			for( entry = zipFile.getNextEntry(); entry != null; entry = zipFile.getNextEntry() )
			{
				String zipFileName = entry.getName();
				StringTokenizer tokens = new StringTokenizer( zipFileName, "/" );

				String lastToken = null;
				while( tokens.hasMoreTokens() )
				{
					lastToken = tokens.nextToken();
				}
				if( lastToken != null && lastToken.equals( resourceName.replace( File.separatorChar, '/' ) ) )
				{
					DataInputStream dis = new DataInputStream( zipFile );
					logMessage( "file-found-from-zip-[" + zipFileName + "]" );
					return dis;
				}
			}
			logMessage( resourceName + " not found with zip file. filename[" + compressedFile.getAbsolutePath() + "]" );
		}
		catch( Throwable th )
		{
			LOGGER.error( "Error while finding resource in zip file ", th );
			throw new ApplicationException( IErrorCodes.RESOURCE_NOT_FOUND, "Error while finding resource in zip files",
					th );
		}

		return null;
	}

	private static void logMessage( String message )
	{
		if( LOGGER.isDebugEnabled() )
		{
			LOGGER.debug( "ResourceFinder::    " + message );
		}
	}

	public static void main( String[] args ) throws IOException, ApplicationException
	{

	}
}
