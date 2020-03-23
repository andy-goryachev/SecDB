// Copyright © 2020 Andy Goryachev <andy@goryachev.com>
package goryachev.secdb.segmented;
import goryachev.crypto.Crypto;
import goryachev.crypto.EAXDecryptStream;
import goryachev.crypto.EAXEncryptStream;
import goryachev.crypto.OpaqueBytes;
import java.io.InputStream;
import java.io.OutputStream;


/**
 * Encryption Helper provides two implementations for when encryption is enabled 
 * and when it is disabled.
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
	
	
	public static EncHelper create(OpaqueBytes key)
	{
		if(key == null)
		{
			return new Clear();
		}
		else
		{
			return new Encrypted(key);
		}
	}
	
	
	//
	
	
	protected static class Clear extends EncHelper
	{
		public long convertLength(long len, boolean whenEncrypting)
		{
			return len;
		}
		

		protected InputStream getDecryptionStream(byte[] nonce, InputStream in)
		{
			return in;
		}


		protected OutputStream getEncryptionStream(byte[] nonce, OutputStream out)
		{
			return out;
		}
	}
	
	
	//
	
	
	protected static class Encrypted extends EncHelper
	{
		private static final int MAC_OVERHEAD = 8;
		private final OpaqueBytes key;
		
		
		protected Encrypted(OpaqueBytes key)
		{
			this.key = key;
		}
		
		
		public long convertLength(long len, boolean whenEncrypting)
		{
			return whenEncrypting ? len + MAC_OVERHEAD : len - MAC_OVERHEAD;
		}
		
		
		protected InputStream getDecryptionStream(byte[] nonce, InputStream in)
		{
			byte[] k = key.getBytes();
			try
			{
				return new DebugInputStream
				(
					"rd:dec", 
					1024, 
					new EAXDecryptStream
					(
						k, 
						nonce, 
						null, 
						new DebugInputStream("read:enc", 1024, in)
					)
				);
			}
			finally
			{
				Crypto.zero(k);
			}
		}


		protected OutputStream getEncryptionStream(byte[] nonce, OutputStream out)
		{
			byte[] k = key.getBytes();
			try
			{
				return new DebugOutputStream
				(
					"wr:dec", 
					1024,
					new EAXEncryptStream
					(
						k, 
						nonce, 
						null, 
						new DebugOutputStream("wr:enc", 1024, out)
					)
				);
			}
			finally
			{
				Crypto.zero(k);
			}
		}
	}
}
