// Copyright © 2020 Andy Goryachev <andy@goryachev.com>
package goryachev.secdb.segmented;
import goryachev.secdb.IStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Random;


/**
 * Large Pseudo Random Stream.
 */
public class LargePseudoRandomStream2
	implements IStream
{
	protected final Random random;
	protected final long length;
	
	
	public LargePseudoRandomStream2(int seed, long length)
	{
		this.random = new Random(0x55aa000000000000L ^ seed);
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
			private byte[] oneByte = new byte[1];
			
			
			public int read() throws IOException
			{
				if(pos < length)
				{
					random.nextBytes(oneByte);
					pos++;
					return oneByte[0] & 0xff;
				}
				return -1;
			}
			
			
			public int read(byte[] buf, int off, int len) throws IOException
			{
				boolean block = true;
				
				if(block)
				{
					long remain = length - pos;
					if(remain <= 0)
					{
						return -1;
					}
					
					int sz = (int)Math.min(len, remain);
					
					// neither Random nor Xoroshiro128Plus have nextBytes(byte[] buf, int off, int len) method
					byte[] b = new byte[sz];
					random.nextBytes(b);
					System.arraycopy(b, 0, buf, off, sz);
					pos += sz;
					return sz;
				}
				else
				{
					int c = read();
					if(c < 0)
					{
						return -1;
					}
					
					buf[off] = (byte)c;
					return 1;
				}
			}
		};
	}
}
