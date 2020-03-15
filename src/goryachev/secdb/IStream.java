// Copyright © 2016-2020 Andy Goryachev <andy@goryachev.com>
package goryachev.secdb;
import goryachev.common.util.CKit;
import goryachev.secdb.util.ByteArrayIStream;
import java.io.InputStream;


/**
 * Input Stream wrapper with additional convenience methods.
 */
public interface IStream
{
	public InputStream getStream();
	
	
	public long getLength();
	
	
	/** returns an IStream created from UTF-8 representation of the given non-null string */
	public static IStream of(String text)
	{
		return new ByteArrayIStream(text.getBytes(CKit.CHARSET_UTF8));
	}
	
	
	/** returns an IStream created from the given non-null byte array */
	public static IStream of(byte[] data)
	{
		return new ByteArrayIStream(data);
	}
}
