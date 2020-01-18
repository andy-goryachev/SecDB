// Copyright © 2020 Andy Goryachev <andy@goryachev.com>
package goryachev.secdb.segmented.log;
import goryachev.common.util.SB;


/**
 * Log Events are written to LogFile.
 */
public abstract class LogEvent
{
	protected abstract void writeEvent(SB sb) throws Exception;
	
	//
	
	protected static final String SEP = "|";
	private final LogEventCode code;
	private final long timestamp;
	private final long sequence;
	
	
	public LogEvent(LogEventCode code)
	{
		this.code = code;
		this.timestamp = System.currentTimeMillis();
		this.sequence = LogFile.getSequence();
	}


	public void write(SB sb) throws Exception
	{
		sb.append(timestamp);
		sb.append(SEP);
		sb.append(sequence);
		sb.append(SEP);
		sb.append(code);
		sb.append(SEP);
		
		writeEvent(sb);
	}
}
