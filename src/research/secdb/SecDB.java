// Copyright © 2019 Andy Goryachev <andy@goryachev.com>
package research.secdb;
import goryachev.common.util.Log;
import goryachev.common.util.SKey;
import java.io.Closeable;
import java.io.IOException;
import research.bplustree.BPlusTreeNode;
import research.bplustree.QueryClient;


/**
 * Secure Key-Value Store.
 */
public class SecDB
	implements Closeable
{
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
	
	
	protected BPlusTreeNode<SKey,DataHolder> loadRoot() throws Exception
	{
		Ref ref = store.getRootRef();
		// TODO if ref == null
		IStream in = store.load(ref);
		return readNode(in);
	}
	
	
	protected BPlusTreeNode<SKey,DataHolder> readNode(IStream is) throws Exception
	{
		byte[] b = is.readBytes(NODE_SIZE_LIMIT);
		byte[] dec = encryptor.decrypt(b);
		try
		{
			return SecIO.read(store, dec);
		}
		finally
		{
			encryptor.zero(dec);
		}
	}


	protected void commit(BPlusTreeNode<SKey,DataHolder> newRoot) throws Exception
	{
		// TODO write nodes and values depth first
		// TODO write new root
		// TODO set root ref
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
			tx.setRoot(root);
			
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
}
