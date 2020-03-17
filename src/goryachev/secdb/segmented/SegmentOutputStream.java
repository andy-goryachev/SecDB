// Copyright © 2020 Andy Goryachev <andy@goryachev.com>
package goryachev.secdb.segmented;
import java.io.IOException;
import java.io.OutputStream;


/**
 * An OutputStream implementation that writes to SegmentFiles.
 */
public class SegmentOutputStream
	extends OutputStream
{
	private final SecStore store;
	private final long length;
	private final boolean isTree;
	private final byte[] key;
	private Ref ref;
	
	
	public SegmentOutputStream(SecStore store, long length, boolean isTree, byte[] key)
	{
		this.store = store;
		this.length = length;
		this.isTree = isTree;
		this.key = key;
	}
	
	
	public void write(int b) throws IOException
	{
		throw new Error("single byte write is not supported");
	}


	// TODO
	public void write(byte[] buf, int offset, int len) throws IOException
	{
		try
		{
			for(;;)
			{
				SegmentFile sf = store.segmentForLength(len, isTree);
				String name = sf.getName();
				long off = sf.getLength();
				
				if(ref == null)
				{
					ref = new Ref.SingleSegment(len, key, name, off);
				}
				else
				{
					ref = ref.addSegment(name, off);
				}
	
				int written = sf.write2(buf, offset, len, key);
				if(written < 0)
				{
					// TODO check if this is right
					return;
				}
				
				
				len -= written;
				if(len <= 0)
				{
					return;
				}
				off += written;
			}
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


	public void close() throws IOException
	{
		// TODO
	}


	public Ref getRef()
	{
		return ref;
	}
}
