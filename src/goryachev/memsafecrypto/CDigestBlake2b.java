// Copyright © 2021-2026 Andy Goryachev <andy@goryachev.com>
package goryachev.memsafecrypto;
import goryachev.common.util.CDigest;
import goryachev.common.util.api.IMessageDigest;
import goryachev.common.util.api.IMessageDigestBlake2b;
import goryachev.memsafecrypto.bc.crypto.digests.Blake2bDigest;


/**
 * CDigest based on Blake2b.
 */
public class CDigestBlake2b
	extends CDigest
	implements IMessageDigestBlake2b
{
	public CDigestBlake2b(int bits)
	{
		super(new Blake2bMD(bits));
	}

	
	//
	
	
	protected static class Blake2bMD
		implements IMessageDigest
	{
		private final Blake2bDigest digest;
		
		
		public Blake2bMD(int bits)
		{
			this.digest = new Blake2bDigest(bits);
		}
		
		
		@Override
		public void update(byte b)
		{
			digest.update(b);
		}


		@Override
		public void update(byte[] buf, int offset, int length)
		{
			digest.update(buf, offset, length);
		}


		@Override
		public void reset()
		{
			digest.reset();
		}


		@Override
		public byte[] digest()
		{
			byte[] out = new byte[digest.getDigestSize()];
			digest.doFinal(out, 0);
			return out;
		}
	}
}
