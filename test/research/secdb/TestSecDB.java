// Copyright © 2019 Andy Goryachev <andy@goryachev.com>
package research.secdb;
import goryachev.common.test.TF;
import goryachev.common.test.Test;
import goryachev.common.util.CKit;
import goryachev.common.util.D;
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
				int index = i;
				
				D.print(i);

				db.execute(new Transaction()
				{
					protected void body() throws Exception
					{
						SKey k = new SKey(String.valueOf(index));
						byte[] v = new byte[] { (byte)index };
						
						D.print("contains:", containsKey(k), "expecting false");
						
						insert(k, new ByteArrayIStream(v));
						
						D.print("contains:", containsKey(k), "expecting true");
						
						DataHolder h = read(k);
						byte[] rv = h.getIStream().readBytes(Integer.MAX_VALUE);
						D.print("success:", CKit.equals(v, rv));
					}
				});
				
				D.print(store.dump());
			}
						
			// query
			SKey start = new SKey("0");
			SKey end = new SKey("1000");
			
			D.print("query", start, end);
			
			db.query(start, true, end, true, new QueryClient<SKey,DataHolder>()
			{
				public void onError(Throwable err)
				{
					err.printStackTrace();
				}
				
				
				public boolean acceptQueryResult(SKey key, DataHolder value)
				{
					D.print(key);
					return true;
				}
			});
		}
		finally
		{
			CKit.close(db);
		}
	}
}
