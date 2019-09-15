// Copyright © 2019 Andy Goryachev <andy@goryachev.com>
package research.secdb;
import goryachev.common.test.TF;
import goryachev.common.test.Test;
import goryachev.common.util.CKit;


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
		IStore store = new InMemoryStore();
		SecDB db = new SecDB(store);
		try
		{
			db.execute(new Transaction()
			{
				protected void body() throws Exception
				{
					// TODO
				}
			});
		}
		finally
		{
			CKit.close(db);
		}
	}
}
