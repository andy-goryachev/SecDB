// Copyright © 2020-2024 Andy Goryachev <andy@goryachev.com>
package goryachev.secdb.segmented;
import goryachev.secdb.IStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Random;


/**
 * Large Pseudo Random Stream.
 * 
 * FIX this implementation is incorrect: I wanted a repeatable stream (for a given seed),
 * yet it does not produce the same byte stream between bulk read() and reading individual bytes.
 */
public class LargePseudoRandomStream
	implements IStream
{
	protected final Random random;
	protected final long length;
	
	
	public LargePseudoRandomStream(int seed, long length)
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
			private final byte[] pool = new byte[8];
			private int pos = pool.length;
			private long current;
			

			public int read() throws IOException
			{
				if(current < length)
				{
					return next();
				}
				return -1;
			}
			
			
			public int read(byte[] buf, int off, int len) throws IOException
			{
				long remain = length - current;
				if(remain <= 0)
				{
					return -1;
				}
				
				int sz = (int)Math.min(remain, len);
				for(int i=0; i<sz; i++)
				{
					buf[off + i] = next();
				}
				return len;
			}
			
			
			protected byte next()
			{
				if(pos >= pool.length)
				{
					random.nextBytes(pool);
					pos = 0;
				}
				
				current++;
				return pool[pos++];
			}
		};
	}
}
