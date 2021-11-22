// Copyright © 2021 Andy Goryachev <andy@goryachev.com>
package goryachev.secdb.segmented;
import goryachev.memsafecrypto.CByteArray;
import goryachev.memsafecrypto.salsa.XSalsaTools;
import goryachev.memsafecrypto.util.CUtils;
import java.security.SecureRandom;


/**
 * TUtils.
 */
public class TUtils
{
	public static CByteArray generateKey()
	{
		SecureRandom rnd = new SecureRandom();
		CByteArray b = new CByteArray(XSalsaTools.KEY_LENGTH_BYTES);
		CUtils.nextBytes(rnd, b);
		return b;
	}
}
