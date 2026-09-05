package goryachev.memsafecrypto.bc.crypto;
import goryachev.memsafecrypto.CByteArray;
import goryachev.memsafecrypto.CCharArray;


/**
 * Standard char[] to byte[] converters for password based derivation algorithms.
 */
public enum PasswordConverter
	implements CharToByteConverter
{
	/**
	 * Do a straight char[] to 8 bit conversion.
	 */
	ASCII
	{
		@Override
		public String getType()
		{
			return "ASCII";
		}


		@Override
		public CByteArray convert(CCharArray password)
		{
			return PBEParametersGenerator.PKCS5PasswordToBytes(password);
		}
	},
	/**
	 * Do a char[] conversion by producing UTF-8 data.
	 */
	UTF8
	{
		@Override
		public String getType()
		{
			return "UTF8";
		}


		@Override
		public CByteArray convert(CCharArray password)
		{
			return PBEParametersGenerator.PKCS5PasswordToUTF8Bytes(password);
		}
	},
	/**
	 * Do char[] to BMP conversion (i.e. 2 bytes per character).
	 */
	PKCS12
	{
		@Override
		public String getType()
		{
			return "PKCS12";
		}


		@Override
		public CByteArray convert(CCharArray password)
		{
			return PBEParametersGenerator.PKCS12PasswordToBytes(password);
		}
	};
}
