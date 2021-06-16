// Copyright © 2021 Andy Goryachev <andy@goryachev.com>
package goryachev.secdb.segmented.xsalsa20poly1305;
import goryachev.crypto.Crypto;
import goryachev.crypto.OpaqueBytes;
import goryachev.crypto.xsalsa20poly1305.XSalsa20Poly1305DecryptStream;
import goryachev.crypto.xsalsa20poly1305.XSalsa20Poly1305EncryptStream;
import goryachev.secdb.segmented.EncHelper;
import goryachev.secdb.segmented.REMOVE.DebugInputStream;
import goryachev.secdb.segmented.REMOVE.DebugOutputStream;
import java.io.InputStream;
import java.io.OutputStream;


/**
 * Xsalsa20poly1305 EncHelper.
 */
public class Xsalsa20poly1305EncHelper
	extends EncHelper
{
	private static final int MAC_OVERHEAD = 8;
	private final OpaqueBytes key;
	
	
	public Xsalsa20poly1305EncHelper(OpaqueBytes key)
	{
		this.key = key;
	}
	
	
	public long convertLength(long len, boolean whenEncrypting)
	{
		return whenEncrypting ? len + MAC_OVERHEAD : len - MAC_OVERHEAD;
	}
	
	
	protected InputStream getDecryptionStream(byte[] nonce, long length, InputStream in)
	{
		byte[] k = key.getBytes();
		try
		{
			return new DebugInputStream
			(
				"rd:dec", 
				1024, 
				new XSalsa20Poly1305DecryptStream
				(
					k, 
					nonce,
					length,
					new DebugInputStream("read:enc", 1024, in)
				)
			);
		}
		finally
		{
			Crypto.zero(k);
		}
	}


	protected OutputStream getEncryptionStream(byte[] nonce, long length, OutputStream out)
	{
		byte[] k = key.getBytes();
		try
		{
			return new DebugOutputStream
			(
				"wr:dec", 
				1024,
				new XSalsa20Poly1305EncryptStream
				(
					k, 
					nonce, 
					new DebugOutputStream("wr:enc", 1024, out)
				)
			);
		}
		finally
		{
			Crypto.zero(k);
		}
	}
}
