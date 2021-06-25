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


/**
 * Tests InMemoryStore.
 */
public class TestInMemoryStore
{
	public static void main(String[] args)
	{
		TF.run();
	}
	
	
	@Test
	public void test() throws Exception
	{
		InMemoryStore store = new InMemoryStore(true);
		DBEngine<InMemoryRef> db = new DBEngine(store);

		for(int i=0; i<10; i++)
		{
			SKey k = key(i);
			D.print(k);
			
			if(i >= 2)
			{
				D.print();
			}

			db.execute(new DBTransaction<InMemoryRef>()
			{
				protected void body() throws Exception
				{
					byte[] v = ("v." + k).getBytes(CKit.CHARSET_UTF8);
					
					D.print("contains:", containsKey(k), "expecting false");
					
					insert(k, new ByteArrayIStream(v));
					
					D.print("contains:", containsKey(k), "expecting true");
					
					DataHolder h = read(k);
					byte[] rv = h.getStoredValue().getIStream().readBytes(Integer.MAX_VALUE);
					D.print("success:", CKit.equals(v, rv));
				}
			});
			
			D.print("store dump:", store.dump());
					
			// query
			SKey start = key(0);
			SKey end = key(1000);
			
			D.print("query", start, end);
			
			db.rangeQuery(start, true, end, true, (SKey key, DataHolder<InMemoryRef> value) ->
			{
				byte[] b = value.getStoredValue().getIStream().readBytes(Integer.MAX_VALUE);
				D.print("    query:", key, Hex.toHexString(b));
				return true;
			});
		}
	}
	
	
	protected static SKey key(int n)
	{
		return new SKey(String.format("%04d", n));
	}
}
