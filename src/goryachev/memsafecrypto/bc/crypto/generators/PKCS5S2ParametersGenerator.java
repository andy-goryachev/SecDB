package goryachev.memsafecrypto.bc.crypto.generators;
import goryachev.memsafecrypto.CByteArray;
import goryachev.memsafecrypto.bc.crypto.CipherParameters;
import goryachev.memsafecrypto.bc.crypto.Digest;
import goryachev.memsafecrypto.bc.crypto.Mac;
import goryachev.memsafecrypto.bc.crypto.PBEParametersGenerator;
import goryachev.memsafecrypto.bc.crypto.macs.HMac;
import goryachev.memsafecrypto.bc.crypto.params.KeyParameter;
import goryachev.memsafecrypto.bc.crypto.params.ParametersWithIV;
import goryachev.memsafecrypto.util.CUtils;


/**
 * Generator for PBE derived keys and ivs as defined by PKCS 5 V2.0 Scheme 2.
 * This generator uses a SHA-1 HMac as the calculation function.
 * <p>
 * The document this implementation is based on can be found at
 * <a href=https://www.rsasecurity.com/rsalabs/pkcs/pkcs-5/index.html>
 * RSA's PKCS5 Page</a>
 */
public class PKCS5S2ParametersGenerator
	extends PBEParametersGenerator
{
	private Mac hMac;
	private CByteArray state;


	public PKCS5S2ParametersGenerator(Digest digest)
	{
		hMac = new HMac(digest);
		state = new CByteArray(hMac.getMacSize());
	}


	private void F(CByteArray S, int c, CByteArray iBuf, CByteArray out, int outOff)
	{
		if(c == 0)
		{
			throw new IllegalArgumentException("iteration count must be at least 1.");
		}

		if(S != null)
		{
			hMac.update(S, 0, S.length());
		}

		hMac.update(iBuf, 0, iBuf.length());
		hMac.doFinal(state, 0);

		CUtils.arraycopy(state, 0, out, outOff, state.length());

		for(int count=1; count< c; count++)
		{
			hMac.update(state, 0, state.length());
			hMac.doFinal(state, 0);

			for(int j=0; j!=state.length(); j++)
			{
				out.xor(outOff + j, state.get(j));
			}
		}
	}


	/** 
	 * @param dkLen - key length in bytes 
	 */
	public CByteArray generateDerivedKey(int dkLen)
	{
		int hLen = hMac.getMacSize();
		int l = (dkLen + hLen - 1) / hLen;
		CByteArray iBuf = new CByteArray(4);
		try
		{
			CByteArray outBytes = new CByteArray(l * hLen);
			int outPos = 0;
	
			CipherParameters param = new KeyParameter(password);
	
			hMac.init(param);
	
			for(int i=1; i<=l; i++)
			{
				// Increment the value in 'iBuf'
				int pos = 3;
				//while(++iBuf[pos] == 0)
				while(iBuf.incrementAndGet(pos) == 0)
				{
					--pos;
				}
	
				F(salt, iterationCount, iBuf, outBytes, outPos);
				outPos += hLen;
			}
			
			return outBytes;
		}
		finally
		{
			iBuf.zero();
		}
	}


	/**
	 * Generate a key parameter derived from the password, salt, and iteration
	 * count we are currently initialised with.
	 *
	 * @param keySize the size of the key we want (in bits)
	 * @return a KeyParameter object.
	 */
	public CipherParameters generateDerivedParameters(int keySize)
	{
		keySize = keySize / 8;

		CByteArray dKey = generateDerivedKey(keySize);
		try
		{
			return new KeyParameter(dKey, 0, keySize);
		}
		finally
		{
			dKey.zero();
		}
	}


	/**
	 * Generate a key with initialisation vector parameter derived from
	 * the password, salt, and iteration count we are currently initialised
	 * with.
	 *
	 * @param keySize the size of the key we want (in bits)
	 * @param ivSize the size of the iv we want (in bits)
	 * @return a ParametersWithIV object.
	 */
	public CipherParameters generateDerivedParameters(int keySize, int ivSize)
	{
		keySize = keySize / 8;
		ivSize = ivSize / 8;

		CByteArray dKey = generateDerivedKey(keySize + ivSize);
		try
		{
			return new ParametersWithIV(new KeyParameter(dKey, 0, keySize), dKey, keySize, ivSize);
		}
		finally
		{
			dKey.zero();
		}
	}


	/**
	 * Generate a key parameter for use with a MAC derived from the password,
	 * salt, and iteration count we are currently initialised with.
	 *
	 * @param keySize the size of the key we want (in bits)
	 * @return a KeyParameter object.
	 */
	public CipherParameters generateDerivedMacParameters(int keySize)
	{
		return generateDerivedParameters(keySize);
	}
}
