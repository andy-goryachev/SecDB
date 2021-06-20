// Copyright © 2020-2021 Andy Goryachev <andy@goryachev.com>
package goryachev.secdb.segmented.eax;
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
	
	
	public EAXEncHelper(SecureRandom r)
	{
		this.random = r;
	}
	
	
	public long convertLength(long len, boolean whenEncrypting)
	{
		return whenEncrypting ? len + MAC_OVERHEAD : len - MAC_OVERHEAD;
	}
	
	
	protected InputStream getDecryptionStream(byte[] key, byte[] nonce, long length, InputStream in)
	{
		return new DebugInputStream
		(
			"rd:dec", 
			1024, 
			new EAXDecryptStream
			(
				Crypto.copy(key), 
				nonce, 
				null, 
				new DebugInputStream("read:enc", 1024, in)
			)
		);
	}


	protected OutputStream getEncryptionStream(byte[] key, byte[] nonce, long length, OutputStream out)
	{
		return new DebugOutputStream
		(
			"wr:dec", 
			1024,
			new EAXEncryptStream
			(
				Crypto.copy(key), 
				nonce, 
				null, 
				new DebugOutputStream("wr:enc", 1024, out)
			)
		);
}
	
	
	protected byte[] createNonce(String unique)
	{
		return unique.getBytes(CKit.CHARSET_UTF8);
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