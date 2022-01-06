// Copyright © 2021-2022 Andy Goryachev <andy@goryachev.com>
package goryachev.secdb.segmented.xsalsa;
import goryachev.common.util.CKit;
import goryachev.memsafecrypto.CByteArray;
import goryachev.memsafecrypto.Crypto;
import goryachev.memsafecrypto.OpaqueBytes;
import goryachev.memsafecrypto.bc.Blake2bDigest;
import goryachev.memsafecrypto.salsa.XSalsa20Poly1305DecryptStream;
import goryachev.memsafecrypto.salsa.XSalsa20Poly1305EncryptStream;
import goryachev.memsafecrypto.salsa.XSalsaTools;
import goryachev.secdb.segmented.IEncHelper;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.SecureRandom;


/**
 * EncryptionHelper based on XSalsa20 cipher and Poly1305 MAC.
 */
public final class XSalsaEncHelper
	implements IEncHelper
{
	private final SecureRandom random;
	private final OpaqueBytes mainKey = new OpaqueBytes();
	
	
	public XSalsaEncHelper(SecureRandom r, OpaqueBytes mainKey)
	{
		this.random = r;
		this.mainKey.setValue(mainKey);
	}
	
	
	public long convertLength(long len, boolean whenEncrypting)
	{
		return whenEncrypting ? len + XSalsaTools.MAC_LENGTH_BYTES : len - XSalsaTools.MAC_LENGTH_BYTES;
	}
	
	
	public InputStream getDecryptionStream(String nonce, long length, InputStream in)
	{
		CByteArray iv = createNonce(nonce);
		CByteArray key = mainKey.getCByteArray();
		try
		{
			return new XSalsa20Poly1305DecryptStream(key, iv, length, in);
		}
		finally
		{
			Crypto.zero(key);
		}
	}


	public OutputStream getEncryptionStream(String nonce, long length, OutputStream out)
	{
		CByteArray iv = createNonce(nonce);
		CByteArray key = mainKey.getCByteArray();
		try
		{
			return new XSalsa20Poly1305EncryptStream(key, iv, out);
		}
		finally
		{
			Crypto.zero(key);
		}
	}


	private CByteArray createNonce(String unique)
	{
		byte[] input = unique.getBytes(CKit.CHARSET_UTF8);
		CByteArray nonce = new CByteArray(XSalsaTools.NONCE_LENGTH_BYTES);
		Blake2bDigest blake2b = new Blake2bDigest(XSalsaTools.NONCE_LENGTH_BYTES);
		blake2b.update(input, 0, input.length);
		blake2b.doFinal(nonce, 0);
		return nonce;
	}
}
