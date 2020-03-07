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
	private static final long SIZE = 999; // 1_000_000_000L;
	
	
	public static void main(String[] args)
	{
		TF.run();
	}
	
	
	@BeforeClass
	public static void initLog() throws Exception
	{
		Log.configure(CKit.readStringQuiet(TestLarge.class, "log-conf.json"));
		
		SegmentFile.SEGMENT_SIZE = 256;
		FileTools.deleteRecursively(DIR);
	}
	
	
	public static IStream createStream(int seed)
	{
//		return new LargePseudoRandomStream(seed, SIZE);
		return new LargePseudoRandomStream2(seed, SIZE);
//		return new TestStream(SIZE);
//		return new TestStream2(seed, SIZE);
//		return new TestStream3(seed, SIZE);
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
		TF.print(ct);
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
					byte[] b = readBytes(st.getIStream(), 16);
					D.print("query:", key, st.getLength(), Hex.toHexStringASCII(b));
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
	
	
	protected void check(String key, IStream in) throws Exception
	{
		int seed = Parsers.parseInteger(key);
		InputStream read = in.getStream();
		InputStream expected = createStream(seed).getStream();
		compare(key, read, expected);
	}
	
	
	protected static InputStream stream(InputStream in)
	{
//		return new BufferedInputStream(in);
		return in;
	}
	
	
	protected static void compare(String key, InputStream readInput, InputStream expectedInput) throws Exception
	{
		InputStream ri = stream(readInput);
		try
		{
			InputStream ei = stream(expectedInput);
			try
			{
				long off = 0;
				
				for(;;)
				{
					int cr = ri.read();
					int ce = ei.read();
					
					if(cr != ce)
					{
						// FIX
						throw new Exception(String.format("mismatch at offset 0x%08x, rd=%02x, exp=%02x, key=%s", off, cr, ce, key));
//						return;
					}
					
					if(cr < 0)
					{
						return;
					}
					
					off++;
				}
			}
			finally
			{
				CKit.close(ei);
			}
		}
		finally
		{
			CKit.close(ri);
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
					byte[] b = readBytes(st.getIStream(), 2048);
					TF.print(key, "\n" + Hex.toHexStringASCII(b));
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
	
	
	/** read data into a new byte array, as long as the object size is below the limit */
	public static byte[] readBytes(IStream inp, int limit) throws Exception
	{
		long len = Math.min(limit, inp.getLength());
		byte[] b = new byte[(int)len];
		InputStream is = inp.getStream();
		CKit.readFully(is, b);
		return b;
	}
}
