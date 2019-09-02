// Copyright © 2019 Andy Goryachev <andy@goryachev.com>
package research.secdb;
import goryachev.common.util.Log;
import goryachev.common.util.SKey;
import java.io.Closeable;
import java.io.IOException;
import research.bplustree.BPlusTree;


/**
 * Secure Key-Value Store.
 * K: key type
 * R: reference type
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
	private final BPlusTree<String,IStored> tree;
	
	
	public SecDB(IStore store, IEncryptor enc)
	{
		this.store = store;
		this.encryptor = enc;
		
		// TODO load root node
		tree = new BPlusTree(TREE_BRANCHING_FACTOR)
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
	
	
	protected BPlusTree<SKey,IStored>.Node loadRoot() throws Exception
	{
		Ref ref = store.getRootRef();
		IStream in = store.load(ref);
		return readNode(in);
	}
	
	
	protected BPlusTree<SKey,IStored>.Node readNode(IStream is) throws Exception
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


	public void commit(BPlusTree<SKey,byte[]>.Node newRoot) throws Exception
	{
		// TODO
	}


	public IStream getValue(SKey key) throws Exception
	{
		return null;
	}


	public Ref putValue(SKey key, IStream in) throws Exception
	{
		return null;
	}


	public boolean containsKey(SKey key) throws Exception
	{
		return false;
	}


	public boolean removeKey(SKey key) throws Exception
	{
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
			BPlusTree<SKey,IStored>.Node root = loadRoot(); 
			tx.setRoot(root);
			
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
