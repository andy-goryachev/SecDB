// Copyright © 2019 Andy Goryachev <andy@goryachev.com>
package research.secdb;
import goryachev.common.util.Log;
import java.io.Closeable;
import java.io.IOException;
import research.bplustree.BPlusTree;


/**
 * Secure Key-Value Store.
 * K: key type
 * R: reference type
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

	private static final int TREE_BRANCHING_FACTOR = 4;
	private final IStore<R> store;
	private final IEncryptor encryptor;
	private final BPlusTree<K,IStored> tree;
	
	
	public SecDB(IStore store, IEncryptor enc)
	{
		this.store = store;
		this.encryptor = enc;
		// TODO load root node
		this.tree = new BPlusTree(TREE_BRANCHING_FACTOR)
		{
			// TODO loading nodes
		};
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


	public synchronized void submit(Transaction tx)
	{
		try
		{
			tx.setDB(this);
			
			tx.body();
			
			try
			{
				tx.onSuccess();
			}
			catch(Throwable e)
			{
				Log.ex(e);
			}
		}
		catch(Throwable err)
		{
			try
			{
				tx.onError(err);
			}
			catch(Throwable e)
			{
				Log.ex(e);
			}
		}
		finally
		{
			try
			{
				tx.onFinish();
			}
			catch(Throwable e)
			{
				Log.ex(e);
			}
		}
	}
}
