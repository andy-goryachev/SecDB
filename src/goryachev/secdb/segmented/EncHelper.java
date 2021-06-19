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
	protected abstract OutputStream getEncryptionStream(byte[] nonce, long cipherTextLength, OutputStream out);

	
	/** 
	 * creates a decryption stream
	 * @param nonce
	 * @param cipherTextLength - the total number of ciphertext (incl. mac) bytes 
	 * @param in
	 */
	protected abstract InputStream getDecryptionStream(byte[] nonce, long cipherTextLength, InputStream in);
	
	
	/** create appropriate nonce.  combination of the input parameters is guaranteed to be unique */
	protected abstract byte[] createNonce(String a1, long a2);
	
	
	/** 
	 * encrypts the key with the provided passphrase.  
	 * @return non-null encrypted data
	 */ 
	protected abstract byte[] encryptKey(OpaqueChars passphrase) throws Exception;
	
	
	/** decrypts the key with the provided passphrase.  may return null */
	protected abstract OpaqueBytes decryptKey(byte[] encryptedKey, OpaqueChars passphrase) throws Exception;
	
	
	//
	
	
	protected EncHelper()
	{
	}
}
