// Copyright © 2021 Andy Goryachev <andy@goryachev.com>
package research.xsalsa20poly1305;
import java.security.MessageDigest;
import java.util.Optional;
import org.bouncycastle.crypto.engines.XSalsa20Engine;
import org.bouncycastle.crypto.macs.Poly1305;
import org.bouncycastle.crypto.params.KeyParameter;
import org.bouncycastle.crypto.params.ParametersWithIV;


/**
 * TestEnc.
 */
public class TestEnc
{
	private static final int KEY_LEN = 32;
	private static final int NONCE_SIZE = 24;
	
	
	public Optional<byte[]> decrypt(byte[] key, byte[] nonce, byte[] ciphertext)
	{
		final XSalsa20Engine xsalsa20 = new XSalsa20Engine();
		final Poly1305 poly1305 = new Poly1305();

		// initialize XSalsa20
		xsalsa20.init(false, new ParametersWithIV(new KeyParameter(key), nonce));

		// generate mac subkey
		final byte[] sk = new byte[KEY_LEN];
		xsalsa20.processBytes(sk, 0, sk.length, sk, 0);

		// hash ciphertext
		poly1305.init(new KeyParameter(sk));
		final int len = Math.max(ciphertext.length - poly1305.getMacSize(), 0);
		poly1305.update(ciphertext, poly1305.getMacSize(), len);
		final byte[] calculatedMAC = new byte[poly1305.getMacSize()];
		poly1305.doFinal(calculatedMAC, 0);

		// extract mac
		final byte[] presentedMAC = new byte[poly1305.getMacSize()];
		System.arraycopy(ciphertext, 0, presentedMAC, 0, Math.min(ciphertext.length, poly1305.getMacSize()));

		// compare macs
		if(!MessageDigest.isEqual(calculatedMAC, presentedMAC))
		{
			return Optional.empty();
		}

		// decrypt ciphertext
		final byte[] plaintext = new byte[len];
		xsalsa20.processBytes(ciphertext, poly1305.getMacSize(), plaintext.length, plaintext, 0);
		return Optional.of(plaintext);
	}


	/**
	 * Encrypt a plaintext using the given key and nonce.
	 *
	 * @param nonce a 24-byte nonce (cf. {@link #nonce(byte[])}, {@link #nonce()})
	 * @param plaintext an arbitrary message
	 * @return the ciphertext
	 */
	public byte[] encrypt(byte[] key, byte[] nonce, byte[] plaintext)
	{
		final XSalsa20Engine xsalsa20 = new XSalsa20Engine();
		final Poly1305 poly1305 = new Poly1305();

		// initialize XSalsa20
		xsalsa20.init(true, new ParametersWithIV(new KeyParameter(key), nonce));

		// generate Poly1305 subkey
		final byte[] sk = new byte[KEY_LEN];
		xsalsa20.processBytes(sk, 0, KEY_LEN, sk, 0);

		// encrypt plaintext
		final byte[] out = new byte[plaintext.length + poly1305.getMacSize()];
		xsalsa20.processBytes(plaintext, 0, plaintext.length, out, poly1305.getMacSize());

		// hash ciphertext and prepend mac to ciphertext
		poly1305.init(new KeyParameter(sk));
		poly1305.update(out, poly1305.getMacSize(), plaintext.length);
		poly1305.doFinal(out, 0);

		return out;
	}
}
