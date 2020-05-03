// Copyright © 2020 Andy Goryachev <andy@goryachev.com>
package goryachev.secdb.segmented;
import goryachev.common.test.BeforeClass;
import goryachev.common.test.TF;
import goryachev.common.test.Test;
import goryachev.common.util.CKit;
import goryachev.common.util.CList;
import goryachev.common.util.CMap;
import goryachev.common.util.FileTools;
import goryachev.common.util.SKey;
import goryachev.log.config.JsonLogConfig;
import goryachev.secdb.util.ByteArrayIStream;
import java.io.File;
import java.util.Random;


/**
 * Test Segments.
 */
public class TestSegments
{
	private static final File DIR = new File("user.home/segment-test");

	
	public static void main(String[] args)
	{
		TF.run();
	}
	
	
	@BeforeClass
	public static void initLog() throws Exception
	{
		SegmentFile.SEGMENT_SIZE = 256;
		JsonLogConfig.configure(CKit.readStringQuiet(TestLarge.class, "log-conf.json"));
		
		FileTools.deleteRecursively(DIR);
	}
	
	
	@Test
	public void test() throws Exception
	{
		int txCount = 10;
		
		CMap<String,String> d = new CMap();
		
		SecDB db;
		try
		{
			db = SecDB.open(DIR, null);
		}
		catch(SecException e)
		{
			switch(e.getErrorCode())
			{
			case DIR_NOT_FOUND:
				SecDB.create(DIR, null, null);
				db = SecDB.open(DIR, null);
				break;
			default:
				throw e;
			}
		}
		
		for(int i=0; i<txCount; i++)
		{
			db.execute(new Transaction()
			{
				protected void body() throws Exception
				{
					txBody(this, d);
				}

				protected void onError(Throwable e)
				{
					TF.fail(e);
				}
			});
		}
		
		db.close();
	}
	
	
	protected void txBody(Transaction tx, CMap<String,String> d) throws Exception
	{
		int count = 10;
		Random r = new Random();

		for(int j=0; j<count; j++)
		{
			String k = String.valueOf(1 + r.nextInt(100));
			boolean delete = r.nextInt(100) > 80;
			if(delete)
			{
				CList<String> keys = d.keys();
				if(keys.size() > 0)
				{
					int ix = r.nextInt(keys.size());
					String del = keys.get(ix);
					d.remove(k);
					tx.remove(new SKey(k));
				}
			}
			else
			{
				String v = String.valueOf(1 + r.nextInt(100));
				d.put(k, k);
				tx.insert(new SKey(k), new ByteArrayIStream(v.getBytes(CKit.CHARSET_UTF8)));
			}
		}
	}
}
