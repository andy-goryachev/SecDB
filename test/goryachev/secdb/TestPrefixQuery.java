// Copyright © 2019-2021 Andy Goryachev <andy@goryachev.com>
package goryachev.secdb;
import goryachev.common.test.TF;
import goryachev.common.test.Test;
import goryachev.common.util.CKit;
import goryachev.common.util.CList;
import goryachev.common.util.SKey;
import goryachev.secdb.util.Utils;


/**
 * Tests prefix query in InMemoryStore.
 */
public class TestPrefixQuery
{
	public static void main(String[] args)
	{
		TF.run();
	}
	
	
	@Test
	public void test() throws Exception
	{
		t(0, 10, "", 0, 1, 10, 2, 3, 4, 5, 6, 7, 8, 9);
		t(0, 1000, 90, 90, 900, 901, 902, 903, 904, 905, 906, 907, 908, 909);
		t(0, 1000, 123, 123);
		t(0, 1000, 1234);
		t(0, 1000, "0", 0);
		t(0, 1000, "1000", 1000);
	}
	
	
	protected void t(int min, int max, Object prefix, int ... expected) throws Exception
	{
		CList<Integer> exp = new CList<>(expected.length);
		for(int v: expected)
		{
			exp.add(v);
		}
		
		DBEngine<InMemoryRef> db = TestUtils.createDB(min, max);

		SKey px = new SKey(prefix.toString());
		
		CList<Integer> result = new CList();
		
		db.prefixQuery(px, (key, dataHolder) ->
		{
			byte[] b = Utils.readBytes(dataHolder.getStoredValue().getIStream(), Integer.MAX_VALUE);
			String s = new String(b, CKit.CHARSET_UTF8);
			int v = Integer.parseInt(s);
			result.add(v);
			
			return true;
		});
		
		TF.eq(result, exp);
	}
}
