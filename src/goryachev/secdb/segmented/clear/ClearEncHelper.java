// Copyright © 2020-2024 Andy Goryachev <andy@goryachev.com>
package goryachev.secdb.segmented.clear;
import goryachev.secdb.segmented.IEncHelper;
import java.io.InputStream;
import java.io.OutputStream;


/**
 * Clear Text EncHelper.
 */
public class ClearEncHelper
	implements IEncHelper
{
	public ClearEncHelper()
	{
	}
	
	
	public long convertLength(long len, boolean whenEncrypting)
	{
		return len;
	}
	

	public InputStream getDecryptionStream(String nonce, long length, InputStream in)
	{
		return in;
	}


	public OutputStream getEncryptionStream(String nonce, long length, OutputStream out)
	{
		return out;
	}
}