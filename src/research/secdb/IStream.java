// Copyright © 2016-2019 Andy Goryachev <andy@goryachev.com>
package research.secdb;
import java.io.InputStream;


/**
 * Input Stream wrapper with additional convenience methods.
 */
public interface IStream
{
	public InputStream getStream();
	
	
	public long getLength();


	public byte[] readBytes(int limit) throws Exception;
}
