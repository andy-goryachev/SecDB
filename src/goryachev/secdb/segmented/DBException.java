// Copyright © 2020 Andy Goryachev <andy@goryachev.com>
package goryachev.secdb.segmented;


/**
 * SecDB Exception.
 */
public class DBException
	extends Exception
{
	private final DBErrorCode code;
	private final Object value;
	
	
	public DBException(DBErrorCode code, Object value)
	{
		super(code + " " + value);
		this.code = code;
		this.value = value;
	}
	
	
	public DBErrorCode getErrorCode()
	{
		return code;
	}
	
	
	public Object getErrorValue()
	{
		return value;
	}
}
