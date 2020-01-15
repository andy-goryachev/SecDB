// Copyright © 2019 Andy Goryachev <andy@goryachev.com>
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


/**
 * Secure Key-Value Database with range queries.
 */
public class SecDB
	implements Closeable
{
	protected final SecStore store;
	protected final DBEngine<Ref> engine;
	
	
	public SecDB(File dir)
	{
		store = new SecStore(dir);
		engine = new DBEngine(store);
	}
	
	
	private SecDB(SecStore s)
	{
		store = s;
		engine = new DBEngine(store);
	}
	
	
	public static SecDB create(File dir, char[] passphrase) throws Exception
	{
		SecStore st = SecStore.create(dir, passphrase);
		return new SecDB(st);
	}
	
	
	// TODO OpaqueString
	public void open(char[] passphrase) throws Exception
	{
		store.open(passphrase);
	}
	
	
	public void close() throws IOException
	{
		store.close();
	}
	
	
	public IStream getValue(SKey key) throws Exception
	{
		// TODO
		return null;
	}
	
	
	// TODO transaction
	// TODO store single value
	
	
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
}
