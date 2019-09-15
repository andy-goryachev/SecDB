// Copyright © 2019 Andy Goryachev <andy@goryachev.com>
package research.secdb;
import goryachev.common.test.TF;
import goryachev.common.test.Test;
import goryachev.common.util.CKit;
import goryachev.common.util.D;
import goryachev.common.util.SKey;


/**
 * Tests SecDB.
 */
public class TestSecDB
{
	public static void main(String[] args)
	{
		TF.run();
	}
	
	
	@Test
	public void test()
	{
		IStore<Ref> store = new InMemoryStore();
		SecDB db = new SecDB(store);
		try
		{
			db.execute(new Transaction()
			{
				protected void body() throws Exception
				{
					SKey k = new SKey("1");
					byte[] v = new byte[] { (byte)1, (byte)2 };
					
					D.print("contains:", containsKey(k), "expecting false");
					
					insert(k, new ByteArrayIStream(v));
					
					D.print("contains:", containsKey(k), "expecting true");
					
					DataHolder h = read(k);
					byte[] rv = h.getIStream().readBytes(Integer.MAX_VALUE);
					D.print("success:", CKit.equals(v, rv));
				}
			});
		}
		finally
		{
			CKit.close(db);
		}
	}
}
