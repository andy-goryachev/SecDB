// Copyright © 2020 Andy Goryachev <andy@goryachev.com>
package goryachev.secdb.crypto;
import goryachev.common.io.DReader;
import goryachev.common.io.DWriter;
import goryachev.common.util.CKit;
import goryachev.crypto.Crypto;
import goryachev.crypto.EAXDecryptStream;
import goryachev.crypto.EAXEncryptStream;
import goryachev.crypto.OpaqueBytes;
import goryachev.crypto.OpaqueChars;
import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.SecureRandom;
import org.bouncycastle.crypto.generators.SCrypt;


/**
 * Handles the main key persistence.
 */
public final class KeyFile
{
	public static final long SIGNATURE_V1 = 0x1DEA202003141447L;
	
	public static final int KEY_SIZE_BYTES = 256/8;
	public static final int NONCE_SIZE_BYTES = 256/8;
	public static final int SCRYPT_N = 16384;
	public static final int SCRYPT_R = 8;
	public static final int SCRYPT_P = 32;
	
	public static final String ERROR_INVALID_FORMAT = "ERROR_INVALID_FORMAT";
	public static final String ERROR_WRONG_SIGNATURE = "ERROR_WRONG_SIGNATURE";
	
	
	/**
	 * Encrypts payload byte array with a key derived from the supplied password.
	 * Returns the byte array formatted according to the specification.  
	 */
	public static final byte[] encrypt(OpaqueBytes data, OpaqueChars pass, SecureRandom random) throws Exception
	{
		int n = SCRYPT_N;
		int r = SCRYPT_R;
		int p = SCRYPT_P;

		ByteArrayOutputStream bout = new ByteArrayOutputStream();
		DWriter out = new DWriter(bout);
		try
		{
			// header
			out.writeLong(SIGNATURE_V1);
			out.writeInt(n);
			out.writeInt(r);
			out.writeInt(p);
	
			// the same nonce is being used for both scrypt and EAX encryption
			byte[] nonce = new byte[NONCE_SIZE_BYTES];
			random.nextBytes(nonce);
			out.write(nonce);
		
			byte[] pw = null;
			byte[] salt = nonce; // reuse nonce as salt

			try
			{
				pw = (pass == null ? new byte[0] : pass.getBytes());
				
				// generate key with scrypt
				// P - passphrase
				// S - salt
				// N - cpu/memory cost
				// r - block mix size parameter
				// p - parallelization parameter
				byte[] key = SCrypt.generate(pw, salt, n, r, p, KEY_SIZE_BYTES);
				
				try
				{					
					EAXEncryptStream es = new EAXEncryptStream(key, nonce, null, out);
					try
					{
						if(data == null)
						{
							// this should produce a key file of the same size as one with the actual key
							writeInt(es, -1);
							es.write(new byte[KEY_SIZE_BYTES]);
						}
						else
						{
							byte[] payload = data.getBytes();
							try
							{
								writeInt(es, payload.length);
								es.write(payload);
							}
							finally
							{
								Crypto.zero(payload);
							}
						}
					}
					finally
					{
						CKit.close(es);
					}
				}
				finally
				{
					Crypto.zero(key);
				}
			}
			finally
			{
				Crypto.zero(pw);
			}
		}
		finally
		{
			CKit.close(out);
		}
		
		return bout.toByteArray();
	}
	
	
	/**
	 * Attempts to decrypt the supplied byte array using the specified passphrase.
	 * Returns the decrypted data or throws an exception. 
	 */
	public static final OpaqueBytes decrypt(byte[] encrypted, OpaqueChars pass) throws Exception
	{
		if(pass == null)
		{
			return null;
		}
		else if(encrypted == null)
		{
			return null;
		}
		else if(encrypted.length == 0)
		{
			return null;
		}
		
		DReader rd = new DReader(encrypted);
		try
		{			
			// header
			long ver = rd.readLong();
			if(ver != SIGNATURE_V1)
			{
				throw new Exception(ERROR_WRONG_SIGNATURE);
			}
			
			int n = rd.readInt();
			check(n, 1, Integer.MAX_VALUE, ERROR_INVALID_FORMAT);
			
			int r = rd.readInt();
			check(r, 1, Integer.MAX_VALUE, ERROR_INVALID_FORMAT);
			
			int p = rd.readInt();
			check(p, 1, Integer.MAX_VALUE, ERROR_INVALID_FORMAT);
			
			byte[] nonce = new byte[NONCE_SIZE_BYTES];
			CKit.readFully(rd, nonce);

			// reuse nonce as salt
			byte[] salt = nonce; // TODO perhaps invert or hash, just in case

			byte[] pw = null;
			try
			{
				// generate key with scrypt
				// P - passphrase
				// S - salt
				// N - cpu/memory cost
				// r - block mix size parameter
				// p - parallelization parameter
				pw = pass.getBytes();
				
				byte[] key = SCrypt.generate(pw, salt, n, r, p, KEY_SIZE_BYTES);
				try
				{
					EAXDecryptStream ds = new EAXDecryptStream(key, nonce, null, rd);
					try
					{
						int len = readInt(ds);
						if(len < 0)
						{
							// no key
							return new OpaqueBytes();
						}
						
						check(len, 0, Integer.MAX_VALUE, ERROR_INVALID_FORMAT);
						
						byte[] decrypted = new byte[len];
						try
						{
							CKit.readFully(ds, decrypted);
							return new OpaqueBytes(decrypted);
						}
						finally
						{
							Crypto.zero(decrypted);
						}
					}
					finally
					{
						CKit.close(ds);
					}
				}
				finally
				{
					Crypto.zero(key);
				}
			}
			finally
			{
				Crypto.zero(pw);
			}
		}
		finally
		{
			CKit.close(rd);
		}
	}
	

	private static void check(int value, int min, int max, String err) throws Exception
	{
		if(value < min)
		{
			throw new Exception(err);
		}
		else if(value > max)
		{
			throw new Exception(err);
		}
	}
	
	
	private static int readInt(InputStream in) throws Exception
	{
		int d = (readByte(in) << 24);
		d |= (readByte(in) << 16);
		d |= (readByte(in) << 8);
		d |= readByte(in);
		return d;
	}
	
	
	private static int readByte(InputStream in) throws Exception
	{
		int c = in.read();
		if(c < 0)
		{
			throw new EOFException();
		}
		return c & 0xff;
	}
	
	
	private static void writeInt(OutputStream out, int d) throws Exception
	{
		out.write(d >>> 24);
		out.write(d >>> 16);
		out.write(d >>>  8);
		out.write(d);
	}
}
