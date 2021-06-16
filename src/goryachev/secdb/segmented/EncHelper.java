// Copyright © 2020-2021 Andy Goryachev <andy@goryachev.com>
package goryachev.secdb.segmented;
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
	
	
	//
	
	
	protected EncHelper()
	{
	}
}
