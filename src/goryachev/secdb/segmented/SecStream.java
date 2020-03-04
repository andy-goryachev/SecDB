// Copyright © 2020 Andy Goryachev <andy@goryachev.com>
package goryachev.secdb.segmented;
import goryachev.common.log.Log;
import java.io.IOException;
import java.io.InputStream;


/**
 * InputStream reads data from the database SegmentFiles.
 */
public class SecStream
	extends InputStream
{
	protected static final Log log = Log.get("SecStream");
	protected final SecStore store;
	protected final Ref ref;
	private long position;
	private int segmentIndex;
	private int segmentOffset;
	
	
	public SecStream(SecStore store, Ref ref)
	{
		this.store = store;
		this.ref = ref;
	}
	

	public int read() throws IOException
	{
		if((ref.getLength() - position) > 0)
		{
			// TODO expensive, optimize to use a local buffer
			byte[] buf = new byte[1];
			for(;;)
			{
				int ct = read(buf, 0, 1);
				if(ct < 0)
				{
					throw new IOException("unexpected EOF: pos=" + position + " ref=" + ref);
				}
				else if(ct == 0)
				{
					continue;
				}
				
				position++;
				segmentOffset++;
				return buf[0] & 0xff;
			}
		}
		return -1;
	}


	public int read(byte[] buffer, int offset, int length) throws IOException
	{
		if((ref.getLength() - position) > 0)
		{
			try
			{
				int rv = readPrivate(buffer, offset, length);
				if(rv < 0)
				{
					throw new IOException("unexpected EOF: pos=" + position + " ref=" + ref);
				}
				else
				{
					position += rv;
					segmentOffset += rv;
				}
				return rv;
			}
			catch(IOException e)
			{
				throw e;
			}
			catch(Exception e)
			{
				throw new IOException(e);
			}
		}
		return -1;
	}
	
	
	protected int readPrivate(byte[] buffer, int offset, int length) throws Exception
	{
		String name;
		long off;
		long sz;
	
		for(;;)
		{
			name = ref.getSegment(segmentIndex);
			off = ref.getOffset(segmentIndex) + segmentOffset;
			sz = SegmentFile.SEGMENT_SIZE - off;
		
			if(sz == 0)
			{
				// next segment
				segmentIndex++;
				if(segmentIndex >= ref.getSegmentCount())
				{
					return -1;
				}

				segmentOffset = 0;
				continue;
			}
			else
			{
				break;
			}
		}
		
		if(sz < length)
		{
			length = (int)sz;
		}
		
		SegmentFile sf = store.getSegmentFile(name);
		int rv = sf.read(off, buffer, offset, length);

		log.trace("off={%08x} offset={%04x} len={%d} f={%s} -> {%d}", off, offset, length, sf.getName(), rv);
		
		return rv;
	}


	public void close() throws IOException
	{
		// TODO clear the key
	}
}
