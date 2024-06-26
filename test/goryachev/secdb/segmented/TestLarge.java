// Copyright © 2020-2024 Andy Goryachev <andy@goryachev.com>
package goryachev.secdb.segmented;
import goryachev.common.test.BeforeClass;
import goryachev.common.test.TF;
import goryachev.common.test.Test;
import goryachev.common.util.CKit;
import goryachev.common.util.CPlatform;
import goryachev.common.util.D;
import goryachev.common.util.FileTools;
import goryachev.common.util.Hex;
import goryachev.common.util.Parsers;
import goryachev.common.util.SKey;
import goryachev.log.config.JsonLogConfig;
import goryachev.secdb.IStored;
import goryachev.secdb.IStream;
import goryachev.secdb.QueryClient;
import goryachev.secdb.segmented.clear.ClearEncHelper;
import java.io.File;
import java.io.InputStream;
import java.util.concurrent.atomic.AtomicInteger;


/**
 * Test SecDB with large objects.
 */
public class TestLarge
{
	private static final long SIZE = 1_000_000_000L;
	
	
	public static void main(String[] args)
	{
		TF.run();
	}
	
	
	public static File dir()
	{
		if(CPlatform.isWindows())
		{
			return new File("H:/Test/SecDB/large-test");
		}
		else
		{
			return new File("user.home/large-test");
		}
	}
	
	
	@BeforeClass
	public static void initLog() throws Exception
	{
		JsonLogConfig.configure(CKit.readStringQuiet(TestLarge.class, "log-conf.json"));
		
//		SegmentFile.SEGMENT_SIZE = 256;
		FileTools.deleteRecursively(dir());
	}
	
	
	public static IStream createStream(int seed)
	{
		// bad implementation
//		return new LargePseudoRandomStream(seed, SIZE);
		
		// not enough info
//		return new IncrementingByteStream(SIZE);
		
		return new TOffsetIStream(SIZE);
	}
	
	
//	@Test
	public void testCreate() throws Exception
	{
		SecDB.create(dir(), new ClearEncHelper());
	}
	
	
//	@Test
	public void testQuery() throws Exception
	{
		SecDB db = SecDB.open(dir(), new ClearEncHelper());
		int ct = query(db, "0", "9");
		TF.print(ct);
	}
	
	
	@Test
	public void testTransactions() throws Exception
	{
		ClearEncHelper h = new ClearEncHelper();
		SecDB db;
		try
		{
			db = SecDB.open(dir(),  h);
		}
		catch(SecException e)
		{
			switch(e.getErrorCode())
			{
			case DIR_NOT_FOUND:
				SecDB.create(dir(), h);
				db = SecDB.open(dir(), h);
				break;
			default:
				throw e;
			}
		}
		
		try
		{
			db.execute(new Transaction()
			{
				protected void body() throws Exception
				{
					insert(new SKey("0"), createStream(0));
					insert(new SKey("1"), createStream(1));
				}
			});
			
			dump(db);
			
			db.execute(new Transaction()
			{
				protected void body() throws Exception
				{
					insert(new SKey("1"), createStream(1));
					insert(new SKey("2"), createStream(2));
				}
			});
			
			dump(db);
			
			int ct = query(db, "0", "9");
			TF.eq(ct, 3);
		}
		finally
		{
			db.close();
		}
	}
	
	
	protected int query(SecDB db, String start, String end) throws Exception
	{
		AtomicInteger ct = new AtomicInteger();
		
		db.rangeQuery(new SKey(start), true, new  SKey(end), false, new QueryClient<SKey,IStored>()
		{
			public boolean acceptQueryResult(SKey key, IStored st) throws Exception
			{
				byte[] b = readBytes(st.getIStream(), 16);
				D.print("query:", key, st.getLength(), Hex.toHexStringASCII(b));
				check(key.toString(), st.getIStream());
				ct.incrementAndGet();
				return true;
			}
		});
		
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
				long off = 0; // TODO remove
				
				for(;;)
				{
					int cr = ri.read();
					int ce = ei.read();
					
					if(cr != ce)
					{
						throw new Exception(String.format("mismatch at offset %d, rd=%02x, exp=%02x, key=%s", off, cr, ce, key));
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
	
	
	protected void dump(SecDB db) throws Exception
	{
		String start = "0";
		String end = "999";
		
		db.rangeQuery(new SKey(start), true, new  SKey(end), false, new QueryClient<SKey,IStored>()
		{
			public boolean acceptQueryResult(SKey key, IStored st) throws Exception
			{
				byte[] b = readBytes(st.getIStream(), 2048);
				TF.print(key, "\n" + Hex.toHexStringASCII(b));
				return true;
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
