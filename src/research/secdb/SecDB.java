// Copyright © 2019 Andy Goryachev <andy@goryachev.com>
package research.secdb;
import java.io.Closeable;
import java.io.IOException;


/**
 * Secure Key-Value Store.
 */
public class SecDB<K extends Comparable<? super K>,R>
	implements Closeable, IKeyValueStore<K,R>
{
	@FunctionalInterface
	public interface QueryCallback<K,V>
	{
		/** accepts query results.  the query is aborted when this callback returns false */
		public boolean acceptQueryResult(K key, V value);
	}
	
	private final IStore store;
	private final IEncryptor encryptor;
	
	
	public SecDB(IStore store, IEncryptor enc)
	{
		this.store = store;
		this.encryptor = enc;
	}
	
	
	public SecDB(IStore store)
	{
		this(store, IEncryptor.NONE);
	}
	
	
	public void open() throws Exception
	{
		store.open();
	}
	
	
	public void close() throws IOException
	{
		store.close();
	}


	public void commit() throws Exception
	{
	}


	public IStream getValue(K key) throws Exception
	{
		return null;
	}


	public R putValue(K key, IStream in) throws Exception
	{
		return null;
	}


	public boolean containsKey(K key) throws Exception
	{
		return false;
	}


	public boolean removeKey(K key) throws Exception
	{
		return false;
	}
	
	
	public void query(K start, boolean includeStart, K end, boolean includeEnd, QueryCallback client)
	{
		// TODO
	}
}
