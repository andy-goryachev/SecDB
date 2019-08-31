// Copyright © 2019 Andy Goryachev <andy@goryachev.com>
package research.secdb;


/**
 * Stored Object interface.
 */
public interface IStored
{
	public boolean hasValue();
	
	
	public long getLength();
	
	
	public IStream getIStream();
}
