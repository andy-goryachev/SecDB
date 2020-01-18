// Copyright © 2020 Andy Goryachev <andy@goryachev.com>
package goryachev.secdb.segmented.log;
import goryachev.common.io.DWriter;
import goryachev.common.util.CKit;
import goryachev.common.util.SB;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
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
	public static LogFile create(File dir) throws Exception
	{
		File f = File.createTempFile(NAME_PREFIX, "", dir); 
		FileOutputStream out = new FileOutputStream(f);
		return new LogFile(f, out);
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
}
