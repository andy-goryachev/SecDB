// Copyright © 2020 Andy Goryachev <andy@goryachev.com>
package goryachev.secdb.segmented.log;
import goryachev.common.util.SB;


/**
 * DB Created Event.
 */
public class CreatedEvent
	extends LogEvent
{
	public CreatedEvent()
	{
		super(LogEventCode.CREATED);
	}

	
	protected void writeEvent(SB sb) throws Exception
	{
	}
}
