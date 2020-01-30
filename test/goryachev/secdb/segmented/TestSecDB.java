// Copyright © 2020 Andy Goryachev <andy@goryachev.com>
package goryachev.secdb.segmented;
import goryachev.common.test.TF;
import goryachev.common.test.Test;
import goryachev.common.util.CKit;
import goryachev.common.util.D;
import goryachev.common.util.Hex;
import goryachev.common.util.SKey;
import goryachev.secdb.IStored;
import goryachev.secdb.IStream;
import goryachev.secdb.QueryClient;
import goryachev.secdb.util.ByteArrayIStream;
import goryachev.secdb.util.Utils;
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
					insert(new SKey("0"), v(0));
					insert(new SKey("1"), v(1));
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
					insert(new SKey("1"), v(2));
					insert(new SKey("2"), v(3));
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
	
	
	protected static IStream v(int x)
	{
		byte[] b = ("value=" + x).getBytes(CKit.CHARSET_ASCII);
		return new ByteArrayIStream(b);
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
			public boolean acceptQueryResult(SKey key, IStored value)
			{
				String res = printValue(value);
				D.print(key, res);
				ct.incrementAndGet();
				return true;
			}

			protected String printValue(IStored is)
			{
				try
				{
					byte[] b = Utils.readBytes(is.getIStream(), Integer.MAX_VALUE);
					return Hex.toHexString(b);
				}
				catch(Exception e)
				{
					return CKit.stackTrace(e);
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
}
