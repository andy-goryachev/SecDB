// Copyright © 2020-2021 Andy Goryachev <andy@goryachev.com>
package goryachev.secdb.segmented.clear;
import goryachev.crypto.Crypto;
import goryachev.crypto.OpaqueBytes;
import goryachev.crypto.OpaqueChars;
import goryachev.secdb.segmented.EncHelper;
import java.io.InputStream;
import java.io.OutputStream;


/**
 * Clear Text EncHelper.
 */
public class ClearEncHelper
	extends EncHelper
{
	public ClearEncHelper()
	{
	}
	
	
	public long convertLength(long len, boolean whenEncrypting)
	{
		return len;
	}
	

	public InputStream getDecryptionStream(byte[] key, byte[] nonce, long length, InputStream in)
	{
		return in;
	}


	public OutputStream getEncryptionStream(byte[] key, byte[] nonce, long length, OutputStream out)
	{
		return out;
	}


	public byte[] createNonce(String unique)
	{
		return null;
	}


	public byte[] encryptKey(OpaqueBytes key, OpaqueChars passphrase) throws Exception
	{
		return new byte[0];
	}


	public OpaqueBytes decryptKey(byte[] encryptedKey, OpaqueChars passphrase) throws Exception
	{
		return null;
	}
	
	
	public byte[] encryptSecret(byte[] key, char[] secret)
	{
		return Crypto.chars2bytes(secret);
	}


	public char[] decryptSecret(byte[] key, byte[] ciphertext)
	{
		return Crypto.bytes2chars(ciphertext);
	}


	public byte[] deriveMaskingKey(byte[] key)
	{
		return null;
	}


	public OpaqueBytes generateKey()
	{
		return null;
	}


	public byte[] deriveKey(String id, String appendix)
	{
		return null;
	}
}