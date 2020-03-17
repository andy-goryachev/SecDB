// Copyright © 2020 Andy Goryachev <andy@goryachev.com>
package goryachev.secdb.segmented;
import goryachev.crypto.OpaqueBytes;
import java.io.InputStream;
import java.io.OutputStream;


/**
 * Encryption Helper provides two implementations for when encryption is enabled 
 * and when it is disabled.
 */
public abstract class EncHelper
{
	public abstract long getLengthFor(long len);
	
	protected abstract InputStream getDecryptionStream(InputStream in);
	
	protected abstract OutputStream getEncryptionStream(OutputStream out);
	
	
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
		public long getLengthFor(long len)
		{
			return len;
		}
		

		protected InputStream getDecryptionStream(InputStream in)
		{
			return in;
		}


		protected OutputStream getEncryptionStream(OutputStream out)
		{
			return out;
		}
	}
	
	
	//
	
	
	protected static class Encrypted extends EncHelper
	{
		private final OpaqueBytes key;
		
		
		protected Encrypted(OpaqueBytes key)
		{
			this.key = key;
		}
		
		
		public long getLengthFor(long len)
		{
			// TODO
			return len;
		}
		
		
		protected InputStream getDecryptionStream(InputStream in)
		{
			// TODO
			return in;
		}


		protected OutputStream getEncryptionStream(OutputStream out)
		{
			// TODO
			return out;
		}
	}
}
