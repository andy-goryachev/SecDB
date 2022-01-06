// Copyright © 2019-2022 Andy Goryachev <andy@goryachev.com>
package goryachev.secdb.segmented;
import goryachev.common.util.SKey;
import goryachev.secdb.DBEngine;
import goryachev.secdb.IStored;
import goryachev.secdb.IStream;
import goryachev.secdb.QueryClient;
import goryachev.secdb.internal.DataHolder;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.util.Collection;


/**
 * Secure Key-Value Database with range and prefix queries.
 */
public class SecDB
	implements Closeable
{
	protected final SecStore store;
	protected final DBEngine<Ref> engine;
	
	
	private SecDB(SecStore s)
	{
		store = s;
		engine = new DBEngine(store);
	}
	
	
	/** checks the directory for database files, returns true if all required files are present. */
	public static boolean isPresent(File dir)
	{
		return SecStore.isPresent(dir);
	}
	
	
	public static void create(File dir, IEncHelper encHelper) throws Exception
	{
		SecStore.create(dir, encHelper);
	}
	
	
	public static SecDB open(File dir, IEncHelper encHelper) throws Exception, SecException
	{
		SecStore st = SecStore.open(dir, encHelper);
		return new SecDB(st);
	}
	
	
	public void close() throws IOException
	{
		store.close();
	}
	
	
	public void execute(Transaction tx) throws Exception
	{
		engine.execute(tx);
	}
	
	
	/** executes a Transaction which inserts a single value */
	public void store(SKey key, IStream in) throws Exception
	{
		execute(new Transaction()
		{
			protected void body() throws Exception
			{
				insert(key, in);
			}
		});
	}
	
	
	public void remove(SKey key) throws Exception
	{
		execute(new Transaction()
		{
			protected void body() throws Exception
			{
				remove(key);
			}
		});
	}
	
	
	public void remove(Collection<SKey> keys) throws Exception
	{
		execute(new Transaction()
		{
			protected void body() throws Exception
			{
				for(SKey k: keys)
				{
					remove(k);
				}
			}
		});
	}
	
	
	public IStored load(SKey key) throws Exception
	{
		DataHolder<Ref> ref = engine.getValue(key);
		return ref == null ? null : ref.getStoredValue();
	}
	
	
	/** range query.  'start' may be less than, greater than, or equal to 'end'. */
	public void rangeQuery(SKey start, boolean includeStart, SKey end, boolean includeEnd, QueryClient<SKey,IStored> client) throws Exception
	{
		engine.rangeQuery(start, includeStart, end, includeEnd, new QueryClient<SKey,DataHolder<Ref>>()
		{
			public boolean acceptQueryResult(SKey key, DataHolder<Ref> h) throws Exception
			{
				IStored v = h.getStoredValue();
				return client.acceptQueryResult(key, v);
			}
		});
	}
	
	
	/** prefix query */
	public void prefixQuery(SKey prefix, QueryClient<SKey,IStored> client) throws Exception
	{
		engine.prefixQuery(prefix, new QueryClient<SKey,DataHolder<Ref>>()
		{
			public boolean acceptQueryResult(SKey key, DataHolder<Ref> h) throws Exception
			{
				IStored v = h.getStoredValue();
				return client.acceptQueryResult(key, v);
			}
		});
	}
	
	
	/** reverse prefix query */
	public void prefixQueryReverse(SKey prefix, QueryClient<SKey,IStored> client) throws Exception
	{
		engine.prefixReverseQuery(prefix, new QueryClient<SKey,DataHolder<Ref>>()
		{
			public boolean acceptQueryResult(SKey key, DataHolder<Ref> h) throws Exception
			{
				IStored v = h.getStoredValue();
				return client.acceptQueryResult(key, v);
			}
		});
	}


	public void dump()
	{
		try
		{
			engine.dump(System.err, " ");
		}
		catch(Throwable e)
		{
			e.printStackTrace();
		}
	}
	
	
	public void dumpKeys()
	{
		try
		{
			engine.dumpKeys(System.err, " ");
		}
		catch(Throwable e)
		{
			e.printStackTrace();
		}
	}
}
