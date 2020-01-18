// Copyright © 2019 Andy Goryachev <andy@goryachev.com>
package goryachev.secdb.segmented;


/**
 * Log Events are written to LogFile.
 */
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
