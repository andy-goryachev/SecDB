// Copyright © 2020 Andy Goryachev <andy@goryachev.com>
package goryachev.secdb.segmented.log;
import goryachev.common.util.SB;


/**
 * DB Opened Event.
 */
public class OpenEvent
	extends LogEvent
{
	public OpenEvent()
	{
		super(LogEventCode.OPENED);
	}

	
	protected void writeEvent(SB sb) throws Exception
	{
	}
}
