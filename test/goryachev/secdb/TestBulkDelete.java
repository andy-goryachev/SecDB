// Copyright © 2019-2020 Andy Goryachev <andy@goryachev.com>
package goryachev.secdb;
import goryachev.common.test.TF;
import goryachev.common.test.Test;
import goryachev.common.util.CKit;
import goryachev.common.util.D;
import goryachev.common.util.SKey;
import goryachev.secdb.util.ByteArrayIStream;


/**
 * Tests bulk delete using InMemoryStore.
 */
public class TestBulkDelete
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
		
		add(db, 0, 100);
		add(db, 100, 200);
		add(db, 200, 300);
		
		delete(db, 100, 200);
		
		db.dumpKeys(System.err, " ");
	}
	
	
	protected void add(DBEngine<InMemoryRef> db, int start, int end)
	{
		db.execute(new DBTransaction<InMemoryRef>()
		{
			protected void body() throws Exception
			{
				for(int i=start; i<end; i++)
				{
					SKey k = key(i);
					byte[] v = ("v." + k).getBytes(CKit.CHARSET_UTF8);
					
					insert(k, new ByteArrayIStream(v));
				}
			}
		});
	}
	
	
	protected void delete(DBEngine<InMemoryRef> db, int start, int end)
	{
		db.execute(new DBTransaction<InMemoryRef>()
		{
			protected void body() throws Exception
			{
				for(int i=start; i<end; i++)
				{
					SKey k = key(i);
					
					remove(k);
				}
			}
		});
	}
	
	
	protected static SKey key(int n)
	{
		return new SKey(String.format("%d", n));
	}
}
