// Copyright © 2019-2021 Andy Goryachev <andy@goryachev.com>
package goryachev.secdb;


/**
 * Stored Object interface.
 */
public interface IStored
{
	public long getLength();
	
	
	public IStream getIStream() throws Exception;
}
