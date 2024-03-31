// Copyright © 2019-2024 Andy Goryachev <andy@goryachev.com>
package goryachev.secdb;
import goryachev.common.test.TF;
import goryachev.common.test.Test;
import goryachev.common.util.CKit;
import goryachev.common.util.CList;
import goryachev.common.util.SKey;


/**
 * Tests range query using InMemoryStore.
 */
public class TestRangeQuery
{
	public static void main(String[] args)
	{
		TF.run();
	}
	
	
	@Test
	public void test() throws Exception
	{
		t(0, 20, "1", true, "9", true, 1, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 2, 20, 3, 4, 5, 6, 7, 8, 9);
		t(0, 20, "1", true, "15", false, 1, 10, 11, 12, 13, 14);
		t(0, 20, "15", false, "1", true, 14, 13, 12, 11, 10, 1); 
		t(0, 1000, "", true, "1", true, 0, 1);
	}
	
	
	protected void t(int min, int max, Object start, boolean includeStart, Object end, boolean includeEnd, int ... expected) throws Exception
	{
		CList<Integer> exp = TestUtils.asList(expected);
		DBEngine<InMemoryRef> db = TestUtils.createDB(min, max);

		SKey sk = new SKey(start.toString());
		SKey ek = new SKey(end.toString());
		
		CList<Integer> result = new CList();
		
		db.rangeQuery(sk, includeStart, ek, includeEnd, (key, dataHolder) ->
		{
			byte[] b = dataHolder.getStoredValue().getIStream().readBytes(Integer.MAX_VALUE);
			String s = new String(b, CKit.CHARSET_UTF8);
			int v = Integer.parseInt(s);
			result.add(v);
			
			return true;
		});
		
		TF.eq(result, exp);
	}
}
