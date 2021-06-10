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
 * Tests range query in InMemoryStore.
 */
public class TestRangeQuery
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
		
		int max = 20;

		db.execute(new DBTransaction<InMemoryRef>()
		{
			protected void body() throws Exception
			{
				for(int i=0; i<max; i++)
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
			
//		db.dump(System.err, " ");
		
		QueryClient<SKey,DataHolder<InMemoryRef>> client = new QueryClient<SKey,DataHolder<InMemoryRef>>()
		{
			public boolean acceptQueryResult(SKey key, DataHolder<InMemoryRef> value) throws Exception
			{
				byte[] b = Utils.readBytes(value.getStoredValue().getIStream(), Integer.MAX_VALUE);
				D.print("    query:", key, new String(b, CKit.CHARSET_UTF8));
				return true;
			}
		};
					
		// query
		SKey start = key(1);
		SKey end = key(9);
		
		D.print("forward rangeQuery query", start, end);
		db.rangeQuery(start, true, end, true, client);
		
		D.print("backward rangeQuery query", end, start);
		db.rangeQuery(end, true, start, true, client);
	}
	
	
	protected static SKey key(int n)
	{
		return new SKey(String.valueOf(n));
	}
}
