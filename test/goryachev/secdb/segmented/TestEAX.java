// Copyright © 2020-2021 Andy Goryachev <andy@goryachev.com>
package goryachev.secdb.segmented;
import goryachev.common.test.TF;
import goryachev.common.test.Test;
import goryachev.common.util.D;
import goryachev.crypto.eax.EAXCipher;
import java.util.Random;
import org.bouncycastle.crypto.engines.AESEngine;
import org.bouncycastle.crypto.modes.EAXBlockCipher;
import org.bouncycastle.crypto.params.AEADParameters;
import org.bouncycastle.crypto.params.KeyParameter;


/**
 * Test EAX.
 */
public class TestEAX
{
	public static void main(String[] args)
	{
		TF.run();
	}
	
	
//	@Test
	public void test() throws Exception
	{
		byte[] key = new byte[256/8];
		int MAC_SIZE_BITS = 64;
		byte[] nonce = new byte[100];
		byte[] ZERO_BYTE_ARRAY = new byte[0];
		
		EAXBlockCipher cipher = new EAXBlockCipher(new AESEngine());
		AEADParameters par = new AEADParameters(new KeyParameter(key), MAC_SIZE_BITS, nonce, ZERO_BYTE_ARRAY);
		cipher.init(true, par);

		for(int i=0; i<300; i++)
		{
			int len = cipher.getOutputSize(i);
			D.print(i, len);
		}
	}
	
	
	@Test
	public void test2() throws Exception
	{
		Random r = new Random();
		byte[] key = new byte[256/8];
		byte[] nonce = new byte[100];
		
		for(int i=0; i<300; i++)
		{
			byte[] data = new byte[i];
			r.nextBytes(data);
			
			byte[] enc = EAXCipher.encrypt(key, nonce, data);
			byte[] dec = EAXCipher.decrypt(key, nonce, enc);
			TF.eq(data, dec);
			
			D.print(i, enc.length, enc.length-i);
		}
	}
}
