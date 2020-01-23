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
	private final String[] data;
	private long timestamp;
	
	
	public LogEvent(LogEventCode code)
	{
		this(code, LogFile.timestamp(), null);
	}
	
	
	protected LogEvent(LogEventCode code, long t, String[] data)
	{
		this.code = code;
		this.timestamp = t;
		this.data = data;
	}
	
	
	public static LogEvent parse(String text) throws Exception
	{
		String[] ss = CKit.split(text, '|');
		if(ss.length >= 4)
		{
			long time = Parsers.parseLong(ss[0]);
			LogEventCode code = LogEventCode.parse(ss[1]);
			
//			switch(code)
//			{
//			case CLOSED:
//			case CREATED:
//			case HEAD:
//			case OPENED:
//			case STATE:
//			case STORE:
//			case STORE_DUP:
//			case STORE_OK:
//			}
			
			return new LogEvent(code, time, ss);
		}
		
		throw new Exception("failed to parse LogEvent: [" + text + "]");
	}
	
	
	public LogEventCode getCode()
	{
		return code;
	}
	
	
	public long getTimeStamp()
	{
		// TODO different between loaded and created
		if(timestamp == 0)
		{
			if(data != null)
			{
				timestamp = Long.parseLong(data[1]);
			}
		}
		return timestamp;
	}


	public void write(SB sb) throws Exception
	{
		sb.append(code);
		sb.append(SEP);
		sb.append(timestamp);
		sb.append(SEP);
		
		switch(code)
		{
		// TODO
		}
	}
}
