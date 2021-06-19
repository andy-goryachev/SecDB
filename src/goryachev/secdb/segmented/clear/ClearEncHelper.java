// Copyright © 2020-2021 Andy Goryachev <andy@goryachev.com>
package goryachev.secdb.segmented.clear;
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
	

	protected InputStream getDecryptionStream(byte[] nonce, long length, InputStream in)
	{
		return in;
	}


	protected OutputStream getEncryptionStream(byte[] nonce, long length, OutputStream out)
	{
		return out;
	}


	protected byte[] createNonce(String a1, long a2)
	{
		return null;
	}
}