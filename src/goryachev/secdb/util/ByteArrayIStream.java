// Copyright © 2019-2020 Andy Goryachev <andy@goryachev.com>
package goryachev.secdb.util;
import goryachev.secdb.IStream;
import java.io.ByteArrayInputStream;
import java.io.InputStream;


/**
 * Byte Array based IStream.
 */
public class ByteArrayIStream
	implements IStream
{
	private final byte[] bytes;
	
	
	public ByteArrayIStream(byte[] bytes)
	{
		this.bytes = bytes;
	}
	

	public InputStream getStream()
	{
		return new ByteArrayInputStream(bytes);
	}


	public long getLength()
	{
		return bytes.length;
	}


	public byte[] readBytes(int limit) throws Exception
	{
		if(limit > bytes.length)
		{
			return bytes.clone();
		}
		else
		{
			throw new Exception("object is too large (" + bytes.length + ") for specified limit " + limit);
		}
	}
}
