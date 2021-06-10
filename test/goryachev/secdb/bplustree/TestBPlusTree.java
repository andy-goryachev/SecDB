// Copyright © 2018-2021 Andy Goryachev <andy@goryachev.com>
package goryachev.secdb.bplustree;
import goryachev.common.test.TF;
import goryachev.common.test.Test;
import goryachev.common.util.CKit;
import goryachev.secdb.QueryClient;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;


public class TestBPlusTree
{
	public static void main(String[] args)
	{
		TF.run();
	}
	
	
	@Test
	public void testBulkDelete() throws Exception
	{
		BPlusTree<String,String> t = new BPlusTree<String,String>(4);
		
		t.insert("1", "1");
		t.insert("2", "2");
		t.insert("3", "3");
		t.insert("4", "4");
		TF.print(t.dumpKeys());
		
		t.remove("3"); // FIX breaks the tree
		TF.print(t.dumpKeys());
		
		t.remove("4");
		TF.print(t.dumpKeys());
	}
	
	
	@Test
	public void testBulkDelete2() throws Exception
	{
		BPlusTree<String,String> t = new BPlusTree<String,String>(4);
		
		t.insert(".files", "1");
		t.insert("D5889315212F71B0751DF9829227A5D9E0EEC08D3CFE177DFC14DC9031F309CC", "2");
		t.insert("query.000001737D81E92F.54CD92E2FBBF7A4F45B457E9B5C911FEC969022D0EC59335283895D91FC2CE59", "3");
		t.insert("query.000001737D82010D.47EE060C790DCD3F3581FC74933822CF829C0D33EAF531C64F81F8CD27651945", "4");
		
		t.remove("query.000001737D81E92F.54CD92E2FBBF7A4F45B457E9B5C911FEC969022D0EC59335283895D91FC2CE59");
		TF.print(t.dumpKeys());
		
		t.remove("query.000001737D82010D.47EE060C790DCD3F3581FC74933822CF829C0D33EAF531C64F81F8CD27651945");
		TF.print(t.dumpKeys());
	}
	
	
	@Test
	public void testLoad() throws Exception
	{
		BPlusTree<Integer,String> t = tree(4, 0, 0);
		
		for(int i=0; i<1000; i++)
		{
			t.insert(i, String.valueOf(i));
		}
		
		for(int i=999; i>=0; i--)
		{
			t.remove(i);
		}
		
		AtomicLong counter = new AtomicLong();
		t.rangeQuery(Integer.MIN_VALUE, true, Integer.MAX_VALUE, true, (k,v) ->
		{
			counter.incrementAndGet();
			return true;
		});
		TF.eq(counter.get(), 0L);
	}
	
	
	@Test
	public void test() throws Exception
	{
		BPlusTree<Integer,String> t = tree(4, 0, 9);
		t.remove(1);
		t.remove(3);
		t.remove(5);
		t.remove(7);
		t.remove(9);
		
		TF.eq(t.get(0), "0");
		TF.eq(t.get(1), null);
		TF.eq(t.get(2), "2");
		TF.eq(t.get(3), null);
		TF.eq(t.get(4), "4");
		TF.eq(t.get(5), null);
		TF.eq(t.get(6), "6");
		TF.eq(t.get(7), null);
		TF.eq(t.get(8), "8");
		TF.eq(t.get(9), null);
	}


	@Test
	public void testForward() throws Exception
	{
		BPlusTree<Integer,String> t = tree(4, 0, 9);
		
		query(t, 3, false, 7, false, 4, 5, 6);
		query(t, 3, true, 7, false, 3, 4, 5, 6);
		query(t, 3, false, 7, true, 4, 5, 6, 7);
		query(t, 3, true, 7, true, 3, 4, 5, 6, 7);
		query(t, 3, true, 3, true, 3);
		query(t, 3, true, 3, false);
		query(t, 1000, true, 2000, true);
		query(t, -1000, true, -2000, true);
		query(t, -1000, false, 1000, false, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9);
	}
	
	
	@Test
	public void testBackward() throws Exception
	{
		BPlusTree<Integer,String> t = tree(4, 0, 9);
		
		query(t, 7, false, 3, false, 6, 5, 4);
		query(t, 7, true, 3, false, 7, 6, 5, 4);
		query(t, 7, false, 3, true, 6, 5, 4, 3);
		query(t, 7, true, 3, true, 7, 6, 5, 4, 3);
		query(t, 3, true, 3, true, 3);
		query(t, 3, true, 3, false);
		query(t, 2000, true, 1000, true); 
		query(t, -2000, true, -1000, true);
		query(t, 1000, true, -1000, true, 9, 8, 7, 6, 5, 4, 3, 2, 1, 0);
	}
	

	protected <K extends Comparable<? super K>,V> List<V> rangeQuery(BPlusTree<K,V> t, K start, boolean includeStart, K end, boolean includeEnd) throws Exception
	{
		ArrayList<V> rv = new ArrayList<>();
		
		t.rangeQuery(start, includeStart, end, includeEnd, (k,v) ->
		{
			rv.add(v);
			return true;
		});
		
		return rv;
	}
	
	
	protected BPlusTree<Integer,String> tree(int branchingFactor, int start, int end) throws Exception
	{
		BPlusTree<Integer,String> t = new BPlusTree<Integer,String>(branchingFactor);
		for(int i=start; i<=end; i++)
		{
			t.insert(i, String.valueOf(i));
		}
		return t;
	}
	
	
	protected void query(BPlusTree<Integer,String> t, int start, boolean incStart, int end, boolean incEnd, int ... result) throws Exception
	{
		String[] exp = new String[result.length];
		for(int i=0; i<result.length; i++)
		{
			exp[i] = String.valueOf(result[i]);
		}
		
		List<String> rv = rangeQuery(t, start, incStart, end, incEnd);
		TF.eq(CKit.toArray(rv), exp);
	}
}
