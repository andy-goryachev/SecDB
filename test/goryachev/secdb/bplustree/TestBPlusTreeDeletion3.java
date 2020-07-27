// Copyright © 2020 Andy Goryachev <andy@goryachev.com>
package goryachev.secdb.bplustree;
import goryachev.common.test.TF;
import goryachev.common.test.Test;
import goryachev.common.util.D;
import goryachev.common.util.SB;


/**
 * Tests BPlusTree Deletion 3.
 */
public class TestBPlusTreeDeletion3
{
	public static void main(String[] args)
	{
		TF.run();
	}
	
	
	@Test
	public void test() throws Exception
	{
		for(int first=0; first<16; first++)
		{
			for(int second=0; second<16; second++)
			{
				try
				{
					t(first, second);
				}
				catch(Throwable e)
				{
					TF.print(first, second);
					throw new Error(e);
				}
			}
		}
	}
	
	
	private void t(int first, int second) throws Exception
	{
		BPlusTree<Integer,String> t = new BPlusTree<Integer,String>(4);
		for(int i=0; i<16; i++)
		{
			t.insert(i, "" + i);
		}
		
		t.remove(first);
		t.remove(second);
		
		for(int i=0; i<16; i++)
		{
			String v = t.get(i);
			if(v == null)
			{
				if((i==first) || (i==second))
				{
					// ok then
				}
				else
				{
					throw new Error();
				}
			}
			else
			{
				TF.eq(i, Integer.parseInt(v));
			}
		}
		
//		String keys = t.dumpKeys();
//		D.print(toDelete, keys);
		
//		
//		SB sb = new SB();
//		sb.nl();
//		for(String s: exp)
//		{
//			sb.append(s).nl();
//		}
//		String expected = sb.toString();
//		
//		TF.print("result", result);
//		TF.print("expected", expected);
//		
//		TF.eq(result, expected);
	}
}
