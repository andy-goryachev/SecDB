// Copyright © 2020-2024 Andy Goryachev <andy@goryachev.com>
package goryachev.secdb.segmented.log;
import goryachev.common.util.Keep;


/**
 * Log Events are written to LogFile.
 */
@Keep
public enum LogEventCode
{
	CLOSED,
	HEAD,
//	OPENED, not needed
	STATE,
//	STORE,
//	STORE_OK,
//	STORE_DUP
	SWITCH_TO,
	SWITCHED_FROM,
	;

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
