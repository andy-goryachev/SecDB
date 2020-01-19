// Copyright © 2020 Andy Goryachev <andy@goryachev.com>
package goryachev.secdb.segmented.log;
import goryachev.common.util.CKit;
import goryachev.common.util.CList;
import goryachev.common.util.SB;
import goryachev.secdb.segmented.DBErrorCode;
import goryachev.secdb.segmented.DBException;
import goryachev.secdb.segmented.Ref;
import java.io.Closeable;
import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.IOException;


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
	protected final File file;
	private FileOutputStream out;
	private static long sequence;
	
	
	public LogFile(File f, FileOutputStream out)
	{
		this.file = f;
		this.out = out;
	}
	
	
	/** creates an empty log file.  assumes the directory exists */
	// TODO log key
	public static LogFile create(File dir, byte[] key) throws Exception
	{
		File f = File.createTempFile(NAME_PREFIX, "", dir); 
		FileOutputStream out = new FileOutputStream(f);
		return new LogFile(f, out);
	}
	
	
	/** creates an empty log file.  assumes the directory exists */
	// TODO log key
	public static LogFile open(File dir, byte[] key) throws Exception
	{
		// pick the oldest log file
		
		File[] fs = dir.listFiles(new FileFilter()
		{
			public boolean accept(File f)
			{
				return f.isFile() && f.getName().startsWith(NAME_PREFIX);
			}
		});
		
		if((fs == null) || (fs.length == 0))
		{
			throw new DBException(DBErrorCode.MISSING_LOG_FILE, dir);
		}
		
		CList<LogFile> lfs = new CList();
		for(File f: fs)
		{
			LogFile lf = LogFile.openPrivate(f); // or load
			lfs.add(lf);
		}
		
		// TODO sort latest first
		
		// TODO
		// check for unexpected shutdown
		throw new Error("todo");
	}
	
	
	protected static LogFile openPrivate(File f) throws Exception
	{
		// TODO
		return null;
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


	public static long getSequence()
	{
		return sequence;
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
