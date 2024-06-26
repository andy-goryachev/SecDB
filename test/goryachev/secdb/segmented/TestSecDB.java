// Copyright © 2020-2024 Andy Goryachev <andy@goryachev.com>
package goryachev.secdb.segmented;
import goryachev.common.test.TF;
import goryachev.common.test.Test;
import goryachev.common.util.CSet;
import goryachev.common.util.D;
import goryachev.common.util.FileTools;
import goryachev.common.util.SKey;
import goryachev.memsafecrypto.CByteArray;
import goryachev.memsafecrypto.OpaqueBytes;
import goryachev.secdb.IStored;
import goryachev.secdb.IStream;
import goryachev.secdb.segmented.clear.ClearEncHelper;
import goryachev.secdb.segmented.xsalsa.XSalsaEncHelper;
import java.io.File;
import java.security.SecureRandom;
import java.util.Random;


/**
 * Test SecDB.
 */
public class TestSecDB
{
	private static final File DIR = new File("user.home/db-test");
	
	
	public static void main(String[] args)
	{
//		Log.initForDebug();
		TF.run();
	}
	
	
	@Test
	public void test() throws Exception
	{
		FileTools.deleteRecursively(DIR);

		CByteArray keyBytes = TUtils.generateKey();
		OpaqueBytes key = new OpaqueBytes(keyBytes);
		
		IEncHelper[] hs =
		{
			new ClearEncHelper(),
			new XSalsaEncHelper(new SecureRandom(), key)
		};
		
		for(IEncHelper h: hs)
		{
			test(new File(DIR, getName(h)), h);
		}
	}
	
	
	private String getName(IEncHelper h)
	{
		if(h instanceof ClearEncHelper)
		{
			return "clear";
		}
		else if(h instanceof XSalsaEncHelper)
		{
			return "xsalsa";
		}
		else
		{
			throw new Error("?" + h);
		}
	}
	
	
	public void test(File dir, IEncHelper helper) throws Exception
	{
		SecDB.create(dir, helper);

		SecDB db;
		try
		{
			db = SecDB.open(dir, helper);
		}
		catch(SecException e)
		{
			switch(e.getErrorCode())
			{
			case DIR_NOT_FOUND:
				SecDB.create(dir, helper);
				db = SecDB.open(dir, helper);
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
			
			for(int i=0; i<10; i++)
			{
				db.execute(new Transaction()
				{
					protected void body() throws Exception
					{
						for(String k: keys)
						{
							boolean remove = random.nextFloat() > 0.9f;
							if(remove)
							{
								remove(new SKey(k));
								result.remove(k);
							}
						}
					}
				});
			}
			
			CSet<String> result2 = new CSet();
			
			db.prefixQuery(new SKey(""), (SKey k, IStored s) ->
			{
				result2.add(k.toString());
				return true;
			});
			
			TF.eq(result, result2);
			
			D.print("after removal", result2.size());
		}
		finally
		{
			db.close();
		}
	}
}
