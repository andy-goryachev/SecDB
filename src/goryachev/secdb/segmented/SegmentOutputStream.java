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
	private SegmentFile sf;
	private long off2;
	private Ref ref;
	private Ref finalRef;
	
	
	public SegmentOutputStream(SecStore store, long length, boolean isTree, byte[] key)
	{
		this.store = store;
		this.length = length;
		this.isTree = isTree;
		this.key = key;
	}
	
	
	public Ref getInitialRef() throws Exception
	{
		sf();
		return ref;
	}
	
	
	protected SegmentFile sf() throws Exception
	{
		if(sf == null)
		{
			sf = store.segmentForLength(length, isTree);
			String name = sf.getName();
			off2 = sf.getLength();
			
			if(ref == null)
			{
				ref = new Ref.SingleSegment(length, key, name, off2);
			}
			else
			{
				ref = ref.addSegment(name, off2);
			}
		}
		return sf;
	}
	
	
	public void write(int b) throws IOException
	{
		throw new Error("single byte write is not supported");
	}


	public void write(byte[] buf, int off, int len) throws IOException
	{
		try
		{
			for(;;)
			{
				int written = sf().write2(buf, off, len, key);
				if(written < 0)
				{
					sf = null;
					continue;
				}
				
				len -= written;
				if(len == 0)
				{
					return;
				}
				else if(len < 0)
				{
					throw new Error("len=" + len);
				}
				
				off2 += written;
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
		finalRef = ref;
	}


	/** TODO available only after close */
	public Ref getRef()
	{
		if(finalRef == null)
		{
			throw new Error("null final ref");
		}
		return finalRef;
	}
}
