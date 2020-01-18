// Copyright © 2019 Andy Goryachev <andy@goryachev.com>
package goryachev.secdb.segmented;


/**
 * Log Events are written to LogFile.
 */
public class LogEvent
{
	private final LogEventCode code;
	private final long timestamp;
	private final long sequence;
	
	
	public LogEvent(LogEventCode code, long timestamp, long sequence)
	{
		this.code = code;
		this.timestamp = timestamp;
		this.sequence = sequence;
	}
}
