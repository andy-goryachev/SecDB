// Copyright © 2018-2019 Andy Goryachev <andy@goryachev.com>
package research.bplustree;
import goryachev.common.test.TF;
import goryachev.common.test.Test;
import goryachev.common.util.CKit;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import research.bplustree.BPlusTree.QueryClient;


public class BPlusTreeTest
{
	public static void main(String[] args)
	{
		TF.run();
	}
	
	
	@Test
	public void testLoad()
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
		QueryClient<Integer,String> c = new BPlusTree.QueryClient<Integer,String>()
		{
			public boolean acceptQueryResult(Integer key, String value)
			{
				counter.incrementAndGet();
				return true;
			}

			public void onError(Throwable err)
			{
				err.printStackTrace();
			}
		};
		t.query(Integer.MIN_VALUE, true, Integer.MAX_VALUE, true, c);
		TF.eq(counter.get(), 0L);
	}
	
	
	@Test
	public void test()
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
	public void testForward()
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
	public void testBackward()
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
	

	protected <K extends Comparable<? super K>,V> List<V> searchRange(BPlusTree<K,V> t, K start, boolean includeStart, K end, boolean includeEnd)
	{
		ArrayList<V> rv = new ArrayList<>();
		
		t.query(start, includeStart, end, includeEnd, new BPlusTree.QueryClient<K,V>()
		{
			public boolean acceptQueryResult(K key, V value)
			{
				rv.add(value);
				return true;
			}

			public void onError(Throwable err)
			{
				err.printStackTrace();
			}
		});
		
		return rv;
	}
	
	
	protected BPlusTree<Integer,String> tree(int branchingFactor, int start, int end)
	{
		BPlusTree<Integer,String> t = new BPlusTree<Integer,String>(branchingFactor);
		for(int i=start; i<=end; i++)
		{
			t.insert(i, String.valueOf(i));
		}
		return t;
	}
	
	
	protected void query(BPlusTree<Integer,String> t, int start, boolean incStart, int end, boolean incEnd, int ... result)
	{
		String[] exp = new String[result.length];
		for(int i=0; i<result.length; i++)
		{
			exp[i] = String.valueOf(result[i]);
		}
		
		List<String> rv = searchRange(t, start, incStart, end, incEnd);
		TF.eq(CKit.toArray(rv), exp);
	}
}
