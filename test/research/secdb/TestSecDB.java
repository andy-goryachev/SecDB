// Copyright © 2019 Andy Goryachev <andy@goryachev.com>
package research.secdb;
import goryachev.common.test.TF;
import goryachev.common.test.Test;
import goryachev.common.util.CKit;
import goryachev.common.util.D;
import goryachev.common.util.Hex;
import goryachev.common.util.SKey;
import research.bplustree.QueryClient;


/**
 * Tests SecDB.
 */
public class TestSecDB
{
	public static void main(String[] args)
	{
		TF.run();
	}
	
	
	@Test
	public void test() throws Exception
	{
		InMemoryStore store = new InMemoryStore();
		SecDB db = new SecDB(store);
		try
		{
			for(int i=0; i<10; i++)
			{
				SKey k = key(i);
				D.print(k);
				
				if(i >= 2)
				{
					D.print();
				}

				db.execute(new Transaction()
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
				
				db.query(start, true, end, true, new QueryClient<SKey,DataHolder>()
				{
					public void onError(Throwable err)
					{
						err.printStackTrace();
					}
					
					
					public boolean acceptQueryResult(SKey key, DataHolder value)
					{
						D.print("    query:", key, Hex.toHexString(value.getBytes()));
						return true;
					}
				});
			}
		}
		finally
		{
			CKit.close(db);
		}
	}
	
	
	protected static SKey key(int n)
	{
		return new SKey(String.format("%04d", n));
	}
}
