// Copyright © 2020-2021 Andy Goryachev <andy@goryachev.com>
package goryachev.secdb.segmented;
import goryachev.crypto.OpaqueBytes;
import goryachev.crypto.OpaqueChars;
import java.io.InputStream;
import java.io.OutputStream;


/**
 * Encryption Helper provides an interface for crypto operations for the 
 * database data streams.
 */
public abstract class EncHelper
{
	public abstract long convertLength(long len, boolean whenEncrypting);

	
	/** 
	 * creates a encryption stream
	 * @param nonce
	 * @param cipherTextLength - the total number of ciphertext (incl. mac) bytes 
	 * @param out
	 */
	public abstract OutputStream getEncryptionStream(byte[] key, byte[] nonce, long cipherTextLength, OutputStream out);

	
	/** 
	 * creates a decryption stream
	 * @param nonce
	 * @param cipherTextLength - the total number of ciphertext (incl. mac) bytes 
	 * @param in
	 */
	public abstract InputStream getDecryptionStream(byte[] key, byte[] nonce, long cipherTextLength, InputStream in);
	
	
	/** create appropriate nonce.  input parameter is guaranteed to be unique for all objects in the store */
	public abstract byte[] createNonce(String unique);
	
	
	/** 
	 * encrypts the key with the provided passphrase.  
	 * @return non-null encrypted data
	 */ 
	public abstract byte[] encryptKey(OpaqueBytes key, OpaqueChars passphrase) throws Exception;
	
	
	/** decrypts the key with the provided passphrase.  may return null */
	public abstract OpaqueBytes decryptKey(byte[] encryptedKey, OpaqueChars passphrase) throws Exception;
	
	
	public abstract byte[] encryptSecret(byte[] key, char[] secret);
	
	
	public abstract char[] decryptSecret(byte[] key, byte[] ciphertext);


	public abstract byte[] deriveMaskingKey(byte[] key);
	
	
	//
	
	
	public EncHelper()
	{
	}
}
