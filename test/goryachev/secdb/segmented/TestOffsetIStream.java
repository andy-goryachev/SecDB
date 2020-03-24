// Copyright © 2020 Andy Goryachev <andy@goryachev.com>
package goryachev.secdb.segmented;
import goryachev.secdb.IStream;
import java.io.IOException;
import java.io.InputStream;


/**
 * IStream streams integer values corresponding to the current data offset.
 */
public class TestOffsetIStream
	implements IStream
{
	protected final long length;
	
	
	public TestOffsetIStream(long length)
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
			private int phase;
			private int val;
			
			
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
				byte v;
				switch(phase)
				{
				case 0:
					val = (int)pos;
					v = (byte)(val >> 24);
					break;
				case 1:
					v = (byte)(val >> 16);
					break;
				case 2:
					v = (byte)(val >> 8);
					break;
				default:
					v = (byte)(val);
					break;
				}
				
				phase = ((phase + 1) % 4);
				pos++;
				
				return v;
			}
		};
	}
}
