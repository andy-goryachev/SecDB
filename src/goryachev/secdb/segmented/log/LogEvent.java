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
	private final TStamp timestamp;
	private final String[] data;
	
	
	public LogEvent(LogEventCode code)
	{
		this(code, new TStamp(LogFile.getSequence(), System.currentTimeMillis()), null);
	}
	
	
	protected LogEvent(LogEventCode code, TStamp t, String[] data)
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
			long seq = Parsers.parseLong(ss[1]);
			LogEventCode code = LogEventCode.parse(ss[2]);
			
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
			
			return new LogEvent(code, new TStamp(seq, time), ss);
		}
		
		throw new Exception("failed to parse LogEvent: [" + text + "]");
	}
	
	
	public LogEventCode getCode()
	{
		return code;
	}


	public void write(SB sb) throws Exception
	{
		sb.append(timestamp.sequence);
		sb.append(SEP);
		sb.append(timestamp.time);
		sb.append(SEP);
		sb.append(code);
		sb.append(SEP);
		
		switch(code)
		{
		// TODO
		}
	}
}
