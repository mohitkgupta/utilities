package org.vedantatree.utils.exceptions.db;

import java.sql.SQLException;

import org.vedantatree.utils.StringUtils;


/**
 * 
 * SQL State Value for Constraint Violation -
 * ftp://ftp.software.ibm.com/ps/products/db2/info/vr6/htm/db2m0/db2state.htm#HDRSTTMSG
 * Table 12. Class Code 23: Constraint Violation
 * SQLSTATE Value Meaning
 * 23000 Integrity Constraint Violation - This code sometimes comes in Unique Constraint Violation also
 * 23001 The update or delete of a parent key is prevented by a RESTRICT update or delete rule.
 * 23502 An insert or update value is null, but the column cannot contain null values.
 * 23503 The insert or update value of a foreign key is invalid.
 * 23504 The update or delete of a parent key is prevented by a NO ACTION update or delete rule.
 * 23505 A violation of the constraint imposed by a unique index or a unique constraint occurred.
 * 23510 A violation of a constraint on the use of the command imposed by the RLST table occurred.
 * 23511 A parent row cannot be deleted, because the check constraint restricts the deletion.
 * 23512 The check constraint cannot be added, because the table contains rows that do not satisfy the constraint
 * definition.
 * 23513 The resulting row of the INSERT or UPDATE does not conform to the check constraint definition.
 * 23514 Check data processing has found constraint violations.
 * 23515 The unique index could not be created or unique constraint added, because the table contains duplicate values
 * of the specified key.
 * 23520 The foreign key cannot be defined, because all of its values are not equal to a parent key of the parent table.
 * 23521 The update of a catalog table violates an internal constraint.
 * 
 * @author Mohit Gupta [mohit.gupta@vedantatree.com]
 */

public class ConstraintViolationException extends DAOException
{

	private static final long	serialVersionUID				= 6704865474071426096L;

	public static int			UNKNOWN_CONSTRAINT_VIOLATION	= -10000;
	public static int			INTEGRITY_CONSTRAINT_VIOLATION	= 23000;
	public static int			UNIQUE_CONSTRAINT_VIOLATION		= 23505;
	public static int			REFERENTIAL_INTEGRITY_VIOLATION	= 23503;
	public static int			NOT_NULL_CONSTRAINT_VIOLATION	= 23502;
	// ADD MORE HERE

	private String				sqlState;

	private int					sqlErrorCode;

	public ConstraintViolationException( int errorCode, String message )
	{
		super( errorCode, message );
	}

	public ConstraintViolationException( int errorCode, String message, Throwable th )
	{
		super( errorCode, message, th );

		if( th instanceof SQLException )
		{
			SQLException sqlException = (SQLException) th;
			sqlState = sqlException.getSQLState();
			sqlErrorCode = sqlException.getErrorCode();
		}
	}

	public ConstraintViolationException( String message, Throwable casuse, SQLException sqlEx )
	{
		super( getErrorCodeBySQLState( sqlEx == null ? null : sqlEx.getSQLState() ), message, casuse );
		if( sqlEx != null )
		{
			sqlState = sqlEx.getSQLState();
			sqlErrorCode = sqlEx.getErrorCode();
		}
	}

	public String getSqlState()
	{
		return sqlState;
	}

	public int getSqlErrorCode()
	{
		return sqlErrorCode;
	}

	public boolean isUniqueConstraintViolation()
	{
		return getErrorCode() == UNIQUE_CONSTRAINT_VIOLATION;
	}

	public boolean isIntegrityConstraintViolation()
	{
		return getErrorCode() == INTEGRITY_CONSTRAINT_VIOLATION;
	}

	public boolean isReferencialIntegrityViolation()
	{
		return getErrorCode() == REFERENTIAL_INTEGRITY_VIOLATION;
	}

	public boolean isNotNullConstraintViolation()
	{
		return getErrorCode() == NOT_NULL_CONSTRAINT_VIOLATION;
	}

	public static int getErrorCodeBySQLState( String sqlState )
	{
		if( !StringUtils.isQualifiedString( sqlState ) )
		{
			return UNKNOWN_CONSTRAINT_VIOLATION;
		}
		else if( "23505".equals( sqlState ) )
		{
			return UNIQUE_CONSTRAINT_VIOLATION;
		}
		else if( "23000".equals( sqlState ) )
		{
			return INTEGRITY_CONSTRAINT_VIOLATION;
		}
		else if( "23502".equals( sqlState ) )
		{
			return NOT_NULL_CONSTRAINT_VIOLATION;
		}
		else if( "23503".equals( sqlState ) || "23520".equals( sqlState ) )
		{
			return REFERENTIAL_INTEGRITY_VIOLATION;
		}

		else
		{
			return UNKNOWN_CONSTRAINT_VIOLATION;
		}
	}

	public String getConstraintName()
	{
		if( isUniqueConstraintViolation() )
		{
			return "UNIQUE_CONSTRAINT_VIOLOATION";
		}
		else if( isIntegrityConstraintViolation() )
		{
			return "INTEGRITY_CONSTRAINT_VIOLOATION";
		}
		else if( isReferencialIntegrityViolation() )
		{
			return "REFERENCIAL_INTEGRITY_CONSTRAINT_VIOLOATION";
		}
		else if( isNotNullConstraintViolation() )
		{
			return "NOT_NULL_CONSTRAINT_VIOLOATION";
		}
		else
		{
			return "UNKNOWN_CONSTRAINT_VIOLOATION";
		}
	}

	@Override
	public String getMessage()
	{
		StringBuffer message = new StringBuffer( super.getMessage() );
		message.append( "--sqlState[" );
		message.append( sqlState );
		message.append( "] sqlErrorCode[" );
		message.append( sqlErrorCode );
		message.append( "] constraint-name[" );
		message.append( getConstraintName() );
		message.append( "]" );
		return message.toString();

	}

}
