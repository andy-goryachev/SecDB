// Copyright © 2020-2024 Andy Goryachev <andy@goryachev.com>
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
	private SegmentFile sf;
	private long position;
	private Ref ref;
	private Ref finalRef;
	
	
	public SegmentOutputStream(SecStore store, long length, boolean isTree)
	{
		this.store = store;
		this.length = length;
		this.isTree = isTree;
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
			position = sf.getLength();
			
			if(ref == null)
			{
				ref = new Ref.SingleSegment(length, name, position);
			}
			else
			{
				ref = ref.addSegment(name, position);
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
				int written = sf().write(buf, off, len);
				if(written < 0)
				{
					sf.closeWriter();
					sf = null;
					continue;
				}
				
				len -= written;
				position += written;

				if(len == 0)
				{
					return;
				}
				else if(len < 0)
				{
					throw new Error("len=" + len);
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
		finalRef = ref;
	}


	/** available only after close */
	public Ref getRef()
	{
		if(finalRef == null)
		{
			throw new Error("null final ref");
		}
		return finalRef;
	}
}
