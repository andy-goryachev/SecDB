// Copyright © 2019 Andy Goryachev <andy@goryachev.com>
package research.secdb;
import goryachev.common.util.Log;
import goryachev.common.util.SKey;
import java.io.Closeable;
import java.io.IOException;


/**
 * Secure Key-Value Store.
 */
public class SecDB
	implements Closeable, IKeyValueStore<SKey,Ref>
{
	@FunctionalInterface
	public interface QueryCallback<K,V>
	{
		/** accepts query results.  the query is aborted when this callback returns false */
		public boolean acceptQueryResult(K key, IStored value);
	}

	private static final int TREE_BRANCHING_FACTOR = 4;
	private static final int NODE_SIZE_LIMIT = 1_000_000;
	private final IStore<Ref> store;
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
	
	
	protected SecNode loadRoot() throws Exception
	{
		Ref ref = store.getRootRef();
		IStream in = store.load(ref);
		return readNode(in);
	}
	
	
	protected SecNode readNode(IStream is) throws Exception
	{
		byte[] b = is.readBytes(NODE_SIZE_LIMIT);
		byte[] dec = encryptor.decrypt(b);
		try
		{
			return SecNode.read(dec);
		}
		finally
		{
			encryptor.zero(dec);
		}
	}


	protected void commit(SecNode newRoot) throws Exception
	{
		// TODO
	}


	public IStream getValue(SKey key) throws Exception
	{
		// TODO
		return null;
	}


	public Ref putValue(SKey key, IStream in) throws Exception
	{
		// TODO
		return null;
	}


	public boolean containsKey(SKey key) throws Exception
	{
		// TODO
		return false;
	}


	public boolean removeKey(SKey key) throws Exception
	{
		// TODO
		return false;
	}
	
	
	public void query(SKey start, boolean includeStart, SKey end, boolean includeEnd, QueryCallback client)
	{
		// TODO
	}


	public synchronized void execute(Transaction tx)
	{
		try
		{
			SecNode root = loadRoot(); 
			tx.setRoot(root);
			
			tx.body();
			
			commit(root);
			
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
