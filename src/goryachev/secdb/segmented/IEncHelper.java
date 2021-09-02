// Copyright © 2020-2021 Andy Goryachev <andy@goryachev.com>
package goryachev.secdb.segmented;
import java.io.InputStream;
import java.io.OutputStream;


/**
 * Encryption Helper provides an interface for crypto operations for the 
 * database data streams.
 */
public interface IEncHelper
{
	public abstract long convertLength(long len, boolean whenEncrypting);

	
	/** 
	 * creates a encryption stream
	 * @param nonce
	 * @param cipherTextLength - the total number of ciphertext (incl. mac) bytes 
	 * @param out
	 */
	public abstract OutputStream getEncryptionStream(String nonce, long cipherTextLength, OutputStream out);

	
	/** 
	 * creates a decryption stream
	 * @param nonce
	 * @param cipherTextLength - the total number of ciphertext (incl. mac) bytes 
	 * @param in
	 */
	public abstract InputStream getDecryptionStream(String nonce, long cipherTextLength, InputStream in);
}
