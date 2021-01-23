package org.vedantatree.exceptions;

import java.io.PrintWriter;
import java.io.StringWriter;

import javax.servlet.ServletException;

import org.apache.commons.logging.Log;
import org.vedantatree.utils.Utilities;


/**
 * All generic utilities method related to exceptions should be put here.
 * 
 * @author Mohit Gupta <mohit.gupta@vedantatree.com>>
 */
public class ExceptionUtils
{

	/**
	 * This method takes an exception as an input argument and returns the stacktrace as a string. It also get the stack
	 * traces of nested exceptions and append these to the trace string.
	 * 
	 * @param exception Exception instance to get the stack trace as string
	 */
	public static String getStackTrace( Throwable exception )
	{
		if( exception == null )
		{
			return "Null Exception Object specified";
		}
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter( sw );
		exception.printStackTrace( pw );
		return sw.toString();
	}

	/**
	 * This method takes an exception as an input argument and returns the stacktrace as a string. It also get the stack
	 * traces of nested exceptions and append these to the trace string.
	 * 
	 * @param exception Exception instance to get the stack trace as string
	 */
	public static String getStackTraceForWebPage( Throwable exception )
	{
		if( exception == null )
		{
			return "Null Exception Object specified";
		}
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter( sw );
		fillStackTrace( exception, pw );
		return sw.toString();
	}

	public static void fillStackTrace( Throwable th, PrintWriter s )
	{
		if( th == null )
		{
			s.println( "Null Exception Object specified" );
		}
		synchronized( s )
		{
			s.println( "<b>" );
			s.println( th );
			s.println( "</b>" );
			StackTraceElement[] trace = th.getStackTrace();
			for( StackTraceElement element : trace )
			{
				s.println( "\tat " + element );
				s.println( "<br/>" );
			}

			Throwable ourCause = th.getCause();
			// special handling for servlet exception, as servlet exception does not pass the root cause to generic
			// exception cause variable, but it stores it in local variable which is root cause
			if( ourCause == null && th instanceof ServletException )
			{
				ourCause = ( (ServletException) th ).getRootCause();
			}
			if( ourCause != null )
			{
				printStackTraceAsCause( ourCause, s, trace );
			}
		}
	}

	/**
	 * Print our stack trace as a cause for the specified stack trace.
	 */
	private static void printStackTraceAsCause( Throwable cause, PrintWriter s, StackTraceElement[] causedTrace )
	{
		// Compute number of frames in common between this and caused
		StackTraceElement[] trace = cause.getStackTrace();
		int m = trace.length - 1, n = causedTrace.length - 1;
		while( m >= 0 && n >= 0 && trace[m].equals( causedTrace[n] ) )
		{
			m--;
			n--;
		}
		int framesInCommon = trace.length - 1 - m;

		s.println( "<br/><br/><br/><b>" );
		s.println( "Caused by: " + cause );
		s.println( "</b>" );
		for( int i = 0; i <= m; i++ )
		{
			s.println( "\tat " + trace[i] );
			s.println( "<br/>" );
		}
		if( framesInCommon != 0 )
		{
			s.println( "\t... " + framesInCommon + " more" );
			s.println( "<br/>" );
		}

		// Recurse if we have a cause
		Throwable ourCause = cause.getCause();
		// special handling for servlet exception, as servlet exception does not pass the root cause to generic
		// exception cause variable, but it stores it in local variable which is root cause
		if( ourCause == null && cause instanceof ServletException )
		{
			ourCause = ( (ServletException) cause ).getRootCause();
		}
		if( ourCause != null )
			printStackTraceAsCause( ourCause, s, trace );
	}

	/**
	 * It logs the exception using logging infrastructure.
	 * 
	 * If stated exception is a IException, it checks whether the exception has already been logged or not and logs only
	 * if it is not logged already.
	 * 
	 * @param logger logger to log the error
	 * @param message Message to log
	 * @param exception error to log
	 */
	public static void logException( Log logger, String message, Throwable exception )
	{
		Utilities.assertNotNullArgument( logger );
		if( !( exception instanceof IException ) || !( (IException) exception ).isLogged() )
		{
			logger.error( message, exception );
		}
	}

	public static void main( String[] args )
	{
		String trace = getStackTrace( new Exception( "Hello first exception",
				new Exception( "Hello second exception", new Exception( "Hello third exception" ) ) ) );
		System.out.print( trace );
	}

}
