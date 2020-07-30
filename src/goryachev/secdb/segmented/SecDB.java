// Copyright © 2019-2020 Andy Goryachev <andy@goryachev.com>
package goryachev.secdb.segmented;
import goryachev.common.util.SKey;
import goryachev.crypto.OpaqueBytes;
import goryachev.crypto.OpaqueChars;
import goryachev.secdb.DBEngine;
import goryachev.secdb.IStored;
import goryachev.secdb.IStream;
import goryachev.secdb.QueryClient;
import goryachev.secdb.internal.DataHolder;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.security.SecureRandom;
import java.util.List;


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
	
	
	/** checks the directory for database files, returns true if all required files are present. */
	public static boolean isPresent(File dir)
	{
		return SecStore.isPresent(dir);
	}
	
	
	public static void create(File dir, OpaqueBytes key, OpaqueChars passphrase) throws Exception
	{
		create(dir, key, passphrase, new SecureRandom());
	}
	
	
	public static void create(File dir, OpaqueBytes key, OpaqueChars passphrase, SecureRandom random) throws Exception
	{
		SecStore.create(dir, key, passphrase, random);
	}
	
	
	public static SecDB open(File dir, OpaqueChars passphrase) throws Exception, SecException
	{
		SecStore st = SecStore.open(dir, passphrase);
		return new SecDB(st);
	}
	
	
	public void close() throws IOException
	{
		store.close();
	}
	
	
	public void execute(Transaction tx)
	{
		engine.execute(tx);
	}
	
	
	/** executes a Transaction which inserts a single value */
	// TODO communicate error?
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
	
	
	public void remove(SKey key)
	{
		execute(new Transaction()
		{
			protected void body() throws Exception
			{
				remove(key);
			}
			
			
			protected void onFinish() 
			{
			}
		});
	}
	
	
	public void remove(List<SKey> keys)
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
			
			
			protected void onFinish() 
			{
			}
		});
	}
	
	
	public IStored load(SKey key) throws Exception
	{
		DataHolder<Ref> ref = engine.getValue(key);
		return ref == null ? null : ref.getStoredValue();
	}
	
	
	/** range query.  'start' may be less than, greater than, or equal to 'end'.  returns true if no exceptions have been thrown */
	public boolean query(SKey start, boolean includeStart, SKey end, boolean includeEnd, QueryClient<SKey,IStored> client)
	{
		return engine.rangeQuery(start, includeStart, end, includeEnd, new QueryClient<SKey,DataHolder<Ref>>()
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
	
	
	/** simplified range query [start,end[.  returns true if no errors */
	public boolean query(SKey start, SKey end, QueryClient<SKey,IStored> client)
	{
		return query(start, true, end, false, client);
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
