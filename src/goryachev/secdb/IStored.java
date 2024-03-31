// Copyright © 2019-2024 Andy Goryachev <andy@goryachev.com>
package goryachev.secdb;


/**
 * Stored Object interface.
 */
public interface IStored
{
	public long getLength();
	
	
	public IStream getIStream() throws Exception;
	
	
	/** reads data into a new byte array, as long as the object size is below the limit; throws an exception otherwise */
	default public byte[] readBytes(int limit) throws Exception
	{
		return getIStream().readBytes(limit);
	}
}
