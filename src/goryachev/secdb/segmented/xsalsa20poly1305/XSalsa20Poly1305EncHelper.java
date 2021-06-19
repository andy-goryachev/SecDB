// Copyright © 2021 Andy Goryachev <andy@goryachev.com>
package goryachev.secdb.segmented.xsalsa20poly1305;
import goryachev.common.io.DWriter;
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
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.SecureRandom;
import org.bouncycastle.crypto.digests.Blake2bDigest;


/**
 * XSalsa20Poly1305 EncHelper.
 */
public class XSalsa20Poly1305EncHelper
	extends EncHelper
{
	private final OpaqueBytes key;
	private final SecureRandom random;
	
	
	public XSalsa20Poly1305EncHelper(OpaqueBytes key, SecureRandom r)
	{
		this.key = key;
		this.random = r;
	}
	
	
	public long convertLength(long len, boolean whenEncrypting)
	{
		return whenEncrypting ? len + XSalsaTools.MAC_LENGTH_BYTES : len - XSalsaTools.MAC_LENGTH_BYTES;
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
	
	
	protected byte[] createNonce(String a1, long a2)
	{
		ByteArrayOutputStream b = new ByteArrayOutputStream();
		DWriter wr = new DWriter(b);
		try
		{
			wr.writeString(a1);
			wr.writeLong(a2);
		}
		catch(Exception e)
		{
			// should never happen
			throw new Error("createNonce", e);
		}
		finally
		{
			CKit.close(wr);
		}
		
		byte[] input = b.toByteArray();
		
	    Blake2bDigest blake2b = new Blake2bDigest(XSalsaTools.NONCE_LENGTH_BYTES);
	    blake2b.update(input, input.length, 0);

	    byte[] nonce = new byte[XSalsaTools.NONCE_LENGTH_BYTES];
	    blake2b.doFinal(nonce, 0);
	    return nonce;
	}
	
	
	protected byte[] encryptKey(OpaqueChars passphrase) throws Exception
	{
		return KeyFile.encrypt(key, passphrase, random);
	}


	protected OpaqueBytes decryptKey(byte[] encryptedKey, OpaqueChars passphrase) throws Exception
	{
		return KeyFile.decrypt(encryptedKey, passphrase);
	}
}
