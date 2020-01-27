// Copyright © 2020 Andy Goryachev <andy@goryachev.com>
package goryachev.secdb.segmented;
import java.io.IOException;
import java.io.InputStream;


/**
 * InputStream reads data from the database SegmentFiles.
 */
public class SecStream
	extends InputStream
{
	protected final SecStore store;
	protected final Ref ref;
	private long position;
	private int segmentIndex;
	private long segmentOffset;
	// TODO for read(), use some kind of LocalBuffer class
//	private byte[] buf = new byte[4096];
//	private int available;
//	private int offset;
	
	
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
		long max;
	
		for(;;)
		{
			name = ref.getSegment(segmentIndex);
			off = ref.getOffset(segmentIndex) + segmentOffset;
		
			max = SegmentFile.SEGMENT_SIZE - off;
		
			if(max == 0)
			{
				// next segment
				segmentIndex++;
				if(segmentIndex >= ref.getSegmentCount())
				{
					return -1;
				}
				
				continue;
			}
			else
			{
				break;
			}
		}
		
		if(max < length)
		{
			length = (int)max;
		}
		
		SegmentFile sf = store.getSegmentFile(name);
		return sf.read(buffer, offset, length);
	}


	public void close() throws IOException
	{
			// TODO clear the key
	}
}
