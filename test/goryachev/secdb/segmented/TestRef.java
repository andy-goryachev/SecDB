// Copyright © 2021 Andy Goryachev <andy@goryachev.com>
package goryachev.secdb.segmented;
import goryachev.common.io.DReader;
import goryachev.common.io.DWriter;
import goryachev.common.test.TF;
import goryachev.common.test.Test;
import goryachev.common.util.CKit;
import goryachev.common.util.D;
import goryachev.common.util.Dump;
import java.io.ByteArrayOutputStream;


/**
 * Tests Ref.
 */
public class TestRef
{
	public static void main(String[] args) throws Exception
	{
		TF.run();
	}
	
	
	@Test
	public void test() throws Exception
	{
		t(new Ref.SingleSegment(0x0123456789abcdefL, "seg", 0x7edcba9876543210L));
		t(new Ref.MultiSegment
		(
			0x0123456789abcdefL, 
			new String[]
			{
				"s1",
				"s2",
				"s3",
			},
			new long[]
			{
				0x1234000500060007L,
				0x7876500040003000L,
				0x1237681723681723L
			}
		));
	}
	
	
	protected void t(Ref r) throws Exception
	{
		String s1 = r.toPersistentString();
		TF.print(s1);
		
		Ref r2 = Ref.parse(s1);
		String s2 = r2.toPersistentString();
		TF.eq(s1, s2);
		
		ByteArrayOutputStream ba = new ByteArrayOutputStream();
		DWriter wr =  new DWriter(ba);
		try
		{
			r.write(wr);
		}
		finally
		{
			CKit.close(wr);
		}
		
		byte[] b1 = ba.toByteArray();
		TF.print(Dump.hex(b1, 0));
		
		Ref r3;
		DReader rd = new DReader(b1);
		try
		{
			r3 = Ref.read(rd);
		}
		finally
		{
			CKit.close(rd);
		}
		
		String s3 = r3.toPersistentString();
		TF.eq(s1, s3);
	}
}
