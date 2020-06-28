// Copyright © 2020 Andy Goryachev <andy@goryachev.com>
package goryachev.secdb.segmented;
import goryachev.secdb.IStream;
import java.io.IOException;
import java.io.InputStream;


/**
 * IStream streams integer values corresponding to the current data offset.
 */
public class TOffsetIStream
	implements IStream
{
	protected final long length;
	
	
	public TOffsetIStream(long length)
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
			private int index;
			private String currentAddress;
			
			
			protected byte val()
			{
				if((index == 0) || (index >= currentAddress.length()))
				{
					currentAddress = pos + " ";
					index = 0;					
				}
				
				byte v = (byte)currentAddress.charAt(index);
				index++;
				pos++;
				
				return v;
			}
		
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
		};
	}
}
