// Copyright © 2020-2021 Andy Goryachev <andy@goryachev.com>
package goryachev.secdb.segmented;
import goryachev.common.log.Log;
import goryachev.common.log.LogLevel;
import goryachev.common.test.TF;
import goryachev.common.test.Test;
import goryachev.common.util.CSet;
import goryachev.common.util.D;
import goryachev.common.util.FileTools;
import goryachev.common.util.SKey;
import goryachev.secdb.IStored;
import goryachev.secdb.IStream;
import goryachev.secdb.segmented.clear.ClearEncHelper;
import java.io.File;
import java.util.Random;


/**
 * Test SecDB - Insert After Remove All.
 */
public class TestInsertAfterRemoveAll
{
	private static final File DIR = new File("user.home/db-test");
	
	
	public static void main(String[] args)
	{
		Log.initAll(LogLevel.DEBUG);
		TF.run();
	}
	
	
	@Test
	public void testRemoveAll() throws Exception
	{
		FileTools.deleteRecursively(DIR);
		
		ClearEncHelper h = new ClearEncHelper();

		SecDB.create(DIR, h);

		SecDB db;
		try
		{
			db = SecDB.open(DIR, h);
		}
		catch(SecException e)
		{
			switch(e.getErrorCode())
			{
			case DIR_NOT_FOUND:
				SecDB.create(DIR, h);
				db = SecDB.open(DIR, h);
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
			
			// except one key
			keys.remove("0");
			
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
			
			D.print("remaining", keys.size());
			
			db.execute(new Transaction()
			{
				protected void body() throws Exception
				{
					remove(new SKey("0"));
					result.remove("0");
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
			
			// loadRoot loads internal node
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
