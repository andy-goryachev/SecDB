// Copyright © 2019-2021 Andy Goryachev <andy@goryachev.com>
package goryachev.secdb;
import goryachev.common.log.Log;
import goryachev.common.util.SKey;
import goryachev.secdb.bplustree.BPlusTreeNode;
import goryachev.secdb.internal.DBEngineIO;
import goryachev.secdb.internal.DBLeafNode;
import goryachev.secdb.internal.DataHolder;
import java.util.function.Predicate;


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
		byte[] b = is.readBytes(NODE_SIZE_LIMIT);
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

	
	/** performs a range query */
	public void rangeQuery(SKey start, boolean includeStart, SKey end, boolean includeEnd, QueryClient<SKey,DataHolder<R>> client) throws Exception
	{
		loadRoot().rangeQuery(start, includeStart, end, includeEnd, client);
	}
	
	
	/** 
	 * Finds all the entries where the key "starts with" the given prefix.  
	 */ 
	public void prefixQuery(SKey prefix, QueryClient<SKey,DataHolder<R>> client) throws Exception
	{
		loadRoot().prefixQuery(prefix, mkPrefix(prefix), client);
	}
	
	
	/** 
	 * Finds all the entries where the key "starts with" the given prefix, in reverse order.  
	 */ 
	public void prefixReverseQuery(SKey prefix, QueryClient<SKey,DataHolder<R>> client) throws Exception
	{
		loadRoot().prefixReverseQuery(prefix, mkPrefix(prefix), client);
	}
	
	
	protected Predicate<SKey> mkPrefix(SKey prefix)
	{
		return new Predicate<SKey>()
		{
			public boolean test(SKey key)
			{
				return key.toString().startsWith(prefix.toString());
			}
		};
	}


	public synchronized void execute(DBTransaction<R> tx) throws Exception
	{
		BPlusTreeNode<SKey,DataHolder<R>> root = loadRoot();
		tx.setRoot(store, root, BRANCHING_FACTOR);

		tx.body();

		BPlusTreeNode<SKey,DataHolder<R>> newRoot = tx.getRoot();
		if(newRoot != null)
		{
			commit(newRoot);
		}
	}
	

	protected void commit(BPlusTreeNode<SKey,DataHolder<R>> newRoot) throws Exception
	{
		R ref = DBEngineIO.store(store, newRoot);
		store.setRootRef(ref);
		
		log.debug("new root=%s", ref);
	}


	public void dump(Appendable out, String indent) throws Exception
	{
		BPlusTreeNode<SKey,DataHolder<R>> root = loadRoot();
		root.dump(out, indent, 0);
	}
	
	
	public void dumpKeys(Appendable out, String indent) throws Exception
	{
		BPlusTreeNode<SKey,DataHolder<R>> root = loadRoot();
		root.dumpKeys(out, indent, 0);
	}
}
