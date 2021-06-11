// Copyright © 2021 Andy Goryachev <andy@goryachev.com>
package goryachev.secdb;
import goryachev.common.test.TF;
import goryachev.common.util.CKit;
import goryachev.common.util.SKey;
import goryachev.secdb.internal.DataHolder;
import goryachev.secdb.util.ByteArrayIStream;
import goryachev.secdb.util.Utils;


/**
 * Test Utils.
 */
public class TestUtils
{
	public static SKey key(int n)
	{
		return new SKey(String.valueOf(n));
	}
	
	
	public static DBEngine<InMemoryRef> createDB(int min, int max)
	{
		InMemoryStore store = new InMemoryStore(false);
		DBEngine<InMemoryRef> db = new DBEngine(store);

		db.execute(new DBTransaction<InMemoryRef>()
		{
			protected void body() throws Exception
			{
				for(int i=min; i<=max; i++)
				{
					SKey k = key(i);
					byte[] v = k.toString().getBytes(CKit.CHARSET_UTF8);
					
					TF.eq(containsKey(k), false);
					
					insert(k, new ByteArrayIStream(v));
					
					TF.eq(containsKey(k), true);
					
					DataHolder h = read(k);
					byte[] rv = Utils.readBytes(h.getStoredValue().getIStream(), Integer.MAX_VALUE);
					TF.eq(v, rv);
				}
			}
		});
		
		return db;
	}
}
