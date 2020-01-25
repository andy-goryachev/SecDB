// Copyright © 2019-2020 Andy Goryachev <andy@goryachev.com>
package goryachev.secdb;
import goryachev.common.util.Log;
import goryachev.common.util.SKey;
import goryachev.secdb.bplustree.BPlusTreeNode;
import goryachev.secdb.internal.DBEngineIO;
import goryachev.secdb.internal.DBLeafNode;
import goryachev.secdb.internal.DataHolder;


/**
 * Key-Value Database Engine.
 */
public class DBEngine<R extends IRef>
{
	private static final int BRANCHING_FACTOR = 4;
	private static final int NODE_SIZE_LIMIT = 1_000_000;
	private final IStore<R> store;
	
	
	public DBEngine(IStore<R> store)
	{
		this.store = store;
	}
	
	
	protected BPlusTreeNode<SKey,DataHolder<R>> loadRoot() throws Exception
	{
		R ref = store.getRootRef();
		if(ref == null)
		{
			return new DBLeafNode(store).modified();
		}
		else
		{
			IStream in = store.load(ref);
			return readNode(in);
		}
	}
	
	
	protected BPlusTreeNode<SKey,DataHolder<R>> readNode(IStream is) throws Exception
	{
		byte[] b = is.readBytes(NODE_SIZE_LIMIT);
		return DBEngineIO.read(store, b);
	}


	public DataHolder<R> getValue(SKey key) throws Exception
	{
		return loadRoot().getValue(key);
	}


	public boolean containsKey(SKey key) throws Exception
	{
		return loadRoot().containsKey(key);
	}

	
	public void query(SKey start, boolean includeStart, SKey end, boolean includeEnd, QueryClient<SKey,DataHolder<R>> client)
	{
		try
		{
			loadRoot().query(start, includeStart, end, includeEnd, client);
		}
		catch(Throwable e)
		{
			client.onError(e);
		}
	}


	public synchronized void execute(DBTransaction<R> tx)
	{
		try
		{
			BPlusTreeNode<SKey,DataHolder<R>> root = loadRoot(); 
			tx.setRoot(store, root, BRANCHING_FACTOR);
			
			tx.body();
			
			BPlusTreeNode<SKey,DataHolder<R>> newRoot = tx.getRoot();
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
	

	protected void commit(BPlusTreeNode<SKey,DataHolder<R>> newRoot) throws Exception
	{
		R ref = DBEngineIO.store(store, newRoot);
		store.setRootRef(ref);
	}
}
