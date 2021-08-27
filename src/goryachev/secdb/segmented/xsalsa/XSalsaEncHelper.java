// Copyright © 2021 Andy Goryachev <andy@goryachev.com>
package goryachev.secdb.segmented.xsalsa;
import goryachev.common.util.CKit;
import goryachev.crypto.Crypto;
import goryachev.crypto.OpaqueBytes;
import goryachev.crypto.OpaqueChars;
import goryachev.crypto.xsalsa20poly1305.XSalsa20Poly1305DecryptStream;
import goryachev.crypto.xsalsa20poly1305.XSalsa20Poly1305EncryptStream;
import goryachev.crypto.xsalsa20poly1305.XSalsaTools;
import goryachev.secdb.crypto.KeyFile;
import goryachev.secdb.segmented.EncHelper;
import goryachev.secdb.segmented.REMOVE.DebugInputStream;
import goryachev.secdb.segmented.REMOVE.DebugOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.SecureRandom;
import org.bouncycastle.crypto.digests.Blake2bDigest;


/**
 * EncryptionHelper based on XSalsa20 cipher and Poly1305 MAC.
 */
public class XSalsaEncHelper
	extends EncHelper
{
	private final SecureRandom random;
	
	
	public XSalsaEncHelper(SecureRandom r)
	{
		this.random = r;
	}
	
	
	public long convertLength(long len, boolean whenEncrypting)
	{
		return whenEncrypting ? len + XSalsaTools.MAC_LENGTH_BYTES : len - XSalsaTools.MAC_LENGTH_BYTES;
	}
	
	
	public InputStream getDecryptionStream(byte[] key, byte[] nonce, long length, InputStream in)
	{
		return new DebugInputStream
		(
			"rd:dec", 
			1024, 
			new XSalsa20Poly1305DecryptStream
			(
				Crypto.copy(key), 
				nonce,
				length,
				new DebugInputStream("read:enc", 1024, in)
			)
		);
	}


	public OutputStream getEncryptionStream(byte[] key, byte[] nonce, long length, OutputStream out)
	{
		return new DebugOutputStream
		(
			"wr:dec", 
			1024,
			new XSalsa20Poly1305EncryptStream
			(
				Crypto.copy(key), 
				nonce, 
				new DebugOutputStream("wr:enc", 1024, out)
			)
		);
	}


	public byte[] createNonce(String unique)
	{
		byte[] input = unique.getBytes(CKit.CHARSET_UTF8);
		byte[] nonce = new byte[XSalsaTools.NONCE_LENGTH_BYTES];
		Blake2bDigest blake2b = new Blake2bDigest(XSalsaTools.NONCE_LENGTH_BYTES);
		blake2b.update(input, 0, input.length);
		blake2b.doFinal(nonce, 0);
		return nonce;
	}


	public byte[] encryptKey(OpaqueBytes key, OpaqueChars passphrase) throws Exception
	{
		return KeyFile.encrypt(key, passphrase, random);
	}


	public OpaqueBytes decryptKey(byte[] encryptedKey, OpaqueChars passphrase) throws Exception
	{
		return KeyFile.decrypt(encryptedKey, passphrase);
	}

	
	protected final byte[] createNonce()
	{
		byte[] b = new byte[XSalsaTools.NONCE_LENGTH_BYTES];
		random.nextBytes(b);
		return b;
	}
	

	// TODO encrypt in blocks of 4096 bytes, first value is length (to avoid leaking secret size)
	public byte[] encryptSecret(byte[] key, char[] secret)
	{
		byte[] input = Crypto.chars2bytes(secret);
		try
		{
			byte[] rv = new byte[XSalsaTools.NONCE_LENGTH_BYTES + input.length];

			byte[] nonce = createNonce();
			System.arraycopy(nonce, 0, rv, 0, nonce.length);

			XSalsaTools.encrypt(key, nonce, 0, nonce.length, input, rv, XSalsaTools.NONCE_LENGTH_BYTES);
			return rv;
		}
		finally
		{
			Crypto.zero(input);
		}
	}


	public char[] decryptSecret(byte[] key, byte[] ciphertext)
	{
		byte[] b = XSalsaTools.decrypt(key, ciphertext, 0, XSalsaTools.NONCE_LENGTH_BYTES, ciphertext, XSalsaTools.NONCE_LENGTH_BYTES, ciphertext.length - XSalsaTools.NONCE_LENGTH_BYTES);
		try
		{
			return Crypto.bytes2chars(b);
		}
		finally
		{
			Crypto.zero(b);
		}
	}


	public byte[] deriveMaskingKey(byte[] key)
	{
		int len = XSalsaTools.KEY_LENGTH_BYTES;
	    Blake2bDigest blake2b = new Blake2bDigest(len);
	    if(key != null)
	    {
	    	blake2b.update(key, 0, key.length);
	    }
	    blake2b.update((byte)'m');
	    blake2b.update((byte)'a');
	    blake2b.update((byte)'s');
	    blake2b.update((byte)'K');
	    
	    byte[] rv = new byte[len];
	    blake2b.doFinal(rv, 0);
		return rv;
	}
}
