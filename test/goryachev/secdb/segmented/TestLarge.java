// Copyright © 2020 Andy Goryachev <andy@goryachev.com>
package goryachev.secdb.segmented;
import goryachev.common.log.Log;
import goryachev.common.test.BeforeClass;
import goryachev.common.test.TF;
import goryachev.common.test.Test;
import goryachev.common.util.CKit;
import goryachev.common.util.D;
import goryachev.common.util.FileTools;
import goryachev.common.util.Hex;
import goryachev.common.util.Parsers;
import goryachev.common.util.SKey;
import goryachev.secdb.IStored;
import goryachev.secdb.IStream;
import goryachev.secdb.QueryClient;
import goryachev.secdb.util.Utils;
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
	private static final File DIR = new File("user.home/large-test");
	private static final long SIZE = 1024; // 1_000_000_000L;
	
	
	public static void main(String[] args)
	{
		TF.run();
	}
	
	
	@BeforeClass
	public static void initLog() throws Exception
	{
		Log.configure(CKit.readStringQuiet(TestLarge.class, "log-conf.json"));
		
		FileTools.deleteRecursively(DIR);
	}
	
	
	protected static IStream createStream(int seed)
	{
//		return new LargePseudoRandomStream(seed, SIZE);
//		return new TestStream(SIZE);
		return new TestStream2(seed, SIZE);
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
	
	
//	@Test
	public void testPseudoRandom() throws Exception
	{
		for(int i=0; i<100; i++)
		{
			compare("test", createStream(i).getStream(), createStream(i).getStream());
		}
	}
	
	
	@Test
	public void testTransactions() throws Exception
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
					insert(new SKey("1"), createStream(1));
				}

				protected void onError(Throwable e)
				{
					error.set(e);
				}
			});
			
			dump(db);
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
			
			dump(db);
			check(error);
			
			int ct = query(db, "0", "9");
			TF.eq(ct, 3);
		}
		finally
		{
			db.close();
		}
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
		InputStream is1 = createStream(seed).getStream();
		InputStream is2 = is.getStream();
		compare(key, is1, is2);
	}
	
	
	protected static InputStream stream(InputStream in)
	{
		return new BufferedInputStream(in);
//		return in;
	}
	
	
	protected static void compare(String key, InputStream is1, InputStream is2) throws Exception
	{
		InputStream in1 = stream(is1);
		try
		{
			InputStream in2 = stream(is2);
			try
			{
				long off = 0;
				
				for(;;)
				{
					if(off == 1024)
					{
						D.p(); // FIX
					}
					
					int c1 = in1.read();
					int c2 = in2.read();
					
					if(c1 != c2)
					{
						// FIX
						throw new Exception(String.format("mismatch at offset 0x%08x, %02x, %02x, key=%s", off, c1, c2, key));
//						return;
					}
					
					if(c1 < 0)
					{
						return;
					}
					
					off++;
				}
			}
			finally
			{
				CKit.close(in2);
			}
		}
		finally
		{
			CKit.close(in1);
		}
	}
	
	
	protected void dump(SecDB db)
	{
		String start = "0";
		String end = "999";
		db.query(new SKey(start), new  SKey(end), new QueryClient<SKey,IStored>()
		{
			public boolean acceptQueryResult(SKey key, IStored st)
			{
				try
				{
					byte[] b = Utils.readBytes(st.getIStream(), Integer.MAX_VALUE);
					D.print(key, "\n" + Hex.toHexStringASCII(b));
					return true;
				}
				catch(Throwable e)
				{
					onError(e);
					return false;
				}
			}

			public void onError(Throwable err)
			{
				err.printStackTrace();
			}
		});
	}
}
