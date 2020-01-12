// Copyright © 2019 Andy Goryachev <andy@goryachev.com>
package goryachev.secdb;


/**
 * A stored object Reference, specific to IStore.
 */
public interface IRef
{
	public long getLength();
	
	public int hashCode();
	
	public boolean equals(Object x);
}
