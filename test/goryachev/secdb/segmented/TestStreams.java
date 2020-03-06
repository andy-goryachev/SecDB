// Copyright © 2020 Andy Goryachev <andy@goryachev.com>
package goryachev.secdb.segmented;
import goryachev.common.test.TF;
import goryachev.common.test.Test;
import goryachev.common.util.D;
import goryachev.common.util.Hex;


/**
 * Test Streams.
 */
public class TestStreams
{
	public static void main(String[] args)
	{
		TF.run();
	}
	
	
	@Test
	public void testPseudoRandom() throws Exception
	{
		for(int i=0; i<100; i++)
		{
			byte[] b = TestLarge.readBytes(TestLarge.createStream(i), 16);
			D.print(i, "\n" + Hex.toHexStringASCII(b));
		}
	}
}
