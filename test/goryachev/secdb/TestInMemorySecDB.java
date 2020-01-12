// Copyright © 2019-2020 Andy Goryachev <andy@goryachev.com>
package goryachev.secdb;
import goryachev.common.test.TF;
import goryachev.common.test.Test;
import goryachev.common.util.CKit;
import goryachev.common.util.D;
import goryachev.common.util.Hex;
import goryachev.common.util.SKey;
import goryachev.secdb.bplustree.QueryClient;
import goryachev.secdb.internal.DataHolder;
import goryachev.secdb.util.ByteArrayIStream;


/**
 * Tests In Memory SecDB.
 */
public class TestInMemorySecDB
{
	public static void main(String[] args)
	{
		TF.run();
	}
	
	
	@Test
	public void test() throws Exception
	{
		InMemoryStore store = new InMemoryStore();
		SecDB<InMemoryRef> db = new SecDB(store);

		for(int i=0; i<10; i++)
		{
			SKey k = key(i);
			D.print(k);
			
			if(i >= 2)
			{
				D.print();
			}

			db.execute(new Transaction<InMemoryRef>()
			{
				protected void body() throws Exception
				{
					byte[] v = ("v." + k).getBytes(CKit.CHARSET_UTF8);
					
					D.print("contains:", containsKey(k), "expecting false");
					
					insert(k, new ByteArrayIStream(v));
					
					D.print("contains:", containsKey(k), "expecting true");
					
					DataHolder h = read(k);
					byte[] rv = h.getIStream().readBytes(Integer.MAX_VALUE);
					D.print("success:", CKit.equals(v, rv));
				}
			});
			
			D.print("store dump:", store.dump());
					
			// query
			SKey start = key(0);
			SKey end = key(1000);
			
			D.print("query", start, end);
			
			db.query(start, true, end, true, new QueryClient<SKey,DataHolder<InMemoryRef>>()
			{
				public void onError(Throwable err)
				{
					err.printStackTrace();
				}
				
				
				public boolean acceptQueryResult(SKey key, DataHolder<InMemoryRef> value)
				{
					try
					{
						byte[] b = value.getIStream().readBytes(Integer.MAX_VALUE);
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
	}
	
	
	protected static SKey key(int n)
	{
		return new SKey(String.format("%04d", n));
	}
}
