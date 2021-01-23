package org.vedantatree.exceptions;

/**
 * It contains all the errorCodes in the system.
 * 
 * User should define the separate block the different segments of codes for their respective modules. Like 0 to 299 are
 * blocked for system and generic errors.
 * 
 * Error codes are mandatory for exceptions extending the ApplicationException and SystemException
 * 
 * ApplicationErrorCodes further will be used to get the localized message from resources. To get an localized message,
 * constant "ER_" will be preappended to error code.
 * 
 * Error codes should not be used generically to distinugish one exception from other untill unless both the exceptions
 * are not related to same class/category
 * 
 * Using error code to distinguish between IO Error and SQL Error is wrong
 * 
 * Using Error Code to check whether SQL Error was due to connection breakup or due to statement failure is fair
 * 
 * @author Mohit Gupta <mohit.gupta@vedantatree.com>
 */
public interface IErrorCodes
{

	static String	LOCALIZED_MESSAGE_QUALIFIER		= "ER_";

	// 0 - 299 ARE BLOCKED FOR SYSTEM AND GENERIC EXCEPTIONS
	static int		UNDEFINED						= 0;

	static int		EXTERNAL_SERVICE_1_DOWN			= 1;
	static int		EXTERNAL_SERVICE_2_DOWN			= 2;

	static int		ILLEGAL_ARGUMENT_ERROR			= 3;
	static int		SQL_STATEMENT_ERROR				= 4;
	static int		DATABASE_CONNECTION_ERROR		= 5;

	// RESOURSE can be anything like file, network connection etc
	static int		RESOURCE_NOT_FOUND				= 6;

	static int		JAVA_NAMING_ERROR				= 7;

	static int		IO_ERROR						= 8;
	static int		COMPONENT_INITIALIZATION_ERROR	= 9;
	static int		SESSION_OPEN_ERROR				= 10;
	static int		SESSION_CLOSE_ERROR				= 11;

	static int		ILLEGAL_STATE_ERROR				= 12;
	static int		SERVER_SYSTEM_ERROR				= 13;
	static int		AUTHENTICATION_FAILURE			= 14;
	static int		ILLEGAL_ACCESS_ERROR			= 15;

	static int		XML_PARSING_ERROR				= 16;

	static int		DAO_ERROR						= 17;

	static int		INSTANTIATION_ERROR				= 17;
	static int		CLASS_NOT_FOUND_ERROR			= 18;

	static int		CHILD_RECORD_FOUND				= 19;
	static int		UNIQUE_CONSTRAINT_FAILED		= 20;
	static int		MAIL_CONFIGURATION_FAILED		= 21;
	static int		PROPERTY_NOT_FOUND				= 22;

	static int		VALIDATION_FAILURE				= 23;

	static int		FORMAT_OF_DATE_WRONG			= 24;

}
