// Copyright © 2021 Andy Goryachev <andy@goryachev.com>
package goryachev.secdb.segmented.xsalsa;
import goryachev.common.util.CKit;
import goryachev.crypto.OpaqueBytes;
import goryachev.crypto.xsalsa20poly1305.XSalsa20Poly1305DecryptStream;
import goryachev.crypto.xsalsa20poly1305.XSalsa20Poly1305EncryptStream;
import goryachev.crypto.xsalsa20poly1305.XSalsaTools;
import goryachev.secdb.segmented.IEncHelper;
import goryachev.secdb.segmented.REMOVE.DebugInputStream;
import goryachev.secdb.segmented.REMOVE.DebugOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.SecureRandom;
import org.bouncycastle.crypto.digests.Blake2bDigest;


/**
 * EncryptionHelper based on XSalsa20 cipher and Poly1305 MAC.
 */
public final class XSalsaEncHelper
	implements IEncHelper
{
	private final SecureRandom random;
	private final OpaqueBytes mainKey = new OpaqueBytes();
	
	
	public XSalsaEncHelper(SecureRandom r, OpaqueBytes mainKey)
	{
		this.random = r;
		this.mainKey.setValue(mainKey);
	}
	
	
	public long convertLength(long len, boolean whenEncrypting)
	{
		return whenEncrypting ? len + XSalsaTools.MAC_LENGTH_BYTES : len - XSalsaTools.MAC_LENGTH_BYTES;
	}
	
	
	public InputStream getDecryptionStream(String nonce, long length, InputStream in)
	{
		byte[] iv = createNonce(nonce);
		byte[] key = mainKey.getBytes();
		
		return new DebugInputStream
		(
			"rd:dec", 
			1024, 
			new XSalsa20Poly1305DecryptStream
			(
				key, 
				iv,
				length,
				new DebugInputStream("read:enc", 1024, in)
			)
		);
	}


	public OutputStream getEncryptionStream(String nonce, long length, OutputStream out)
	{
		byte[] iv = createNonce(nonce);
		byte[] key = mainKey.getBytes();
		
		return new DebugOutputStream
		(
			"wr:dec", 
			1024,
			new XSalsa20Poly1305EncryptStream
			(
				key, 
				iv, 
				new DebugOutputStream("wr:enc", 1024, out)
			)
		);
	}


	private byte[] createNonce(String unique)
	{
		byte[] input = unique.getBytes(CKit.CHARSET_UTF8);
		byte[] nonce = new byte[XSalsaTools.NONCE_LENGTH_BYTES];
		Blake2bDigest blake2b = new Blake2bDigest(XSalsaTools.NONCE_LENGTH_BYTES);
		blake2b.update(input, 0, input.length);
		blake2b.doFinal(nonce, 0);
		return nonce;
	}
}
