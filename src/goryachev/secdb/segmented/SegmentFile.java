// Copyright © 2019-2020 Andy Goryachev <andy@goryachev.com>
package goryachev.secdb.segmented;
import java.io.File;
import java.io.InputStream;
import java.io.RandomAccessFile;


/**
 * SecStore Segment File.
 */
public class SegmentFile
{
	protected final File file;
	protected final String name;
	private long length;
	// TODO or use MappedByteBuffer?
	private RandomAccessFile raf;
	
	
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
		return length;
	}


	/** writes as much data as possible until segment is full.  returns the amount of remaining data to store */ 
	public long write(InputStream in, byte[] key) throws Exception
	{
		// TODO
		throw new Error();
	}
}
