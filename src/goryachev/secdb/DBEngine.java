// Copyright © 2019-2020 Andy Goryachev <andy@goryachev.com>
package goryachev.secdb;
import goryachev.common.log.Log;
import goryachev.common.util.SKey;
import goryachev.secdb.bplustree.BPlusTreeNode;
import goryachev.secdb.internal.DBEngineIO;
import goryachev.secdb.internal.DBLeafNode;
import goryachev.secdb.internal.DataHolder;
import goryachev.secdb.util.Utils;


/**
 * Key-Value Database Engine.
 */
public class DBEngine<R extends IRef>
{
	private static final int BRANCHING_FACTOR = 4;
	private static final int NODE_SIZE_LIMIT = 1_000_000;
	protected static final Log log = Log.get("DBEngine");
	private final IStore<R> store;
	
	
	public DBEngine(IStore<R> store)
	{
		this.store = store;
	}
	
	
	protected BPlusTreeNode<SKey,DataHolder<R>> loadRoot() throws Exception
	{
		R ref = store.getRootRef();
		log.debug("root=%s", ref);
		
		if(ref == null)
		{
			return DBLeafNode.createModified(store);
		}
		else
		{
			IStream in = store.load(ref);
			try
			{
				return readNode(in);
			}
			catch(Throwable e)
			{
				throw new Exception("at ref=" + ref, e);
			}
		}
	}
	
	
	protected BPlusTreeNode<SKey,DataHolder<R>> readNode(IStream is) throws Exception
	{
		byte[] b = Utils.readBytes(is, NODE_SIZE_LIMIT);
		return DBEngineIO.read(store, b);
	}


	public DataHolder<R> getValue(SKey key) throws Exception
	{
		DataHolder<R> h = loadRoot().getValue(key);
		log.trace("key=%s, ref=%s", key, h);
		return h;
	}


	public boolean containsKey(SKey key) throws Exception
	{
		return loadRoot().containsKey(key);
	}

	
	/** performs a range query, returns true if no exceptions (Throwables) have been thrown, false otherwise */
	public boolean rangeQuery(SKey start, boolean includeStart, SKey end, boolean includeEnd, QueryClient<SKey,DataHolder<R>> client)
	{
		try
		{
			loadRoot().rangeQuery(start, includeStart, end, includeEnd, client);
			return true;
		}
		catch(Throwable e)
		{
			client.onError(e);
			return false;
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
				log.error(e);
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
				log.error(e);
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
				log.error(e);
			}
		}
	}
	

	protected void commit(BPlusTreeNode<SKey,DataHolder<R>> newRoot) throws Exception
	{
		R ref = DBEngineIO.store(store, newRoot);
		store.setRootRef(ref);
		
		log.debug("new root=%s", ref);
	}


	public void dumpTree() throws Exception
	{
		BPlusTreeNode<SKey,DataHolder<R>> root = loadRoot();
		root.dump(System.err, 0);
	}
}
