// Copyright © 2020 Andy Goryachev <andy@goryachev.com>
package goryachev.secdb.util;
import goryachev.common.util.CKit;
import goryachev.secdb.IStream;
import java.io.File;
import java.io.InputStream;
import java.io.RandomAccessFile;


/**
 * Utilities.
 */
public class Utils
{
	public static boolean isEmptyDir(File dir)
	{
		if(dir.isDirectory())
		{
			File[] fs = dir.listFiles();
			if(fs == null)
			{
				return true;
			}
			return fs.length == 0;
		}
		return false;
	}
	
	
	/** copies input into the output stream.  returns the number of bytes copied.  supports cancellation */
	public static long copy(InputStream in, RandomAccessFile out, byte[] buf, long length) throws Exception
	{
		long count = 0;
		for(;;)
		{
			CKit.checkCancelled();
			
			int rd = in.read(buf);
			if(rd < 0)
			{
				return count;
			}
			else if(rd > 0)
			{
				out.write(buf, 0, rd);
				count += rd;
			}
		}
	}
	
	
	/** read data into a new byte array, as long as the object size is below the limit */
	public static byte[] readBytes(IStream in, int limit) throws Exception
	{
		long len = in.getLength();
		if(len > limit)
		{
			throw new Exception("object is too large: size=" + len + ", limit=" + limit);
		}
		
		byte[] b = new byte[(int)len];
		CKit.readFully(in.getStream(), b);
		return b;
	}
}
