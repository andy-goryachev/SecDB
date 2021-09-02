// Copyright © 2019-2021 Andy Goryachev <andy@goryachev.com>
package goryachev.secdb.segmented;
import goryachev.common.util.CKit;
import java.io.File;
import java.io.RandomAccessFile;


/**
 * SecStore Segment File.
 * 
 * Uses RandomAccessFiles (one for writing, the other for reading).
 * A decision was made not to use MappedByteBuffer because of their sub-optimal 
 * implementation on Windows (i.e. cannot delete a mapped file).
 * 
 * or use read-only mmap, write with FileChannel
 * http://www.mapdb.org/blog/mmap_files_alloc_and_jvm_crash/
 */
public class SegmentFile
{
	// FIX make final after debugging
	public static /*final*/ long SEGMENT_SIZE = CKit.mebi(512);
	protected static final int BUF_SIZE = 4096;
	protected final File file;
	protected final String name;
	private RandomAccessFile reader;
	private RandomAccessFile writer;
	/** only one writer is allowed */
	private final byte[] writeBuffer = new byte[BUF_SIZE];
	
	
	public SegmentFile(File file, String name)
	{
		this.file = file;
		this.name = name;
	}
	
	
	public String getName()
	{
		return name;
	}
	
	
	public long getLength()
	{
		// this might query the file system each time
		return file.length();
	}

	
	/** writes as much as possible to the segment file.  returns the number of bytes written, or -1 if the segment is full */
	public int write(byte[] buf, int off, int len) throws Exception
	{
		long seglen = getLength();
		long available = SEGMENT_SIZE - seglen;
		if(available <= 0)
		{
			return -1;
		}
		
		if(writer == null)
		{
			File pf = file.getParentFile();
			if(pf != null)
			{
				pf.mkdirs();
			}
			writer = new RandomAccessFile(file, "rw");
		}
		
		int sz = (int)Math.min(available, len);
		
		writer.seek(seglen);
		writer.write(buf, off, sz);
		
		return sz;
	}
	
	
	public void closeWriter() throws Exception
	{
		if(writer != null)
		{
			writer.close();
			writer = null;
		}
	}
	
	
	public void closeReader() throws Exception
	{
		RandomAccessFile rd;
		synchronized(this)
		{
			rd = reader;
			reader = null;
		}
		
		if(rd != null)
		{
			rd.close();
		}
	}


	public int read(long position, byte[] buf, int off, int len) throws Exception
	{
		// TODO we need to keep track of the total number of open readers 
		// to avoid having too many open files
		if(reader == null)
		{
			synchronized(this)
			{
				if(reader == null)
				{
					reader = new RandomAccessFile(file, "r");
				}
			}
		}
		
		synchronized(reader)
		{
			reader.seek(position);
			return reader.read(buf, off, len);
		}
	}
}
