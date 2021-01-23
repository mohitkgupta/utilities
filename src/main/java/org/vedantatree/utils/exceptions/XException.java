/*
 * Created on Jan 1, 2001
 *
 * Copyright 2005 Ganges - Organization for Research
 */
package org.vedantatree.utils.exceptions;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * @author Mohit Gupta
 * 
 *         TODO
 *         Need to make provision for mail information. i.e. a user may specify mail
 *         information with the exception.
 * 
 *         We can define a mail appender using log4j framework which can get the
 *         required information from the log4j property file and the exception itself
 *         and can send the mail.
 * 
 *         We need to store the mail information with exception itself. But this will be
 *         optional. If this is stored then it will be used while sending the mail
 *         otherwise the information defined with log4j appender will be used.
 * 
 *         With exception object, a flag need to be set with exception that whether we
 *         should send the error or not.
 */
public class XException extends Exception
{

	protected static final Log		LOGGER					= LogFactory.getLog( XException.class );

	protected static final String	CUSTOMIZED_ERROR_CODE	= "Customized Error Code";

	private int						errorCode				= Integer.MIN_VALUE;

	private boolean					sendMail;

	private String					toAddress;

	private String[]				ccAddresses;

	private String[]				bccAddresses;

	private String					subject;

	public XException( String message, int pErrorCode )
	{
		this( message, pErrorCode, null, false, false );
	}

	public XException( String message, int errorCode, boolean debug, boolean sendMail )
	{
		this( message, errorCode, null, debug, sendMail );
	}

	public XException( String message, int errorCode, Throwable cause )
	{
		this( message, errorCode, cause, false, false );
	}

	public XException( String message, int errorCode, Throwable cause, boolean debug, boolean sendMail )
	{
		super( message, null );
		this.errorCode = errorCode;
		this.sendMail = sendMail;
		if( debug )
		{
			LOGGER.debug( getMessage(), cause );
		}
		else
		{
			LOGGER.error( getMessage(), cause );
		}
	}

	public final int getErrorCode()
	{
		return errorCode;
	}

	/*
	 * TODO: Need to use Resource Bundle here.
	 * Resource Bundle should be used on the basis of error codes.
	 * Message can be treated as error detail for technical usage.
	 * The string loaded from resource bundler using error code should be
	 * displayed to the user as error description.
	 */
	@Override
	public String getMessage()
	{
		return super.getMessage() + ": error-code[" + errorCode + "] code-description[" + getErrorDescription() + "]";
	}

	protected String getErrorDescription()
	{
		return ( errorCode >= ErrorCodes.ERROR_DESCRIPTION.length || errorCode < 0 ) ? CUSTOMIZED_ERROR_CODE
				: ErrorCodes.ERROR_DESCRIPTION[errorCode];
	}

	public void setMailingInformation( String toAddress, String[] ccAddresses, String[] bccAddresses, String subject )
	{
		this.toAddress = toAddress;
		this.ccAddresses = ccAddresses;
		this.bccAddresses = bccAddresses;
		this.subject = subject;
	}

	/**
	 * @return Returns the bccAddresses.
	 */
	public String[] getBccAddresses()
	{
		return bccAddresses;
	}

	/**
	 * @return Returns the ccAddresses.
	 */
	public String[] getCcAddresses()
	{
		return ccAddresses;
	}

	/**
	 * @return Returns the subject.
	 */
	public String getSubject()
	{
		return subject;
	}

	/**
	 * @return Returns the toAddress.
	 */
	public String getToAddress()
	{
		return toAddress;
	}
}