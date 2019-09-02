// Copyright © 2014-2019 Andy Goryachev <andy@goryachev.com>
package research.secdb;


// TODO not sure if this is still needed
public interface IKeyValueStore<K extends Comparable<? super K>, R>
{
	/** returns input stream for a stored data value.  returns null when no value exists for a given key. */
	public IStored getValue(K key) throws Exception;
	
	
	/** returns true if the database contains the specified key */
	public boolean containsKey(K key) throws Exception;
}
