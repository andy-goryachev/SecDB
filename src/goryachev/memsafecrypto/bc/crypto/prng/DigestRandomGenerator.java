package goryachev.memsafecrypto.bc.crypto.prng;
import goryachev.memsafecrypto.CByteArray;
import goryachev.memsafecrypto.bc.crypto.Digest;
import goryachev.memsafecrypto.util.CUtils;


/**
 * Random generation based on the digest with counter. Calling addSeedMaterial will
 * always increase the entropy of the hash.
 * <p>
 * Internal access to the digest is synchronized so a single one of these can be shared.
 * </p>
 */
public class DigestRandomGenerator
	implements RandomGenerator
{
	private static long CYCLE_COUNT = 10;

	private long stateCounter;
	private long seedCounter;
	private Digest digest;
	private CByteArray state;
	private CByteArray seed;


	public DigestRandomGenerator(Digest digest)
	{
		this.digest = digest;

		this.seed = new CByteArray(digest.getDigestSize());
		this.seedCounter = 1;

		this.state = new CByteArray(digest.getDigestSize());
		this.stateCounter = 1;
	}


	public void addSeedMaterial(byte[] inSeed)
	{
		synchronized(this)
		{
			if(!CUtils.isNullOrEmpty(inSeed))
			{
				digestUpdate(inSeed);
			}
			
			digestUpdate(seed);
			digestDoFinal(seed);
		}
	}


	public void addSeedMaterial(long rSeed)
	{
		synchronized(this)
		{
			digestAddCounter(rSeed);
			
			digestUpdate(seed);
			digestDoFinal(seed);
		}
	}


	public void nextBytes(byte[] bytes)
	{
		nextBytes(bytes, 0, bytes.length);
	}


	public void nextBytes(byte[] bytes, int start, int len)
	{
		synchronized(this)
		{
			int stateOff = 0;

			generateState();

			int end = start + len;
			for(int i=start; i<end; i++)
			{
				if(stateOff == state.length())
				{
					generateState();
					stateOff = 0;
				}
				bytes[i] = state.get(stateOff++);
			}
		}
	}
	
	
	public void nextBytes(CByteArray bytes)
	{
		nextBytes(bytes, 0, bytes.length());
	}


	public void nextBytes(CByteArray bytes, int start, int len)
	{
		synchronized(this)
		{
			int stateOff = 0;

			generateState();

			int end = start + len;
			for(int i=start; i<end; i++)
			{
				if(stateOff == state.length())
				{
					generateState();
					stateOff = 0;
				}
				bytes.set(i, state.get(stateOff++));
			}
		}
	}


	private void cycleSeed()
	{
		digestUpdate(seed);
		digestAddCounter(seedCounter++);

		digestDoFinal(seed);
	}


	private void generateState()
	{
		digestAddCounter(stateCounter++);
		digestUpdate(state);
		digestUpdate(seed);

		digestDoFinal(state);

		if((stateCounter % CYCLE_COUNT) == 0)
		{
			cycleSeed();
		}
	}


	private void digestAddCounter(long seed)
	{
		for(int i=0; i<8; i++)
		{
			digest.update((byte)seed);
			seed >>>= 8;
		}
	}
	
	
	private void digestUpdate(byte[] inSeed)
	{
		digest.update(inSeed, 0, inSeed.length);
	}


	private void digestUpdate(CByteArray inSeed)
	{
		digest.update(inSeed, 0, inSeed.length());
	}


	private void digestDoFinal(CByteArray result)
	{
		digest.doFinal(result, 0);
	}
}
