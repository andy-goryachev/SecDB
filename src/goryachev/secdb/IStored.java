// Copyright © 2019 Andy Goryachev <andy@goryachev.com>
package goryachev.secdb;


/**
 * Stored Object interface.
 */
public interface IStored
{
	public boolean hasValue();
	
	
	public long getLength();
	
	
	public IStream getIStream() throws Exception;
}
