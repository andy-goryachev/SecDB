// Copyright © 2016-2022 Andy Goryachev <andy@goryachev.com>
package goryachev.secdb;
import goryachev.common.util.CKit;
import goryachev.secdb.util.ByteArrayIStream;
import java.io.IOException;
import java.io.InputStream;


/**
 * Input Stream wrapper with additional convenience methods.
 */
public interface IStream
{
	public InputStream getStream();
	
	
	public long getLength();
	
	
	/** reads data into a new byte array, as long as the object size is below the limit; throws an exception otherwise */
	default public byte[] readBytes(int limit) throws Exception
	{
		long len = getLength();
		if(len > limit)
		{
			throw new IOException("object is too large: size=" + len + ", limit=" + limit);
		}
		
		byte[] b = new byte[(int)len];
		InputStream is = getStream();
		try
		{
			CKit.readFully(is, b);
			return b;
		}
		finally
		{
			CKit.close(is);
		}
	}
	
	
	//
	
	
	/** returns an IStream created from UTF-8 representation of the given non-null string */
	public static IStream of(String text)
	{
		byte[] b = text.getBytes(CKit.CHARSET_UTF8);
		return of(b);
	}
	
	
	/** returns an IStream created from the given non-null byte array */
	public static IStream of(byte[] data)
	{
		return new ByteArrayIStream(data);
	}
}
