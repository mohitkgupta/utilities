package org.vedantatree.utils.types;

public class MethodKey
{

	public MethodKey( String methodName, Type parameterTypes[] )
	{
		this.methodName = methodName;
		this.parameterTypes = parameterTypes;
		key = generateKey( methodName, parameterTypes );
	}

	public static String generateKey( String methodName, Type parameterTypes[] )
	{
		if( methodName == null )
			throw new IllegalArgumentException( "Passed methodName can't be null." );
		StringBuffer buffer = new StringBuffer( methodName );
		buffer.append( '(' );
		int length = parameterTypes != null ? parameterTypes.length : 0;
		for( int i = 0; i < length; i++ )
		{
			Type type = parameterTypes[i];
			buffer.append( type.getTypeName() );
			if( i < length - 1 )
				buffer.append( ',' );
		}

		buffer.append( ')' );
		return buffer.toString();
	}

	public boolean isAssignaleFrom( MethodKey key )
	{
		boolean result = false;
		if( key != null && key.methodName.equals( methodName ) )
		{
			Type thatParameterTypes[] = key.parameterTypes;
			int thatLength = thatParameterTypes != null ? thatParameterTypes.length : 0;
			int length = parameterTypes != null ? parameterTypes.length : 0;
			if( thatLength == length )
			{
				boolean assignableFrom = true;
				for( int i = 0; i < length; i++ )
				{
					if( parameterTypes[i].isAssignableFrom( thatParameterTypes[i] ) )
						continue;
					assignableFrom = false;
					break;
				}

				result = assignableFrom;
			}
		}
		return result;
	}

	@Override
	public boolean equals( Object obj )
	{
		boolean result = false;
		if( obj instanceof MethodKey )
		{
			MethodKey thatKey = (MethodKey) obj;
			result = thatKey.key.equals( key );
		}
		return result;
	}

	@Override
	public int hashCode()
	{
		return key.hashCode();
	}

	@Override
	public String toString()
	{
		return key.toString();
	}

	private String	methodName;
	private Type	parameterTypes[];
	private String	key;
}
