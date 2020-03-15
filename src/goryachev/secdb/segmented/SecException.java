// Copyright © 2020 Andy Goryachev <andy@goryachev.com>
package goryachev.secdb.segmented;


/**
 * SecDB Exception.
 */
public class SecException
	extends Exception
{
	private final SecErrorCode code;
	private final Object value;
	
	
	public SecException(SecErrorCode code, Object value)
	{
		super(code + " " + value);
		this.code = code;
		this.value = value;
	}
	
	
	public SecException(SecErrorCode code, Throwable cause)
	{
		super(code.toString(), cause);
		this.code = code;
		this.value = null;
	}
	
	
	public SecErrorCode getErrorCode()
	{
		return code;
	}
	
	
	public Object getErrorValue()
	{
		return value;
	}
}
