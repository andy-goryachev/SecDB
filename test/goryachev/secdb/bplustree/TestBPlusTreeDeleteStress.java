// Copyright © 2020 Andy Goryachev <andy@goryachev.com>
package goryachev.secdb.bplustree;
import goryachev.common.test.TF;
import goryachev.common.test.Test;
import goryachev.common.util.CMap;
import java.util.Random;


/**
 * BPlusTree stress test.
 */
public class TestBPlusTreeDeleteStress
{
	public static void main(String[] args)
	{
		TF.run();
	}
	
	
	@Test
	public void stressTest() throws Exception
	{
		for(int i=0; i<1000; i++)
		{
			test();
		}
	}
	
	
	protected void test() throws Exception
	{
		long seed = new Random().nextLong();
		TF.print(seed);
	}
	
	
	protected void test(long seed) throws Exception
	{
		Random r = new Random(seed);
		BPlusTree<Integer,String> t = new BPlusTree<Integer,String>(4);
		CMap<Integer,String> m = new CMap();
		
		try
		{
			for(int i=0; i<10000; i++)
			{
				int ct = 1 + r.nextInt(200);
				for(int j=0; j<ct; j++)
				{
					int k = r.nextInt(256);
					
					String prev = t.dumpKeys();
					
					try
					{
						String v = "v." + k;
						t.insert(k, v);
						m.put(k, v);
						
						TF.print("add", k);
						check(m, t);
						t.checkInvariants();
					}
					catch(Throwable e)
					{
						TF.print("previous:", prev);
						throw new Error("adding key=" + k, e);
					}
				}
				
				for(int j=0; j<100; j++)
				{
					int k = r.nextInt(256);
					
					String prev = t.dumpKeys();
					
					try
					{
						t.remove(k);
						m.remove(k);
						
						TF.print("remove", k);
						check(m, t);
						t.checkInvariants();
					}
					catch(Throwable e)
					{
						TF.print("previous:", prev);
						throw new Error("deleting key=" + k, e);
					}
				}
			}
		}
		catch(Throwable e)
		{
			try
			{
				TF.print(t.dumpKeys());
			}
			catch(Throwable err)
			{
				err.printStackTrace();
			}
			throw e;
		}
		
		check(m, t);
		
	}
	
	
	protected void check(CMap<Integer,String> m, BPlusTree<Integer,String> t) throws Exception
	{
		for(int k=0; k<256; k++)
		{
			String vt = t.get(k);
			String vm = m.get(k);
			TF.eq(vt, vm);
		}		
	}
}
