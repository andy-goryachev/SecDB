// Copyright © 2020 Andy Goryachev <andy@goryachev.com>
package goryachev.secdb.segmented.log;
import goryachev.common.io.CReader;
import goryachev.common.util.CComparator;
import goryachev.common.util.CKit;
import goryachev.common.util.CList;
import goryachev.common.util.SB;
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
	protected final byte[] key;
	protected final EnumMap<LogEventCode,LogEvent> events = new EnumMap<>(LogEventCode.class);
	private FileOutputStream out;
	private boolean error;
	private long lastTime;
	
	
	public LogFile(File f, byte[] key, FileOutputStream out)
	{
		this.file = f;
		this.key = key;
		this.out = out;
	}
	
	
	/** creates an empty log file.  assumes the directory exists */
	// TODO log key
	public static LogFile create(File dir, byte[] key) throws Exception
	{
		File f = File.createTempFile(NAME_PREFIX, "", dir); 
		FileOutputStream out = new FileOutputStream(f);
		return new LogFile(f, key, out);
	}
	
	
	/** 
	 * returns a list of all log files found in the base directory,
	 * sorted most recent first.
	 */
	// TODO log key
	public static List<LogFile> open(File dir, byte[] key) throws Exception
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
		
		Collections.sort(lfs, new CComparator<LogFile>()
		{
			public int compare(LogFile a, LogFile b)
			{
				return compareLong(a.getLastTime(), b.getLastTime());
			}
		});
		
		return lfs;
	}
	
	
	protected long getLastTime()
	{
		return lastTime;
	}


	protected static LogFile load(File f, byte[] key) throws Exception
	{
		FileOutputStream out = new FileOutputStream(f, true);
		LogFile lf = new LogFile(f, key, out);
		lf.load();
		return lf;
	}
	
	
	protected void load() throws Exception
	{
		CReader rd = new CReader(file);
		try
		{
			String line;
			while((line = rd.readLine()) != null)
			{
				LogEvent ev = LogEvent.parse(line);
				events.put(ev.getCode(), ev);
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
	
	
	public void appendEvent(LogEvent ev) throws Exception
	{
		SB sb = new SB(256);
		ev.write(sb);
		byte[] b = sb.toString().getBytes(CKit.CHARSET_UTF8);

		out.write(b);
	}


	public void close() throws IOException
	{
		CKit.close(out);
	}


	public static long timestamp()
	{
		return tstamp.nextTimeStamp();
	}
	
	
	public boolean isRecoveryNeeded()
	{
		// TODO
		return false;
	}


	public Ref getRootRef()
	{
		// TODO
		return null;
	}
}
