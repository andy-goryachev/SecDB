// Copyright © 2020 Andy Goryachev <andy@goryachev.com>
package goryachev.secdb.segmented;
import goryachev.common.test.TF;
import goryachev.common.test.Test;
import goryachev.common.util.D;
import goryachev.common.util.SKey;
import goryachev.secdb.IStored;
import goryachev.secdb.QueryClient;
import goryachev.secdb.util.ByteArrayIStream;
import java.io.File;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;


/**
 * Test SecDB.
 */
public class TestSecDB
{
	private static final File DIR = new File("user.home/db-test");
	
	
	public static void main(String[] args)
	{
		TF.run();
	}
	
	
//	@Test
	public void testCreate() throws Exception
	{
		SecDB.create(DIR, null);
	}
	
	
	@Test
	public void testOpen() throws Exception
	{
		SecDB db = SecDB.open(DIR, null);
		
		db.execute(new Transaction()
		{
			protected void body() throws Exception
			{
				insert(new SKey("0"), new ByteArrayIStream("0".getBytes()));
				insert(new SKey("0"), new ByteArrayIStream("1".getBytes()));
			}
		});
		
		int ct = query(db, "0", "9");
		TF.eq(ct, 2);
		
		db.close();
	}
	
	
	protected int query(SecDB db, String start, String end) throws Exception
	{
		AtomicInteger ct = new AtomicInteger();
		AtomicReference<Throwable> error = new AtomicReference();
		
		db.query(new SKey(start), new  SKey(end), new QueryClient<SKey,IStored>()
		{
			public boolean acceptQueryResult(SKey key, IStored value)
			{
				D.print(key, value);
				ct.incrementAndGet();
				return true;
			}

			public void onError(Throwable err)
			{
				err.printStackTrace();
				error.set(err);
			}
		});
		
		if(error.get() != null)
		{
			throw new Exception(error.get());
		}
		
		return ct.get();
	}
}
