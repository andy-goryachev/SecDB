// Copyright © 2019-2020 Andy Goryachev <andy@goryachev.com>
package goryachev.secdb.segmented;
import goryachev.common.util.SKey;
import goryachev.crypto.OpaqueChars;
import goryachev.secdb.DBEngine;
import goryachev.secdb.IStored;
import goryachev.secdb.IStream;
import goryachev.secdb.QueryClient;
import goryachev.secdb.internal.DataHolder;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;


/**
 * Secure Key-Value Database with range queries.
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
	
	
	public static void create(File dir, char[] passphrase) throws Exception
	{
		SecStore.create(dir, passphrase);
	}
	
	
	public static SecDB open(File dir, OpaqueChars passphrase) throws Exception
	{
		SecStore st = SecStore.open(dir, passphrase);
		return new SecDB(st);
	}
	
	
	public void close() throws IOException
	{
		store.close();
	}
	
	
	public IStored getValue(SKey key) throws Exception
	{
		DataHolder<Ref> ref = engine.getValue(key);
		return ref == null ? null : ref.getStoredValue();
	}
	
	
	public void execute(Transaction tx)
	{
		engine.execute(tx);
	}
	
	
	/** executes a Transaction which inserts a single value */ 
	public void store(SKey key, IStream in, Runnable onFinish)
	{
		execute(new Transaction()
		{
			protected void body() throws Exception
			{
				insert(key, in);
			}
			
			
			protected void onFinish() 
			{
				if(onFinish != null)
				{
					onFinish.run();
				}
			}
		});
	}
	
	
	/** range query.  'start' may be less than, greater than, or equal to 'end' */
	public void query(SKey start, boolean includeStart, SKey end, boolean includeEnd, QueryClient<SKey,IStored> client)
	{
		engine.query(start, includeStart, end, includeEnd, new QueryClient<SKey,DataHolder<Ref>>()
		{
			public void onError(Throwable err)
			{
				client.onError(err);
			}
			
			
			public boolean acceptQueryResult(SKey key, DataHolder<Ref> h)
			{
				IStored v = h.getStoredValue();
				return client.acceptQueryResult(key, v);
			}
		});
	}
	
	
	/** simplified range query [start,end[ */
	public void query(SKey start, SKey end, QueryClient<SKey,IStored> client)
	{
		query(start, true, end, false, client);
	}
}
