// Copyright © 2019-2021 Andy Goryachev <andy@goryachev.com>
package goryachev.secdb;
import goryachev.common.test.TF;
import goryachev.common.test.Test;
import goryachev.common.util.CKit;
import goryachev.common.util.D;
import goryachev.common.util.Hex;
import goryachev.common.util.SKey;
import goryachev.secdb.internal.DataHolder;
import goryachev.secdb.util.ByteArrayIStream;
import goryachev.secdb.util.Utils;


/**
 * Tests prefix query in InMemoryStore.
 */
public class TestPrefixQuery
{
	public static void main(String[] args)
	{
		TF.run();
	}
	
	
	@Test
	public void test() throws Exception
	{
		InMemoryStore store = new InMemoryStore(false);
		DBEngine<InMemoryRef> db = new DBEngine(store);

		db.execute(new DBTransaction<InMemoryRef>()
		{
			protected void body() throws Exception
			{
				for(int i=0; i<1000; i++)
				{
					SKey k = key(i);
					byte[] v = ("v." + k).getBytes(CKit.CHARSET_UTF8);
					
					TF.eq(containsKey(k), false);
					
					insert(k, new ByteArrayIStream(v));
					
					TF.eq(containsKey(k), true);
					
					DataHolder h = read(k);
					byte[] rv = Utils.readBytes(h.getStoredValue().getIStream(), Integer.MAX_VALUE);
					TF.eq(v, rv);
				}
			}
		});
			
		db.dump(System.err, " ");
					
		// query
		SKey prefix = key(90);
		
		D.print("prefix query", prefix);
		
		db.prefixQuery(prefix, new QueryClient<SKey,DataHolder<InMemoryRef>>()
		{
			public void onError(Throwable err)
			{
				err.printStackTrace();
			}
			
			
			public boolean acceptQueryResult(SKey key, DataHolder<InMemoryRef> value)
			{
				try
				{
					byte[] b = Utils.readBytes(value.getStoredValue().getIStream(), Integer.MAX_VALUE);
					D.print("    query:", key, Hex.toHexString(b));
				}
				catch(Exception e)
				{
					e.printStackTrace();
				}
				return true;
			}
		});
	}
	
	
	protected static SKey key(int n)
	{
		return new SKey(String.valueOf(n));
	}
}
