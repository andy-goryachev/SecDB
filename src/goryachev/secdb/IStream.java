// Copyright © 2016-2020 Andy Goryachev <andy@goryachev.com>
package goryachev.secdb;
import java.io.InputStream;


/**
 * Input Stream wrapper with additional convenience methods.
 */
public interface IStream
{
	public InputStream getStream();
	
	
	public long getLength();
}
