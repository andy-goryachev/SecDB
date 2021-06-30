// Copyright © 2021 Andy Goryachev <andy@goryachev.com>
package goryachev.secdb.segmented;
import goryachev.common.test.TF;
import goryachev.common.test.Test;
import goryachev.crypto.xsalsa20poly1305.XSalsaTools;
import goryachev.secdb.segmented.xsalsa.XSalsaEncHelper;
import java.security.SecureRandom;
import java.util.Random;


/**
 * Tests XSalsaEncHelper.
 */
public class TestEncHelper
{
	public static void main(String[] args) throws Exception
	{
		TF.run();
	}
	
	
	@Test
	public void test() throws Exception
	{
		t("");
		t(".");
		t("abracadabra");
	}
	
	
	protected void t(String input)
	{
		XSalsaEncHelper h = new XSalsaEncHelper(new SecureRandom());
		
		Random r = new Random();
		long seed= r.nextLong();
		TF.printf("seed=%016X", seed);
		r.setSeed(seed);
		
		byte[] key = new byte[XSalsaTools.KEY_LENGTH_BYTES];
		r.nextBytes(key);
		
		char[] in = (input == null ? null : input.toCharArray());
		
		byte[] enc = h.encryptSecret(key, in);
		
		char[] dec = h.decryptSecret(key, enc);
		
		TF.eq(dec, in);
	}
}
