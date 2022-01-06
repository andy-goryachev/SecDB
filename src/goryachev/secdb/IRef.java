// Copyright © 2019-2022 Andy Goryachev <andy@goryachev.com>
package goryachev.secdb;


/**
 * A reference of a data object stored in IStore.
 */
public interface IRef
{
	/** returns the amount of storage used (data length + storage and encryption overhead) */
	public long getLength();
	
	public int hashCode();
	
	public boolean equals(Object x);
}
