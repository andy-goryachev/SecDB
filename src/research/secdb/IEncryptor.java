// Copyright © 2019 Andy Goryachev <andy@goryachev.com>
package research.secdb;


/**
 * Encryption Provider Interface.
 */
public interface IEncryptor
{
	public static final IEncryptor NONE = new IEncryptor()
	{
	};
}
