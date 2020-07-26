// Copyright © 2020 Andy Goryachev <andy@goryachev.com>
package goryachev.secdb.bplustree;
import goryachev.common.test.TF;
import goryachev.common.test.Test;
import goryachev.common.util.D;
import goryachev.common.util.SB;


/**
 * Tests BPlusTree Deletion.
 * 
 * based on https://www.programiz.com/dsa/deletion-from-a-b-plus-tree
 * NOTE: cannot use ^^ because the initial tree differs from that mentioned in the article.
 */
public class TestBPlusTreeDeletion
{
	public static void main(String[] args)
	{
		TF.run();
	}
	
	
//	@Test
	public void testState() throws Exception
	{
		BPlusTree<Integer,String> t = create(new int[] { 5, 15, 20, 25, 30, 35, 40, 45, 55 });
		D.print(t.dumpKeys());
	}
	
	
	@Test
	public void testCase1() throws Exception
	{
		test
		(
			new int[] { 5, 15, 20, 25, 30, 35, 40, 45, 55 },
			40,
			"    L=5",
			"    L=15",
			"  I=20",
			"    L=20",
			"    L=25",
			"I=30",
			"    L=30",
			"    L=35",
			"  I=45",
			"    L=45",
			"    L=55"
		);
		
//		BPlusTree<String,String> t = new BPlusTree<String,String>(4);
//		
//		t.insert("1", "1");
//		t.insert("2", "2");
//		t.insert("3", "3");
//		t.insert("4", "4");
//		D.print(t.dumpKeys());
//		
//		t.remove("3"); // FIX breaks the tree
//		D.print(t.dumpKeys());
//		
//		t.remove("4");
//		D.print(t.dumpKeys());
	}
	
	
	private void test(int[] elements, int toDelete, String ... exp) throws Exception
	{
		BPlusTree<Integer,String> t = create(elements);
		
		t.remove(toDelete);
		
		String result = t.dumpKeys();
		
		SB sb = new SB();
		sb.nl();
		for(String s: exp)
		{
			sb.append(s).nl();
		}
		String expected = sb.toString();
		
		TF.print("result", result);
		TF.print("expected", expected);
		
		TF.eq(result, expected);
	}


	private BPlusTree<Integer,String> create(int[] elements) throws Exception
	{
		BPlusTree<Integer,String> t = new BPlusTree<Integer,String>(3);
		
		for(int v: elements)
		{
			t.insert(v, String.valueOf(v));
		}
		
		return t;
	}
	
	
	//
	
	
	
}
