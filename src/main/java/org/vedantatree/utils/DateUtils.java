package org.vedantatree.utils;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.vedantatree.exceptions.ApplicationException;
import org.vedantatree.exceptions.IErrorCodes;


public class DateUtils
{

	private static Log		LOGGER			= LogFactory.getLog( DateUtils.class );

	public static final int	UNPARSABLE_DATE	= 1221;

	/**
	 * This Method will receivced the String date and test that provided date is a valid date or not
	 * 
	 * @param inDate
	 * @return
	 */
	public static boolean isValidDate( String inDate )
	{

		if( inDate == null )
			return false;

		// set the format to use as a constructor argument
		SimpleDateFormat dateFormat = new SimpleDateFormat( "dd/MM/yyyy" );

		if( inDate.trim().length() != dateFormat.toPattern().length() )
			return false;

		dateFormat.setLenient( false );

		try
		{
			// parse the inDate parameter
			dateFormat.parse( inDate.trim() );
		}
		catch( ParseException pe )
		{
			return false;
		}
		return true;
	}

	/**
	 * Converts incoming string to Date object.
	 * 
	 * @param dateTime: date in string format dd/MM/yyyy
	 * @return Date
	 * @throws ECTException
	 */
	public static Date convertStringToDate( String dateString )
	{
		LOGGER.trace( "entering :convertStringToDate . dateString [" + dateString + "]" );

		if( dateString == null || dateString.length() == 0 )
		{
			LOGGER.debug( "Returning null date" );
			return null;
		}
		SimpleDateFormat sdf = new SimpleDateFormat( "dd/MM/yyyy" );
		Date date = null;
		try
		{
			date = sdf.parse( dateString );
		}
		catch( ParseException e )
		{
			LOGGER.error( "Date Time provided not Valid [ " + e + "]" );
			/*
			 * @TODO throw exception
			 */
		}

		return date;
	}

	/**
	 * Converts incoming string to Timestamp object.
	 * 
	 * @param dateTime: date in string format dd/MM/yyyy
	 * @return Date
	 * @throws ApplicationException
	 * @throws ECTException
	 * @author Vikas
	 */
	public static Timestamp convertStringToTimeStamp( String dateString ) throws ApplicationException
	{
		LOGGER.trace( "entering :convertStringToTimestamp . dateString [" + dateString + "]" );

		if( dateString == null || dateString.length() == 0 )
		{
			LOGGER.debug( "Returning null date" );
			return null;
		}
		SimpleDateFormat sdf = new SimpleDateFormat( "dd/MM/yyyy" );
		Date date = null;
		try
		{
			date = sdf.parse( dateString );
		}
		catch( ParseException e )
		{
			LOGGER.error( "Date Time provided not Valid [ " + e + "]" );
			ApplicationException ae = new ApplicationException( UNPARSABLE_DATE, "Erroe While Parse Date" );
			LOGGER.debug( ae );
			throw ae;
		}

		return new Timestamp( date.getTime() );
	}

	public static Date convertStringToDates( String dateString ) throws ParseException
	{
		LOGGER.trace( "entering :convertStringToDate . dateString [" + dateString + "]" );

		if( dateString == null || dateString.length() == 0 )
		{
			LOGGER.debug( "Returning null date" );
			return null;
		}
		SimpleDateFormat sdf = new SimpleDateFormat( "yyyy/MM/dd" );
		Date date = null;
		try
		{
			date = sdf.parse( dateString );
		}
		catch( ParseException e )
		{
			LOGGER.error( "Date Time provided not Valid [ " + e + "]" );
			throw e;
		}

		return date;
	}

	static public long getNoOfDaysBetweenDates( Date firstDate, Date secondDate ) throws IllegalArgumentException
	{
		if( firstDate == null || secondDate == null )
		{
			return 0l;
		}
		long diff = secondDate.getTime() - firstDate.getTime();
		double days = diff / ( 24 * 60 * 60 * 1000 );
		Math.ceil( days );
		return (long) days;
	}

	/**
	 * Converts Date to String <dtK> - date <sFormat> - format-string d/m/Y
	 * 
	 * Format character Y A full numeric representation of a year, 4 digits, examples: 1999 or 2003 m Numeric
	 * representation of a month, with leading zeros, 01 through 12 d Day of the month, 2 digits with leading zeros, 01
	 * to 31 H 24-hour format of an hour with leading zeros, 00 through 23 i Minutes with leading zeros, 00 to 59 s
	 * Seconds, with leading zeros, 00 through 59 Z Seconds, with leading zeros 000 through 999
	 */
	public static String convertDateToString( Date dtK )
	{
		if( dtK == null )
		{
			return null;
		}

		String sDate;
		int nYear, nMonth, nDay, nHour, nMinute, nSecond, nMS;
		Calendar clnK;
		String sf;
		int jc;
		String sFormat = "d/m/Y";

		clnK = Calendar.getInstance( Locale.US );
		clnK.setTime( dtK );
		nYear = clnK.get( Calendar.YEAR );
		nMonth = 1 + clnK.get( Calendar.MONTH );
		nDay = clnK.get( Calendar.DAY_OF_MONTH );
		nHour = clnK.get( Calendar.HOUR_OF_DAY );
		nMinute = clnK.get( Calendar.MINUTE );
		nSecond = clnK.get( Calendar.SECOND );
		nMS = clnK.get( Calendar.MILLISECOND );

		sDate = "";
		for( jc = 0; jc < sFormat.length(); jc++ )
		{
			switch( sFormat.charAt( jc ) )
			{
				case 'Y':
					sDate += nYear;
					break;
				case 'm':
					sf = "" + nMonth;
					if( nMonth < 10 )
						sf = "0" + sf;
					sDate += sf;
					break;
				case 'd':
					sf = "" + nDay;
					if( nDay < 10 )
						sf = "0" + sf;
					sDate += sf;
					break;
				case 'H':
					sf = "" + nHour;
					if( nHour < 10 )
						sf = "0" + sf;
					sDate += sf;
					break;
				case 'i':
					sf = "" + nMinute;
					if( nMinute < 10 )
						sf = "0" + sf;
					sDate += sf;
					break;
				case 's':
					sf = "" + nSecond;
					if( nSecond < 10 )
						sf = "0" + sf;
					sDate += sf;
					break;
				case 'Z':
					sf = "" + nMS;
					if( nMS < 10 )
						sf = "0" + sf;
					sDate += sf;
					break;
				default:
					sDate += sFormat.substring( jc, jc + 1 );
			}
		}

		return sDate;

	}

	/**
	 * This method is added by Vikas to check that provided date lises in provided Fiscal Year Id or not
	 * date is in String and Fiscal Year id is like 2012/2013
	 * 
	 * @param transactionDate
	 * @param fiscalYearId
	 * @return
	 * @throws ApplicationException
	 */
	public static boolean dateLiesInFiscalYear( String transactionDate, String fiscalYearId )
			throws ApplicationException
	{
		StringUtils.isQualifiedString( transactionDate );
		StringUtils.isQualifiedString( transactionDate );

		Timestamp transactionDateTimeStamp = null;
		try
		{
			transactionDateTimeStamp = convertStringToTimeStamp( transactionDate );
		}
		catch( ApplicationException e )
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
			String message = "during conversion of string date [" + transactionDate
					+ "] to timestamp date error occure ";
			ApplicationException ap = new ApplicationException( IErrorCodes.FORMAT_OF_DATE_WRONG, message, e );
			LOGGER.debug( ap );
			throw ap;
		}

		String fiscalYearStringFromDate = calculateFiscalYearByDate( transactionDateTimeStamp );
		LOGGER.debug( "fiscal-year string from date [" + fiscalYearStringFromDate + "] passed fiscal year id ["
				+ fiscalYearId + "]" );
		if( fiscalYearId.equals( fiscalYearStringFromDate ) )
		{
			LOGGER.debug( "fiscal-Year-Id [" + fiscalYearId + "] and FiscalYear From provided date ["
					+ fiscalYearStringFromDate + "] both found same" );
			return true;
		}
		else
		{
			LOGGER.debug( "fiscal-Year-Id [" + fiscalYearId + "] and FiscalYear From provided date ["
					+ fiscalYearStringFromDate + "] both found different " );
			return false;
		}

	}

	/**
	 * Convert String format of date "12/02/2007" to "12-FEB-2007"
	 * 
	 * @param inputDate
	 * @return
	 */
	public static String convertDateToDBDate( String inputDate )
	{

		if( inputDate != null && inputDate.trim().length() > 0 )
		{
			SimpleDateFormat sm = new SimpleDateFormat( "dd-MMM-yy" );
			inputDate = sm.format( DateUtils.convertStringToDate( inputDate ) );
			inputDate = inputDate.toUpperCase();
		}
		return inputDate;
	}

	public static String convertDBDateToDate( Date dbDate )
	{
		String date = "";
		if( dbDate != null )
		{
			SimpleDateFormat sm = new SimpleDateFormat( "dd/MM/yyyy" );
			date = sm.format( dbDate );
			LOGGER.debug( "date in convertDbToDate dbDate[ " + dbDate + " ]converted date[ " + date + " ]" );
		}
		return date;
	}

	public static Date convertStringDateToDate( Date dbDate )
	{
		String date = "";
		if( dbDate != null )
		{
			SimpleDateFormat sm = new SimpleDateFormat( "MM/dd/yyyy" );
			date = sm.format( dbDate );
			LOGGER.debug( "date in convertStringDateToDate dbDate[ " + dbDate + " ]converted date[ " + date + " ]" );
		}
		return convertStringToDate( date );
	}

	public static Date nullifyTime( Date dateTime )
	{
		if( dateTime == null )
		{
			throw new IllegalArgumentException( "Null Date argument could not be nullify" );
		}
		else
		{
			Calendar calender = new GregorianCalendar();
			calender.setTime( dateTime );
			calender.set( 11, 0 );
			calender.set( 12, 0 );
			calender.set( 13, 0 );
			calender.set( 14, 0 );
			return calender.getTime();
		}
	}

	public static String getPreviousFYByStartDateOfFiscalYear( Timestamp date )

	{
		Calendar cal = new GregorianCalendar();
		cal.setTime( date );
		int currentYear = cal.get( Calendar.YEAR );
		int previousYear = currentYear - 1;
		String previousYearString = previousYear + "/" + currentYear;
		LOGGER.debug( "in date util getPreviousFYByStartDateOfFiscalYear result [" + previousYearString + "]" );
		return previousYearString;

	}

	public static String getPreviousToPreviousFYByStartDateOfFiscalYear( Timestamp date )

	{
		Calendar cal = new GregorianCalendar();
		cal.setTime( date );
		int previousYear = cal.get( Calendar.YEAR ) - 1;
		int previousToPreviousYear = cal.get( Calendar.YEAR ) - 2;
		String previousToPreviousYearString = previousToPreviousYear + "/" + previousYear;
		LOGGER.debug( "in date util getPreviousToPreviousFYByStartDateOfFiscalYear result ["
				+ previousToPreviousYearString + "]" );
		return previousToPreviousYearString;

	}

	public static String getNextFYByEndDateOfFiscalYear( Timestamp date )

	{
		Calendar cal = new GregorianCalendar();
		cal.setTime( date );
		int currentYear = cal.get( Calendar.YEAR );
		int nextYear = currentYear + 1;
		String nextYearString = currentYear + "/" + nextYear;
		LOGGER.debug( "in date util getNextFYByStartDateOfFiscalYear result [" + nextYearString + "]" );
		return nextYearString;

	}

	public static String getNextToNextFYByEndDateOfFiscalYear( Timestamp date )

	{
		Calendar cal = new GregorianCalendar();
		cal.setTime( date );
		int nextYear = cal.get( Calendar.YEAR ) + 1;
		int nextToNextYear = cal.get( Calendar.YEAR ) + 2;
		String nextTonextYearString = nextYear + "/" + nextToNextYear;
		LOGGER.debug( "in date util getNextToFYByEndDateOfFiscalYear result [" + nextTonextYearString + "]" );
		return nextTonextYearString;

	}

	/*
	 * * This method will calculate by logic what is the fiscal year string , it calculate by logic
	 * this method considering that F.Y start from JULY and end up on JUNE every year
	 * it will return answer as 2011/2012
	 */
	public static String calculateFiscalYearByDate( Timestamp date )

	{
		Utilities.assertNotNullArgument( date, "date can not be null" );
		LOGGER.debug( " Date passed for calculating F.Y  [" + date + "]" );

		int JULY_MONTH = 6;

		Calendar cal = new GregorianCalendar();
		cal.setTime( date );

		int month = cal.get( Calendar.MONTH );
		int year = cal.get( Calendar.YEAR );
		int dateInt = cal.get( Calendar.DATE );
		LOGGER.debug( "year [" + year + "] month [" + month + "] date [" + dateInt + "]" );
		String fiscalYearLast = null;
		String fiscalYearFirst = null;

		if( month < JULY_MONTH )
		{
			fiscalYearLast = new Integer( year ).toString();
			fiscalYearFirst = new Integer( year - 1 ).toString();

		}
		else
		{
			fiscalYearLast = new Integer( year + 1 ).toString();
			fiscalYearFirst = new Integer( year ).toString();
		}

		String fiscalYear = fiscalYearFirst + "/" + fiscalYearLast;
		LOGGER.debug( "fiscalYear [" + fiscalYear + "]" );

		return fiscalYear;
	}

	public static void main( String[] args )
	{
		// Date date = convertStringToDate( "14/11/2007" );
		// System.out.println( date.toString() );
		// System.out.println( convertDateToString( date ) );
		Date date = convertStringToDate( "30/06/2012" );
		Timestamp currentDate = new Timestamp( date.getTime() );
		System.out.println( calculateFiscalYearByDate( currentDate ) );
		String fiscalYear = calculateFiscalYearByDate( currentDate );
		System.out.println( "Fiscal Year [" + fiscalYear + "]" );

		try
		{
			System.out.println( "Fiscal Year [" + dateLiesInFiscalYear( "30/05/2012", "2011/2012" ) );
		}
		catch( ApplicationException e )
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
