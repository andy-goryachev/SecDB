// Copyright © 2020 Andy Goryachev <andy@goryachev.com>
package goryachev.secdb.segmented;
import goryachev.common.util.CKit;
import goryachev.common.util.Xoroshiro128Plus;
import goryachev.secdb.IStream;
import java.io.IOException;
import java.io.InputStream;


/**
 * IStream streams incrementing byte values.
 */
public class TestStream3
	implements IStream
{
	protected final byte[] bytes;
	protected final long length;
	private int phase;
	
	
	public TestStream3(int seed, long length)
	{
		this.bytes = toBytes(seed).getBytes(CKit.CHARSET_ASCII);
		this.length = length;
	}
	
	
	private static String toBytes(int seed)
	{
		switch(seed)
		{
		case 0:
			return "zero-0 ";
		case 1:
			return "one-111 ";
		case 2:
			return "two-2222 ";
		case 3:
			return "three-333 ";
		default:
			return "abcdefg";
		}
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
				int rv = bytes[phase];
				phase++;
				if(phase >= bytes.length)
				{
					phase = 0;
				}
				return rv;
			}
		};
	}
}
