package research.xsalsa20poly1305;
import goryachev.common.test.TF;
import goryachev.common.test.Test;
import goryachev.common.util.CKit;
import goryachev.crypto.xsalsa20poly1305.XSalsa20Poly1305DecryptStream;
import goryachev.crypto.xsalsa20poly1305.XSalsa20Poly1305EncryptStream;
import goryachev.crypto.xsalsa20poly1305.XSalsaTools;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Random;


public class TestXSalsa20poly1306Streams
{
	public static void main(String[] args)
	{
		TF.run();
	}
	
	
	@Test
	public void testStreams() throws Exception
	{
		int cleartextSize = 1_000_000;
		
		// for reproducibility
		Random r = new Random();
		long seed = r.nextLong();
		TF.print("seed", seed);
		
		r.setSeed(seed);
		
		byte[] key = new byte[XSalsaTools.KEY_LENGTH_BYTES];
		r.nextBytes(key);
		
		byte[] nonce = new byte[XSalsaTools.NONCE_LENGTH_BYTES];
		r.nextBytes(nonce);
		
		byte[] clearText = new byte[cleartextSize];
		r.nextBytes(clearText);
		
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		XSalsa20Poly1305EncryptStream es = new XSalsa20Poly1305EncryptStream(key, nonce, out);
		try
		{
			es.write(clearText);
		}
		finally
		{
			CKit.close(es);
		}
		
		byte[] cipherText = out.toByteArray();
		TF.printf("clear text length=%d, ciphertext length=%d", cleartextSize, cipherText.length);
		
		ByteArrayInputStream in = new ByteArrayInputStream(cipherText);
		XSalsa20Poly1305DecryptStream is = new XSalsa20Poly1305DecryptStream(key, nonce, cipherText.length, in);
		try
		{
			byte[] clear2 = CKit.readBytes(is);
			TF.eq(clearText, clear2);
		}
		finally
		{
			CKit.close(is);
		}
		
	}
}