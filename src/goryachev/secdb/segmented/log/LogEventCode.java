// Copyright © 2020 Andy Goryachev <andy@goryachev.com>
package goryachev.secdb.segmented.log;
import goryachev.common.util.Keep;


/**
 * Log Events are written to LogFile.
 */
@Keep
public enum LogEventCode
{
	CREATED,
	CLOSED,
	HEAD,
	OPENED,
	STATE,
	STORE,
	STORE_OK,
	STORE_DUP;

	//
	
	public static LogEventCode parse(String text) throws Exception
	{
		for(LogEventCode c: values())
		{
			if(c.toString().equals(text))
			{
				return c;
			}
		}
		throw new Exception("?" + text);
	}
}
