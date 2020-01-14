// Copyright © 2019 Andy Goryachev <andy@goryachev.com>
package goryachev.secdb.segmented;
import goryachev.common.util.SKey;
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
	
	
	public SecDB(File dir)
	{
		store = new SecStore(dir);
	}
	
	
	private SecDB(SecStore s)
	{
		store = s;
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
	
	
	// TODO DataHolder -> IStored
	public void query(SKey start, boolean includeStart, SKey end, boolean includeEnd, QueryClient<SKey,DataHolder<Ref>> client) throws Exception
	{
		// TODO
	}
}
