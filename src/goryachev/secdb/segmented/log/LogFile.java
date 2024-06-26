// Copyright © 2020-2024 Andy Goryachev <andy@goryachev.com>
package goryachev.secdb.segmented.log;
import goryachev.common.io.CReader;
import goryachev.common.util.CComparator;
import goryachev.common.util.CKit;
import goryachev.common.util.CList;
import goryachev.common.util.SB;
import goryachev.memsafecrypto.OpaqueBytes;
import goryachev.secdb.segmented.Ref;
import java.io.Closeable;
import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;


/**
 * The purpose of Log File is to
 * 
 *   1. retain state between shutdowns
 *   2. assist in recovery
 */
public class LogFile
	implements Closeable
{
	protected static final String NAME_PREFIX = "log.";
	protected static final MonotonicUniqueTimeStamp tstamp = new MonotonicUniqueTimeStamp();
	protected final File file;
	protected final OpaqueBytes key;
	protected final EnumMap<LogEventCode,LogEvent> events = new EnumMap<>(LogEventCode.class);
	private LogEvent lastEvent;
	private FileOutputStream out;
	private boolean error;
	private long lastTime;
	
	
	public LogFile(File f, OpaqueBytes key)
	{
		this.file = f;
		this.key = key;
	}
	
	
	/** creates an empty log file.  assumes the directory exists */
	public static LogFile create(File dir, OpaqueBytes key) throws Exception
	{
		File f = File.createTempFile(NAME_PREFIX, "", dir); 
		return new LogFile(f, key);
	}
	
	
	/** 
	 * returns a list of all log files found in the base directory,
	 * sorted most recent first.
	 */
	// TODO log key
	public static List<LogFile> open(File dir, OpaqueBytes key) throws Exception
	{
		// pick the oldest log file
		
		File[] fs = dir.listFiles(new FileFilter()
		{
			public boolean accept(File f)
			{
				return f.isFile() && f.getName().startsWith(NAME_PREFIX);
			}
		});
		
		CList<LogFile> lfs = new CList();
		if(fs != null)
		{
			for(File f: fs)
			{
				LogFile lf = LogFile.load(f, key);
				lfs.add(lf);
			}
		}
		
		// recent first
		Collections.sort(lfs, new CComparator<LogFile>()
		{
			public int compare(LogFile a, LogFile b)
			{
				return compareLong(b.getLastTime(), a.getLastTime());
			}
		});
		
		return lfs;
	}
	
	
	protected long getLastTime()
	{
		return lastTime;
	}
	
	
	public String getName()
	{
		return file.getAbsolutePath();
	}


	protected static LogFile load(File f, OpaqueBytes key) throws Exception
	{
		LogFile lf = new LogFile(f, key);
		lf.load();
		
		if(lf.lastEvent != null)
		{
			lf.lastTime = lf.lastEvent.getTimeStamp();
		}
		
		return lf;
	}
	
	
	@SuppressWarnings("resource") // compiler is wrong
	protected void load() throws Exception
	{
		CReader rd = new CReader(file);
		long time = 0;
		
		try
		{
			String line;
			while((line = rd.readLine()) != null)
			{
				LogEvent ev = LogEvent.parse(line);
				if(ev.getTimeStamp() < time)
				{
					throw new Exception("timestamp jump: last=" + time + " loading=" + ev);
				}
				time = ev.getTimeStamp();

				events.put(ev.getCode(), ev);
				lastEvent = ev;
			}
		}
		catch(Exception e)
		{
			error = true;
		}
		finally
		{
			CKit.close(rd);
		}
	}
	
	
	public void appendEvent(LogEventCode cd) throws Exception
	{
		appendEvent(new LogEvent.Save(cd, null));
	}
	
	
	public void appendEvent(LogEventCode cd, Object data) throws Exception
	{
		appendEvent(new LogEvent.Save(cd, data));
	}
	
	
	protected void appendEvent(LogEvent ev) throws Exception
	{
		SB sb = new SB(256);
		ev.write(sb);
		byte[] b = sb.toString().getBytes(CKit.CHARSET_UTF8);

		if(out == null)
		{
			out = new FileOutputStream(file, true);
		}
		out.write(b);
	}


	public void close() throws IOException
	{
		CKit.close(out);
		out = null;
	}


	public static long timestamp()
	{
		return tstamp.nextTimeStamp();
	}
	
	
	public boolean isRecoveryNeeded()
	{
		if(lastEvent == null)
		{
			return true;
		}
		
		// TODO instead, we should probably determine if the last write has been successful or not.
		// perhaps write to a file and delete it?  or create a marker file?
		// or perhaps write in the log: WRITING... HEAD
//		switch(lastEvent.getCode())
//		{
//		case CLOSED:
//			return false;
//		}
		
		return false;
	}


	public Ref getRootRef()
	{
		LogEvent ev = events.get(LogEventCode.HEAD);
		if(ev != null)
		{
			return ev.getRefData();
		}
		return null;
	}
}
