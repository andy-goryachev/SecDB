// Copyright © 2014-2019 Andy Goryachev <andy@goryachev.com>
package research.secdb;


public interface IKeyValueStore<K extends Comparable<? super K>, R>
{
	/** returns input stream for a stored data value.  returns null when no value exists for a given key. */
	public IStream getValue(K key) throws Exception;
	
	
	/** inserts an object with the key, returns the storage reference */
	public R putValue(K key, IStream in) throws Exception;
	
	
	/** returns true if the database contains the specified key */
	public boolean containsKey(K key) throws Exception;
	
	
	/** removes key, returns true if key existed */
	public boolean removeKey(K key) throws Exception;
}
