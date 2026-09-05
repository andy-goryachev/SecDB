// Copyright © 2020-2026 Andy Goryachev <andy@goryachev.com>
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
	
	
	@Override
	public long convertLength(long len, boolean whenEncrypting)
	{
		return len;
	}
	

	@Override
	public InputStream getDecryptionStream(String nonce, long length, InputStream in)
	{
		return in;
	}


	@Override
	public OutputStream getEncryptionStream(String nonce, long length, OutputStream out)
	{
		return out;
	}
}