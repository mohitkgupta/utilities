package org.vedantatree.utils.exceptions;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


public class ApplicationException extends Exception implements IException
{

	private static final long	serialVersionUID	= 2008020602L;

	private int					errorCode;
	private boolean				logged;
	private List<Serializable>	parameters			= new ArrayList<Serializable>();

	public ApplicationException( int errorCode, String message )
	{
		super( message );
		this.errorCode = errorCode;
	}

	public ApplicationException( int errorCode, String message, Throwable th )
	{
		super( message, th );
		this.errorCode = errorCode;
	}

	public ApplicationException( ApplicationException ae )
	{
		super( ae.getMessage(), ae.getCause() );
		this.errorCode = ae.getErrorCode();
	}

	public final int getErrorCode()
	{
		return errorCode;
	}

	public final boolean isLogged()
	{
		return logged;
	}

	public final void setLogged( boolean logged )
	{
		this.logged = logged;
	}

	public String getDetailedMessage()
	{
		return "errorCode[" + getErrorCode() + "] :: message[" + super.getMessage() + "] :: parameters[" + parameters
				+ "]";
	}

	public Throwable getRootCause()
	{
		Throwable rootCause = getCause();
		while( rootCause.getCause() != null )
		{
			rootCause = rootCause.getCause();
		}
		return rootCause;
	}

	public IException addMessageParameter( Serializable param )
	{
		parameters.add( param );
		return this;
	}

	public List<Serializable> getMessageParameters()
	{
		return parameters;
	}

}
