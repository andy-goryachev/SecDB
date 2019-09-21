// Copyright © 2019 Andy Goryachev <andy@goryachev.com>
package goryachev.secdb;
import java.util.Arrays;


/**
 * Encryption Provider Interface.
 */
public interface IEncryptor
{
	public static final IEncryptor NONE = new None();

	public byte[] decrypt(byte[] b);
	
	public byte[] encrypt(byte[] b);
	
	public void zero(byte[] dec);
	
	
	//
	
	
	/** provides no encryption */
	public final class None implements IEncryptor
	{
		public byte[] decrypt(byte[] b)
		{
			return b;
		}

		
		public byte[] encrypt(byte[] b)
		{
			return b;
		}
		

		public void zero(byte[] b)
		{
			if(b != null)
			{
				Arrays.fill(b, (byte)0);
			}
		}
	}



}
