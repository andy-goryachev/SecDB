// Copyright © 2020 Andy Goryachev <andy@goryachev.com>
package goryachev.secdb.segmented;
import goryachev.secdb.IStream;
import java.io.IOException;
import java.io.InputStream;


/**
 * IStream streams incrementing byte values.
 */
public class IncrementingByteStream
	implements IStream
{
	protected final long length;
	
	
	public IncrementingByteStream(long length)
	{
		this.length = length;
	}
	
	
	public long getLength()
	{
		return length;
	}


	public InputStream getStream()
	{
		return new InputStream()
		{
			private long pos;
			
			
			public int read() throws IOException
			{
				if(pos < length)
				{
					int rv = val() & 0xff;
					return rv;
				}
				return -1;
			}
			
			
			public int read(byte[] buf, int off, int len) throws IOException
			{
				long remain = length - pos;
				if(remain <= 0)
				{
					return -1;
				}
				
				int sz = (int)Math.min(len, remain);
				for(int i=0; i<sz; i++)
				{
					buf[off + i] = val();
				}
				
				return sz;
			}
			
			
			protected byte val()
			{
				return (byte)pos++;
			}
		};
	}
}
