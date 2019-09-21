// Copyright © 2019 Andy Goryachev <andy@goryachev.com>
package goryachev.secdb;
import java.io.ByteArrayInputStream;
import java.io.InputStream;


/**
 * ByteArray IStream.
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
