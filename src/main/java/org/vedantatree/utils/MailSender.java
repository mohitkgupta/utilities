package org.vedantatree.utils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.mail.Address;
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.vedantatree.utils.config.ConfigurationManager;
import org.vedantatree.utils.exceptions.ApplicationException;
import org.vedantatree.utils.exceptions.IErrorCodes;
import org.vedantatree.utils.exceptions.SystemException;


/**
 * MailSender component is used to send the mails to intended receivers. It supports the attachments also.
 * 
 * @author
 * @author Mohit Gupta [mohit.gupta@vedantatree.com]
 */

public class MailSender
{

	private static final Log	logger			= LogFactory.getLog( MailSender.class );
	private static final String	SMTP_HOST_NAME	= "SMTP.server";
	private static final String	SMTP_PORT		= "SMTP.port";
	private static final String	SSL_FACTORY		= "javax.net.ssl.SSLSocketFactory";
	private static final String	USERNAME_FROM	= "SMTP.login";
	private static final String	PASSWORD		= "SMTP.password";
	private static final String	SMTP_AUTH		= "SMTP.auth";
	private static final String	SMTP_FALLBACK	= "SMTP.fallback";

	public MailSender() throws ApplicationException
	{
		try
		{
			// ConfigurationManager.ensurePropertiesLoaded( "/conf/mail.properties" );
			ConfigurationManager.ensurePropertiesLoaded( "mail.properties" );
		}
		catch( ApplicationException e )
		{
			ApplicationException ae = new ApplicationException( IErrorCodes.MAIL_CONFIGURATION_FAILED,
					"Configuration file could not initialzied properly during MailSender Initialization or "
							+ "mail.properties file not found. Please check that mail.properties exist in project path, "
							+ "and it is having all required properties defined properly for sending email",
					e );
			logger.error( ae );
			throw ae;
		}
	}

	/**
	 * This method will send the specified message to given recipients.
	 * 
	 * @param recipientEmailIds List of email ids for recipients who should be in 'to' list
	 * @param ccRecipientEmailIds List of email ids for recipients who should be in 'cc' list
	 * @param attachedFiles List of files which should be attached with email message
	 * @param subject Subject of the mail
	 * @param body Body of the email
	 * @return true if mail sent successfully, false otherwise. Mostly false returned if there is any error during
	 *         execution.
	 * @throws SystemException If any of the specified parameter is not correct
	 */
	public boolean sendMail( List<String> recipientEmailIds, List<String> ccRecipientEmailIds,
			List<String> attachedFiles, String subject, String body )
	{
		logger.debug( "sendMail: receipients[" + recipientEmailIds + "] ccRecipients[" + ccRecipientEmailIds
				+ "] subject[" + subject + "]" );

		if( recipientEmailIds == null || recipientEmailIds.size() == 0 )
		{
			SystemException se = new SystemException( IErrorCodes.ILLEGAL_ARGUMENT_ERROR,
					"No receipient email address has been specified. At least, one receipient should be given. receipient["
							+ recipientEmailIds + "]" );
			logger.error( se );
			throw se;
		}

		Properties props = getMailProperties();

		String senderAddress = ConfigurationManager.getSharedInstance().getPropertyValue( USERNAME_FROM );
		if( !StringUtils.isQualifiedString( senderAddress ) )
		{
			SystemException se = new SystemException( IErrorCodes.ILLEGAL_ARGUMENT_ERROR,
					"Sender Email Address is not specified with mail.sender property file. Please correct that." );
			logger.error( se );
			throw se;
		}

		String senderPassword = ConfigurationManager.getSharedInstance().getPropertyValue( PASSWORD );

		try
		{
			Session session = Session.getInstance( props, new SimpleAuthenticator( senderAddress, senderPassword ) );

			MimeMessage mimeMailMessage = new MimeMessage( session );
			mimeMailMessage.setFrom( new InternetAddress( senderAddress.trim() ) );
			mimeMailMessage.setSentDate( new java.util.Date() );
			mimeMailMessage.setSubject( subject );

			// set 'to' recipients
			Address to[] = new InternetAddress[recipientEmailIds.size()];
			for( int i = 0; i < recipientEmailIds.size(); i++ )
			{
				to[i] = new InternetAddress( recipientEmailIds.get( i ).trim() );
			}
			mimeMailMessage.setRecipients( javax.mail.Message.RecipientType.TO, to );

			// set 'cc' recipients if given
			if( ccRecipientEmailIds != null && ccRecipientEmailIds.size() > 0 )
			{
				Address cc[] = new InternetAddress[ccRecipientEmailIds.size()];
				for( int j = 0; j < ccRecipientEmailIds.size(); j++ )
				{
					cc[j] = new InternetAddress( ccRecipientEmailIds.get( j ).trim() );
				}
				mimeMailMessage.setRecipients( Message.RecipientType.CC, cc );
			}

			MimeBodyPart mimePartForBody = new MimeBodyPart();
			mimePartForBody.setText( body );

			MimeMultipart mimeMultipart = new MimeMultipart();
			mimeMultipart.addBodyPart( mimePartForBody );

			// add attachment files if given
			if( attachedFiles != null && attachedFiles.size() > 0 )
			{
				MimeBodyPart mimePartForAttachment = null;
				for( int i = 0; i < attachedFiles.size(); i++ )
				{
					String attachmentFileName = attachedFiles.get( i );

					mimePartForAttachment = new MimeBodyPart();

					FileDataSource filedatasource = new FileDataSource( attachmentFileName );
					mimePartForAttachment.setDataHandler( new DataHandler( filedatasource ) );
					mimePartForAttachment.setFileName(
							attachmentFileName.substring( attachmentFileName.lastIndexOf( File.separatorChar ) + 1 ) );

					mimeMultipart.addBodyPart( mimePartForAttachment );
				}
			}

			mimeMailMessage.setContent( mimeMultipart );

			Transport.send( mimeMailMessage );

			logger.debug( "Mail Sent Successfully to[" + StringUtils.collectionToString( recipientEmailIds ) + "] cc["
					+ ( ccRecipientEmailIds == null ? "null" : StringUtils.collectionToString( ccRecipientEmailIds ) )
					+ "]" );

			return true;
		}
		catch( MessagingException e )
		{
			logger.error( "Error while sending mail. sender[" + senderAddress + "]", e );

			// We are not throwing error from here intentionally, as failure in mail sending operation should not stop
			// the work. Mail sending can be failed due to mail server error or similar. However, we are returning null
			// from here, so that client API can take informed decision.

			// throw new MessagingException(
			// "Error while sending mail from the given address from[" + from +
			// "] could not connect to server",e );

			return false;
		}
	}

	private Properties getMailProperties()
	{
		Properties props = new Properties();
		props.put( "mail.smtp.host", ConfigurationManager.getSharedInstance().getPropertyValue( SMTP_HOST_NAME ) );
		props.put( "mail.smtp.auth", ConfigurationManager.getSharedInstance().getPropertyValue( SMTP_AUTH ) );
		props.put( "mail.smtp.port", ConfigurationManager.getSharedInstance().getPropertyValue( SMTP_PORT ) );
		props.put( "mail.smtp.socketFactory.port",
				ConfigurationManager.getSharedInstance().getPropertyValue( SMTP_PORT ) );
		props.put( "mail.smtp.socketFactory.class", SSL_FACTORY );
		props.put( "mail.smtp.socketFactory.fallback",
				ConfigurationManager.getSharedInstance().getPropertyValue( SMTP_FALLBACK ) );
		return props;
	}

	public static void main( String[] args ) throws ApplicationException
	{
		ConfigurationManager.initialize( "log4j.xml", "conf/mail.properties" );
		List<String> to = new ArrayList<String>();
		to.add( "mohit.gupta@vedantatree.com" ); // specify email address to test

		List<String> cc = new ArrayList<String>();
		cc.add( "mohit.gupta@minecofin.gov.rw" ); // specify email address to test

		List<String> attachmentFiles = new ArrayList<String>();
		attachmentFiles.add( "conf\\log4j.xml" );

		new MailSender().sendMail( to, cc, attachmentFiles,
				"Hi, We wish everybody will be in loving and peaceful state. ", "Testing Mail Sender" );
	}
}

/**
 * An password authenticator for mail sender
 */
class SimpleAuthenticator extends Authenticator
{

	String	username;
	String	password;

	public SimpleAuthenticator( String username, String password )
	{
		this.username = username;
		this.password = password;
	}

	@Override
	public PasswordAuthentication getPasswordAuthentication()
	{
		return new PasswordAuthentication( username, password );
	}

}
