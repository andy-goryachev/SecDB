// Copyright © 2019-2020 Andy Goryachev <andy@goryachev.com>
package goryachev.secdb.segmented;
import goryachev.common.util.CKit;
import goryachev.secdb.IStream;
import goryachev.secdb.util.Utils;
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
	public static final long SEGMENT_SIZE = CKit.mebi(512);
	protected static final int BUF_SIZE = 4096;
	protected final File file;
	protected final String name;
	private RandomAccessFile reader;
	private RandomAccessFile writer;
	
	
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


	/** 
	 * writes as much data as possible until segment is full.  
	 * returns the number of bytes written.
	 */ 
	public long write(IStream in, byte[] key) throws Exception
	{
		long size = in.getLength();
		long len = getLength();
		long max = SEGMENT_SIZE - len;
		
		byte[] buf = new byte[BUF_SIZE];
		
		if(writer == null)
		{
			file.getParentFile().mkdirs();
			writer = new RandomAccessFile(file, "rw");
		}
		writer.seek(len);
		
		long toWrite = Math.min(max, size);
		return Utils.copy(in.getStream(), writer, buf, toWrite);
		
		// TODO don't forget to close the writer
	}


	public int read(long position, byte[] buffer, int offset, int length) throws Exception
	{
		if(reader == null)
		{
			reader = new RandomAccessFile(file, "r");
		}
		
		synchronized(reader)
		{
			reader.seek(position);
			return reader.read(buffer, offset, length);
		}
	}
}
