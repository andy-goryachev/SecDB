// Copyright © 2020-2024 Andy Goryachev <andy@goryachev.com>
package goryachev.secdb.util;
import goryachev.common.util.CKit;
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
}
