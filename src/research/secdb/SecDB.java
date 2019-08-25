// Copyright © 2019 Andy Goryachev <andy@goryachev.com>
package research.secdb;
import java.io.Closeable;
import java.io.IOException;
import research.bplustree.BPlusTree;


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
		public boolean acceptQueryResult(K key, IStored value);
	}

	private static final int BRANCHING_FACTOR = 4;
	private final IStore store;
	private final IEncryptor encryptor;
	private final BPlusTree<K,IStored> tree;
	
	
	public SecDB(IStore store, IEncryptor enc)
	{
		this.store = store;
		this.encryptor = enc;
		this.tree = new BPlusTree(BRANCHING_FACTOR);
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
