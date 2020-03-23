// Copyright © 2020 Andy Goryachev <andy@goryachev.com>
package goryachev.secdb.segmented;
import goryachev.common.log.Log;
import java.io.IOException;
import java.io.InputStream;


/**
 * This InputStream reads data from SegmentFile(s).
 */
public class SegmentInputStream
	extends InputStream
{
	protected static final Log log = Log.get("SegmentInputStream");
	protected final SecStore store;
	protected final Ref ref;
	protected final long length;
	private long position;
	private int segmentIndex;
	private int segmentOffset;
	
	
	public SegmentInputStream(SecStore store, Ref ref)
	{
		this.store = store;
		this.ref = ref;
		this.length = ref.getLength();
	}
	

	public int read() throws IOException
	{
		if((length - position) > 0)
		{
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
				
				return buf[0] & 0xff;
			}
		}
		return -1;
	}


	public int read(byte[] buf, int off, int len) throws IOException
	{
		long remain = length - position;
		if(remain <= 0)
		{
			return -1;
		}
		
		try
		{
			int sz = (int)Math.min(remain, len);
			int rv = readPrivate(buf, off, sz);
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
	
	
	protected int readPrivate(byte[] buf, int offset, int len) throws Exception
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
		
		if(sz < len)
		{
			len = (int)sz;
		}
		
		SegmentFile sf = store.getSegmentFile(name);
		int rv = sf.read(off, buf, offset, len);

		log.trace("off={%08x} offset={%04x} len={%d} f={%s} -> {%d}", off, offset, len, sf.getName(), rv);
		
		return rv;
	}


	public void close() throws IOException
	{
		// TODO clear the key
	}
}
