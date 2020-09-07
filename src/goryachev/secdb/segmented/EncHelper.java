// Copyright © 2020 Andy Goryachev <andy@goryachev.com>
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
	
	protected abstract InputStream getDecryptionStream(byte[] nonce, InputStream in);
	
	protected abstract OutputStream getEncryptionStream(byte[] nonce, OutputStream out);
	
	
	//
	
	
	protected EncHelper()
	{
	}
}
