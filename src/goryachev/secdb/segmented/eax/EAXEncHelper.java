// Copyright © 2020-2021 Andy Goryachev <andy@goryachev.com>
package goryachev.secdb.segmented.eax;
import goryachev.common.io.DWriter;
import goryachev.common.util.CKit;
import goryachev.crypto.Crypto;
import goryachev.crypto.OpaqueBytes;
import goryachev.crypto.OpaqueChars;
import goryachev.crypto.eax.EAXDecryptStream;
import goryachev.crypto.eax.EAXEncryptStream;
import goryachev.secdb.crypto.KeyFile;
import goryachev.secdb.segmented.EncHelper;
import goryachev.secdb.segmented.REMOVE.DebugInputStream;
import goryachev.secdb.segmented.REMOVE.DebugOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.SecureRandom;


/**
 * EAX Encryption Helper.
 */
public class EAXEncHelper
	extends EncHelper
{
	private static final int MAC_OVERHEAD = 8;
	private final SecureRandom random;
	private final OpaqueBytes key;
	
	
	public EAXEncHelper(OpaqueBytes key, SecureRandom r)
	{
		this.key = key;
		this.random = r;
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
				new EAXDecryptStream
				(
					k, 
					nonce, 
					null, 
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
				new EAXEncryptStream
				(
					k, 
					nonce, 
					null, 
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
		
		return b.toByteArray();
	}


	protected byte[] encryptKey(OpaqueBytes key, OpaqueChars passphrase) throws Exception
	{
		return KeyFile.encrypt(key, passphrase, random);
	}


	protected OpaqueBytes decryptKey(byte[] encryptedKey, OpaqueChars passphrase) throws Exception
	{
		return KeyFile.decrypt(encryptedKey, passphrase);
	}
}