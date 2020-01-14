// Copyright © 2019 Andy Goryachev <andy@goryachev.com>
package goryachev.secdb.segmented;
import goryachev.common.util.SKey;
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
	
	
	// TODO OpaqueString
	public void open(char[] passphrase) throws Exception
	{
		store.open(passphrase);
	}
	
	
	public void close() throws IOException
	{
		store.close();
	}
	
	
	public DataHolder<Ref> getValue(SKey key) throws Exception
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
