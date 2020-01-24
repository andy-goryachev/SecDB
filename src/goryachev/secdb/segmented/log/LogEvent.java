// Copyright © 2020 Andy Goryachev <andy@goryachev.com>
package goryachev.secdb.segmented.log;
import goryachev.common.util.CKit;
import goryachev.common.util.Parsers;
import goryachev.common.util.SB;


/**
 * Log Events are written to LogFile.
 */
public abstract class LogEvent
{
	public abstract void write(SB sb) throws Exception;
	
	//
	
	protected static final String SEP = "|";
	protected final LogEventCode code;
	protected final long timestamp;
	
	
	public LogEvent(LogEventCode code, long timestamp)
	{
		this.code = code;
		this.timestamp = timestamp;
	}
	
	
	public static LogEvent parse(String text) throws Exception
	{
		String[] ss = CKit.split(text, '|');
		if(ss.length >= 3)
		{
			LogEventCode code = LogEventCode.parse(ss[0]);
			long time = Parsers.parseLong(ss[1]);
			
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
			
			return new LogEvent.Read(code, time, ss);
		}
		
		throw new Exception("failed to parse LogEvent: [" + text + "]");
	}
	
	
	public LogEventCode getCode()
	{
		return code;
	}
	
	
	public long getTimeStamp()
	{
		return timestamp;
	}
	
	
	//
	
	
	public static class Read extends LogEvent
	{
		private final String[] data;

		
		public Read(LogEventCode code, long timestamp, String[] data)
		{
			super(code, timestamp);
			this.data = data;
		}
		
		
		public void write(SB sb) throws Exception
		{
			throw new Error();
		}
	}
	
	
	//
	
	
	public static class Save extends LogEvent
	{
		private final Object data;
		
		
		public Save(LogEventCode code, Object data)
		{
			super(code, LogFile.timestamp());
			this.data = data;
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
}
