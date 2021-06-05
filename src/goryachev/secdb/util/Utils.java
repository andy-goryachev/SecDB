// Copyright © 2020-2021 Andy Goryachev <andy@goryachev.com>
package goryachev.secdb.util;
import goryachev.common.util.CKit;
import goryachev.secdb.IStream;
import java.io.DataOutput;
import java.io.File;
import java.io.InputStream;


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
	public static long copy(InputStream in, DataOutput out, byte[] buf, long length) throws Exception
	{
		long count = 0;
		for(;;)
		{
			CKit.checkCancelled();
			
			int sz = (int)Math.min(buf.length, length);
			if(sz == 0)
			{
				break;
			}
			
			int rd = in.read(buf, 0, sz);
			if(rd < 0)
			{
				break;
			}
			else if(rd > 0)
			{
				out.write(buf, 0, rd);
				count += rd;
				length -= rd;
			}
		}
		
		return count;
	}
	
	
	/** read data into a new byte array, as long as the object size is below the limit */
	public static byte[] readBytes(IStream inp, int limit) throws Exception
	{
		long len = inp.getLength();
		if(len > limit)
		{
			throw new Exception("object is too large: size=" + len + ", limit=" + limit);
		}
		
		byte[] b = new byte[(int)len];
		InputStream is = inp.getStream();
		try
		{
			CKit.readFully(is, b);
			return b;
		}
		finally
		{
			CKit.close(is);
		}
	}
}
