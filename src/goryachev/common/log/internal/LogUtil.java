// Copyright © 2020 Andy Goryachev <andy@goryachev.com>
package goryachev.common.log.internal;
import goryachev.common.log.AbstractLogConfig;
import goryachev.common.log.AppenderBase;
import goryachev.common.log.Log;
import goryachev.common.log.LogLevel;
import goryachev.common.util.CKit;
import goryachev.common.util.CMap;
import goryachev.common.util.CSet;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;


/**
 * Log Utilities.
 */
public class LogUtil
{
	private static Gson gson;
	private static Pattern LEVELS = Pattern.compile("(OFF)|(FATAL)|(ERROR)|(WARN)|(INFO)|(DEBUG)|(TRACE)|(ALL)", Pattern.CASE_INSENSITIVE);
	
	
	public static LogConfig parseLogConfig(String spec) throws Exception
	{
		return gson().fromJson(spec, LogConfig.class);
	}
	
	
	private static Gson gson()
	{
		if(gson == null)
		{
			gson = new GsonBuilder().
				setLenient().
				setPrettyPrinting().
				create();
		}
		return gson;
	}


	public static AbstractLogConfig createDisabledLogConfig()
	{
		return new AbstractLogConfig()
		{
			public boolean isVerbose() { return true; }
			public LogLevel getLogLevel(String name) { return LogLevel.OFF; }
			public LogLevel getDefaultLogLevel() { return LogLevel.OFF; }
			public List<AppenderBase> getAppenders() { return null; }
		};
	}


	public static LogLevel parseLevel(String text)
	{
		if(CKit.isNotBlank(text))
		{
			Matcher m = LEVELS.matcher(text.trim());
			if(m.matches())
			{
				if(m.group(1) != null)
				{
					return LogLevel.OFF;
				}
				else if(m.group(2) != null)
				{
					return LogLevel.FATAL;
				}
				else if(m.group(3) != null)
				{
					return LogLevel.ERROR;
				}
				else if(m.group(4) != null)
				{
					return LogLevel.WARN;
				}
				else if(m.group(5) != null)
				{
					return LogLevel.INFO;
				}
				else if(m.group(6) != null)
				{
					return LogLevel.DEBUG;
				}
				else if(m.group(7) != null)
				{
					return LogLevel.TRACE;
				}
				else if(m.group(8) != null)
				{
					return LogLevel.ALL;
				}
			}
		}
		return LogLevel.OFF;
	}
	
	
	public static void process(CMap<String,LogLevel> m, String[] names, LogLevel lv)
	{
		if(names != null)
		{
			for(String name: names)
			{
				m.put(name, lv);
			}
		}
	}


	public static void process(CMap<String,LogLevel> m, CMap<String,String> channels)
	{
		if(channels != null)
		{
			for(String name: channels.keySet())
			{
				String v = channels.get(name);
				LogLevel lv = parseLevel(v);
				m.put(name, lv);
			}
		}
	}
	
	
	public static boolean checkNeedsCaller(Iterable<AppenderBase> appenders)
	{
		for(AppenderBase a: appenders)
		{
			if(a.needsCaller())
			{
				return true;
			}
		}
		return false;
	}


	public static boolean needsCaller(FormatField[] fields)
	{
		for(FormatField f: fields)
		{
			if(f.needsCaller())
			{
				return true;
			}
		}
		return false;
	}


	public static CSet<String> initIgnoreClassNames()
	{
		CSet<String> s = new CSet();
		s.add(Log.class.getName());
		return s;
	}
}
