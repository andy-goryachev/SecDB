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
	STORE_DUP,
}
