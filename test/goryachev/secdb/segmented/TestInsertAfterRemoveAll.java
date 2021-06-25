// Copyright © 2020-2021 Andy Goryachev <andy@goryachev.com>
package goryachev.secdb.segmented;
import goryachev.common.log.Log;
import goryachev.common.test.TF;
import goryachev.common.test.Test;
import goryachev.common.util.CSet;
import goryachev.common.util.D;
import goryachev.common.util.FileTools;
import goryachev.common.util.SKey;
import goryachev.crypto.OpaqueBytes;
import goryachev.crypto.OpaqueChars;
import goryachev.crypto.xsalsa20poly1305.XSalsaTools;
import goryachev.secdb.IStored;
import goryachev.secdb.IStream;
import goryachev.secdb.segmented.clear.ClearEncHelper;
import goryachev.secdb.segmented.eax.EAXEncHelper;
import goryachev.secdb.segmented.xsalsa20poly1305.XSalsa20Poly1305EncHelper;
import java.io.File;
import java.security.SecureRandom;
import java.util.Random;


/**
 * Test SecDB - Insert After Remove All.
 */
public class TestInsertAfterRemoveAll
{
	private static final File DIR = new File("user.home/db-test");
	
	
	public static void main(String[] args)
	{
//		Log.initForDebug();
		TF.run();
	}
	
	
	@Test
	public void testRemoveAll() throws Exception
	{
		FileTools.deleteRecursively(DIR);

		SecDB.create(DIR, null, null, null);

		SecDB db;
		try
		{
			db = SecDB.open(DIR, null, null);
		}
		catch(SecException e)
		{
			switch(e.getErrorCode())
			{
			case DIR_NOT_FOUND:
				SecDB.create(DIR, null, null, null);
				db = SecDB.open(DIR, null, null);
				break;
			default:
				throw e;
			}
		}
		
		Random random = new Random();
		long seed = random.nextLong();
		random.setSeed(seed);
		TF.print("seed:", seed);
		
		CSet<String> keys = new CSet();
		CSet<String> result = new CSet();
		
		try
		{
			for(int i=0; i<100; i++)
			{
				db.execute(new Transaction()
				{
					protected void body() throws Exception
					{
						for(int j=0; j<100; j++)
						{
							String k = String.valueOf(random.nextInt(1_000));
							keys.add(k);
							insert(new SKey(k), IStream.of(k));
						}
					}
				});
			}
			
			db.prefixQuery(new SKey(""), (SKey k, IStored s) ->
			{
				result.add(k.toString());
				return true;
			});
			
			TF.eq(keys, result);
			
			D.print("inserted", keys.size());
			
			db.execute(new Transaction()
			{
				protected void body() throws Exception
				{
					for(String k: keys)
					{
						remove(new SKey(k));
						result.remove(k);
					}
				}
			});
			
			CSet<String> result2 = new CSet();
			
			db.prefixQuery(new SKey(""), (SKey k, IStored s) ->
			{
				result2.add(k.toString());
				return true;
			});
			
			TF.eq(result, result2);
			
			D.print("after removal", result2.size());
			
			// and finally, the bug
			
			db.execute(new Transaction()
			{
				protected void body() throws Exception
				{
					for(String k: keys)
					{
						insert(new SKey("bug"), IStream.of("yo"));
					}
				}
			});
		}
		finally
		{
			db.close();
		}
	}
}
