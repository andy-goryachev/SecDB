// Copyright © 2020 Andy Goryachev <andy@goryachev.com>
package goryachev.secdb.segmented;
import goryachev.common.test.TF;
import goryachev.common.test.Test;
import goryachev.common.util.D;
import goryachev.common.util.Parsers;
import goryachev.common.util.SKey;
import goryachev.secdb.IStored;
import goryachev.secdb.IStream;
import goryachev.secdb.QueryClient;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.InputStream;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;


/**
 * Test SecDB with large objects.
 */
public class TestLarge
{
	private static final File DIR = new File("H:/Test/SecDB/large-test");
	private static final long SIZE = 1_000_000_000L;
	
	
	public static void main(String[] args)
	{
		TF.run();
	}
	
	
//	@Test
	public void testCreate() throws Exception
	{
		SecDB.create(DIR, null);
	}
	
	
//	@Test
	public void testQuery() throws Exception
	{
		SecDB db = SecDB.open(DIR, null);
		int ct = query(db, "0", "9");
		D.print(ct);
	}
	
	
	@Test
	public void testOpen() throws Exception
	{
		SecDB db;
		try
		{
			db = SecDB.open(DIR, null);
		}
		catch(DBException e)
		{
			switch(e.getErrorCode())
			{
			case DIR_NOT_FOUND:
				SecDB.create(DIR, null);
				db = SecDB.open(DIR, null);
				break;
			default:
				throw e;
			}
		}
		
		AtomicReference<Throwable> error = new AtomicReference();
		
		try
		{
			db.execute(new Transaction()
			{
				protected void body() throws Exception
				{
					insert(new SKey("0"), createStream(0));
					insert(new SKey("1"), createStream(99));
				}

				protected void onError(Throwable e)
				{
					error.set(e);
				}
			});
			
			check(error);
			
			db.execute(new Transaction()
			{
				protected void body() throws Exception
				{
					insert(new SKey("1"), createStream(1));
					insert(new SKey("2"), createStream(2));
				}
				
				protected void onError(Throwable e)
				{
					error.set(e);
				}
			});
			
			check(error);
			
			int ct = query(db, "0", "9");
			TF.eq(ct, 3);
		}
		finally
		{
			db.close();
		}
	}
	
	
	protected static IStream createStream(int seed)
	{
		//return new LargePseudoRandomStream(0xaa550000 | seed, SIZE);
		return new TestStream(SIZE);
	}
	
	
	protected void check(AtomicReference<Throwable> error)
	{
		Throwable e = error.get();
		if(e != null)
		{
			throw new Error("transaction failed", e);
		}
	}
	
	
	protected int query(SecDB db, String start, String end) throws Exception
	{
		AtomicInteger ct = new AtomicInteger();
		AtomicReference<Throwable> error = new AtomicReference();
		
		db.query(new SKey(start), new  SKey(end), new QueryClient<SKey,IStored>()
		{
			public boolean acceptQueryResult(SKey key, IStored st)
			{
				try
				{
					D.print(key, st.getLength());
					check(key.toString(), st.getIStream());
					ct.incrementAndGet();
					return true;
				}
				catch(Exception e)
				{
					onError(e);
					return false;
				}
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
	
	
	protected void check(String key, IStream is) throws Exception
	{
		int seed = Parsers.parseInteger(key);
		InputStream in1 = new BufferedInputStream(createStream(seed).getStream());
		InputStream in2 = new BufferedInputStream(is.getStream());

		long off = 0;
		
		for(;;)
		{
			int c1 = in1.read();
			int c2 = in2.read();
			
			if(c1 != c2)
			{
				throw new Exception(String.format("mismatch at offset %08x, %d, %d, key=%s", off, c1, c2, key));
			}
			
			if(c1 < 0)
			{
				return;
			}
			
			off++;
		}
	}
}
