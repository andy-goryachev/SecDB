// Copyright © 2020 Andy Goryachev <andy@goryachev.com>
package goryachev.secdb.segmented.log;
import goryachev.common.util.CKit;
import goryachev.common.util.Parsers;
import goryachev.common.util.SB;


/**
 * Log Events are written to LogFile.
 */
public final class LogEvent
{
	protected static final String SEP = "|";
	private final LogEventCode code;
	private final long timestamp;
	private final long sequence;
	
	
	public LogEvent(LogEventCode code)
	{
		this(code, System.currentTimeMillis(), LogFile.getSequence());
	}
	
	
	protected LogEvent(LogEventCode code, long time, long seq)
	{
		this.code = code;
		this.timestamp = time;
		this.sequence = seq;
	}
	
	
	public static LogEvent parse(String text) throws Exception
	{
		String[] ss = CKit.split(text, '|');
		if(ss.length >= 4)
		{
			long time = Parsers.parseLong(ss[0]);
			long seq = Parsers.parseLong(ss[1]);
			LogEventCode code = LogEventCode.parse(ss[2]);
			String data = ss[3];
			
			switch(code)
			{
			case CLOSED:
			case CREATED:
			case HEAD:
			case OPENED:
			case STATE:
			case STORE:
			case STORE_DUP:
			case STORE_OK:
			}
			
			return new LogEvent(code, time, seq);
		}
		
		throw new Exception("failed to parse LogEvent: [" + text + "]");
	}


	public void write(SB sb) throws Exception
	{
		sb.append(timestamp);
		sb.append(SEP);
		sb.append(sequence);
		sb.append(SEP);
		sb.append(code);
		sb.append(SEP);
		
		switch(code)
		{
		// TODO
		}
	}
}
