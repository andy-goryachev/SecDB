// Copyright © 2020 Andy Goryachev <andy@goryachev.com>
package goryachev.secdb.segmented;
import java.io.IOException;
import java.io.InputStream;


/**
 * InputStream with Length.
 */
public class InputStreamWithLength
	extends InputStream
{
	private final InputStream in;
	private final long length;
	private int count = SegmentStream.HEADER_SIZE;
	
	
	public InputStreamWithLength(InputStream in, long len)
	{
		this.in = in;
		this.length = len;
	}
	
	
	protected int next()
	{
		--count;
		return ((int)(length >> (count * 8))) & 0xff; 
	}
	

	public int read() throws IOException
	{
		if(count > 0)
		{
			return next();
		}
		else
		{
			return in.read();
		}
	}
	
	
	public int read(byte[] buf, int off, int len) throws IOException
	{
		if(count > 0)
		{
			int i = 0;
			while(count > 0)
			{
				buf[off + i] = (byte)next();
				i++;
				if((count == 0) || (i >= len))
				{
					break;
				}
			}
			return i;
		}
		else
		{
			return in.read(buf, off, len);
		}
	}
}
