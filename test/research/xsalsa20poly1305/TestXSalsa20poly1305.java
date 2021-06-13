package research.xsalsa20poly1305;
import goryachev.common.test.TF;
import goryachev.common.test.Test;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import com.codahale.xsalsa20poly1305.Keys;
import com.codahale.xsalsa20poly1305.SimpleBox;


// https://github.com/codahale/xsalsa20poly1305
public class TestXSalsa20poly1305
{
	public static void main(String[] args)
	{
		TF.run();
	}
	
	
	@Test
	public void asymmetricEncryption()
	{
		// Alice has a key pair
		final byte[] alicePrivateKey = Keys.generatePrivateKey();
		final byte[] alicePublicKey = Keys.generatePublicKey(alicePrivateKey);

		// Bob also has a key pair
		final byte[] bobPrivateKey = Keys.generatePrivateKey();
		final byte[] bobPublicKey = Keys.generatePublicKey(bobPrivateKey);

		// Bob and Alice exchange public keys. (Not pictured.)

		// Bob wants to send Alice a very secret message. 
		final byte[] message = "this is very secret".getBytes(StandardCharsets.UTF_8);

		// Bob encrypts the message using Alice's public key and his own private key
		final SimpleBox bobBox = new SimpleBox(alicePublicKey, bobPrivateKey);
		final byte[] ciphertext = bobBox.seal(message);

		// Bob sends Alice this ciphertext. (Not pictured.)

		// Alice decrypts the message using Bob's public key and her own private key.
		final SimpleBox aliceBox = new SimpleBox(bobPublicKey, alicePrivateKey);
		final Optional<byte[]> plaintext = aliceBox.open(ciphertext);

		// Now Alice has the message!
		System.out.println(new String(plaintext.get(), StandardCharsets.UTF_8));
	}


	@Test
	public void symmetricEncryption()
	{
		// There is a single secret key.
		final byte[] secretKey = Keys.generateSecretKey();

		// And you want to use it to store a very secret message.
		final byte[] message = "this is very secret".getBytes(StandardCharsets.UTF_8);

		// So you encrypt it.
		final SimpleBox box = new SimpleBox(secretKey);
		final byte[] ciphertext = box.seal(message);

		// And you store it. (Not pictured.)

		// And then you decrypt it later.
		final Optional<byte[]> plaintext = box.open(ciphertext);

		// Now you have the message again!
		System.out.println(new String(plaintext.get(), StandardCharsets.UTF_8));
	}

	// There is also SecretBox, which behaves much like SimpleBox but requires you to manage your own
	// nonces. More on that later.
	
	
	/*
	Misuse-Resistant Nonces

	XSalsa20Poly1305 is composed of two cryptographic primitives: XSalsa20, a stream cipher, 
	and Poly1305, a message authentication code. In order to be secure, both require a nonce -- 
	a bit string which can only be used once for any given key. If a nonce is re-used -- i.e., 
	used to encrypt two different messages -- this can have catastrophic consequences for 
	the confidentiality and integrity of the encrypted messages: an attacker may be able to recover
	plaintext messages and even forge seemingly-valid messages. 
	As a result, it is incredibly important that nonces be unique.

	XSalsa20 uses 24-byte (192-bit) nonces, which makes the possibility of a secure random number generator 
	generating the same nonce twice essentially impossible, even over trillions of messages. For normal operations, 
	SecretBox#nonce() (which simply returns 24 bytes from SecureRandom) should be safe to use. But because of the 
	downside risk of nonce misuse, this library provides a secondary function for generating misuse-resistant nonces: 
	SecretBox#nonce(), which requires the message the nonce will be used to encrypt.

	SecretBox#nonce(byte[]) uses the BLAKE2b hash algorithm, keyed with the given key and using randomly-generated 
	128-bit salt and personalization parameters. If the local SecureRandom implementation is functional, the hash algorithm 
	mixes those 256 bits of entropy along with the key and message to produce a 192-bit nonce, which will have 
	the same chance of collision as SecretBox#nonce(). In the event that the local SecureRandom implementation 
	is misconfigured, exhausted of entropy, or otherwise compromised, the generated nonce will be unique 
	to the given combination of key and message, thereby preserving the security of the messages. 
	Please note that in this event, using SecretBox#nonce() to encrypt messages will be deterministic -- 
	duplicate messages will produce duplicate ciphertexts, and this will be observable to any attackers.

	Because of the catastrophic downside risk of nonce reuse, the SimpleBox functions use SecretBox#nonce(byte[]) to generate nonces.
	*/
}