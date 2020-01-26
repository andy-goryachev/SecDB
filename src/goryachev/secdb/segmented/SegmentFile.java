// Copyright © 2019-2020 Andy Goryachev <andy@goryachev.com>
package goryachev.secdb.segmented;
import java.io.InputStream;


/**
 * SecStore Segment File.
 */
public class SegmentFile
{
	protected final String name;
	private long length;
	
	
	public SegmentFile(String name)
	{
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
