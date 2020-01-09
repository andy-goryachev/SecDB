// Copyright © 2019-2020 Andy Goryachev <andy@goryachev.com>
package goryachev.secdb;
import goryachev.common.util.Log;
import goryachev.common.util.SKey;
import goryachev.secdb.bplustree.BPlusTreeNode;
import goryachev.secdb.bplustree.QueryClient;
import goryachev.secdb.impl.DataHolder;
import goryachev.secdb.impl.SecIO;
import goryachev.secdb.impl.SecLeafNode;


/**
 * Secure Key-Value Store.
 */
public class SecDB
{
	private static final int BRANCHING_FACTOR = 4;
	private static final int NODE_SIZE_LIMIT = 1_000_000;
	private final IStore<Ref> store;
	
	
	public SecDB(IStore<Ref> store)
	{
		this.store = store;
	}
	
	
	protected BPlusTreeNode<SKey,DataHolder> loadRoot() throws Exception
	{
		Ref ref = store.getRootRef();
		if(ref == null)
		{
			return new SecLeafNode(store).modified();
		}
		else
		{
			IStream in = store.load(ref);
			return readNode(in);
		}
	}
	
	
	protected BPlusTreeNode<SKey,DataHolder> readNode(IStream is) throws Exception
	{
		byte[] b = is.readBytes(NODE_SIZE_LIMIT);
		return SecIO.read(store, b);
	}


	public DataHolder getValue(SKey key) throws Exception
	{
		return loadRoot().getValue(key);
	}


	public boolean containsKey(SKey key) throws Exception
	{
		return loadRoot().containsKey(key);
	}

	
	public void query(SKey start, boolean includeStart, SKey end, boolean includeEnd, QueryClient<SKey,DataHolder> client) throws Exception
	{
		loadRoot().query(start, includeStart, end, includeEnd, client);
	}


	public synchronized void execute(Transaction tx)
	{
		try
		{
			BPlusTreeNode<SKey,DataHolder> root = loadRoot(); 
			tx.setRoot(store, root, BRANCHING_FACTOR);
			
			tx.body();
			
			BPlusTreeNode<SKey,DataHolder> newRoot = tx.getRoot();
			if(newRoot != null)
			{
				commit(newRoot);
			}
			
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
	

	protected void commit(BPlusTreeNode<SKey,DataHolder> newRoot) throws Exception
	{
		Ref ref = SecIO.store(store, newRoot);
		store.setRootRef(ref);
	}
}
