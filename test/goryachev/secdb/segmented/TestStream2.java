// Copyright © 2020 Andy Goryachev <andy@goryachev.com>
package goryachev.secdb.segmented;
import goryachev.common.util.Xoroshiro128Plus;
import goryachev.secdb.IStream;
import java.io.IOException;
import java.io.InputStream;


/**
 * IStream streams incrementing byte values.
 */
public class TestStream2
	implements IStream
{
	protected final int seed;
	protected final long length;
	
	
	public TestStream2(int seed, long length)
	{
		this.seed = seed;
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
					int rv = val();
					pos++;
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
				
				int ct = (int)Math.min(len, remain);
				for(int i=0; i<ct; i++)
				{
					buf[off + i] = (byte)val();
					pos++;
				}
				
				return ct;
			}
			
			
			protected int val()
			{
				switch((int)pos & 0x03)
				{
				case 0:
					return (seed >> 24) & 0xff;
				case 1:
					return (seed >> 16) & 0xff;
				case 2:
					return (seed >> 8) & 0xff;
				default:
					return seed & 0xff;
				}
			}
		};
	}
}
