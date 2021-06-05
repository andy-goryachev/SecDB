// Copyright © 2020-2021 Andy Goryachev <andy@goryachev.com>
package goryachev.secdb.bplustree;
import goryachev.common.test.TF;
import goryachev.common.test.Test;
import goryachev.common.util.D;
import goryachev.common.util.SB;


/**
 * Tests BPlusTree: exhaustively deletes 2 of each keys.
 */
public class TestBPlusTreeDelete2Keys
{
	public static void main(String[] args)
	{
		TF.run();
	}
	
	
//	@Test
	public void testSpecific() throws Exception
	{
		t(0, 8);
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
	
	
	private void t(Integer first, Integer second) throws Exception
	{
		BPlusTree<Integer,String> t = new BPlusTree<Integer,String>(4);
		for(int i=0; i<16; i++)
		{
			t.insert(i, "" + i);
		}
		
		String origin = t.dumpKeys();
		
		t.remove(first);
		
		String prev = t.dumpKeys();
		
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
					TF.print("\norigin:", origin, "\ndelete", first, ":\n", prev, "\ndelete", second, ":\n", t.dumpKeys());
					throw new Error("missing " + i);
				}
			}
			else
			{
				TF.eq(i, Integer.parseInt(v));
			}
		}
	}
}
