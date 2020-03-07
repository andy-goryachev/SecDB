// Copyright © 2019-2020 Andy Goryachev <andy@goryachev.com>
package goryachev.secdb.segmented;
import goryachev.common.util.SKey;
import goryachev.secdb.DBEngine;
import goryachev.secdb.IStored;
import goryachev.secdb.QueryClient;
import goryachev.secdb.DBTransaction;
import goryachev.secdb.internal.DataHolder;
import goryachev.secdb.segmented.log.LogFile;
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
	
	
	// TODO OpaqueString
	public static SecDB open(File dir, char[] passphrase) throws Exception
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
	
	
	public void execute(DBTransaction<Ref> tx)
	{
		engine.execute(tx);
	}
	
	
	// TODO store single value
	
	
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
