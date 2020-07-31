// Copyright © 2019-2020 Andy Goryachev <andy@goryachev.com>
package goryachev.secdb.segmented;
import goryachev.common.io.DReader;
import goryachev.common.io.DWriter;
import goryachev.common.log.Log;
import goryachev.common.util.CFileLock;
import goryachev.common.util.CKit;
import goryachev.common.util.CMap;
import goryachev.common.util.GUID256;
import goryachev.common.util.Hex;
import goryachev.crypto.OpaqueBytes;
import goryachev.crypto.OpaqueChars;
import goryachev.secdb.IStore;
import goryachev.secdb.IStream;
import goryachev.secdb.crypto.KeyFile;
import goryachev.secdb.segmented.log.LogEventCode;
import goryachev.secdb.segmented.log.LogFile;
import goryachev.secdb.util.Utils;
import java.io.BufferedInputStream;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.SecureRandom;
import java.util.List;


/**
 * Encrypted Local Segmented Store.
 * 
 * - stores encrypted blocks in append-only files named segments
 * - (long) data values are encrypted with a random key to enable sharing
 */
public class SecStore
	implements Closeable,IStore<Ref>
{
	protected static final String LOCK_FILE = "lock";
	protected static final String KEY_FILE = "key";
	protected static final int BUFFER_SIZE = 4096;
	protected static final Log log = Log.get("SecStore");
	private final File dir;
	private final CFileLock lock;
	private final LogFile logFile;
	private final OpaqueBytes key;
	protected final EncHelper encHelper;
	private final CMap<String,SegmentFile> segments = new CMap();
	private SegmentFile currentSegment;
	private Ref root;
	
	
	public SecStore(File dir, CFileLock lock, LogFile logFile, Ref root, OpaqueBytes key)
	{
		this.dir = dir;
		this.logFile = logFile;
		this.root = root;
		this.lock = lock;
		this.key = key;
		this.encHelper = EncHelper.create(key);
	}
	
	
	/** checks the directory for database files, returns true if all required files are present. */
	public static boolean isPresent(File dir)
	{
		if(!dir.exists())
		{
			return false;
		}
		
		if(!dir.isDirectory())
		{
			return false;
		}
		
		File keyFile = getKeyFile(dir);
		if(!keyFile.exists())
		{
			return false;
		}
		
		return true;
	}
	
	
	/** might throw SecException which contains error code and additional information */
	public static void create(File dir, OpaqueBytes key, OpaqueChars passphrase, SecureRandom random) throws SecException, Exception
	{
		if(!dir.exists())
		{
			dir.mkdirs();
			if(!dir.exists())
			{
				throw new SecException(SecErrorCode.DIR_UNABLE_TO_CREATE, dir);
			}
		}
		
		if(dir.exists())
		{
			if(!Utils.isEmptyDir(dir))
			{
				throw new SecException(SecErrorCode.DIR_NOT_EMPTY, dir);
			}
		}

		// encrypt the main key
		byte[] encryptedKey;
		if((key == null) && (passphrase == null) && (random == null))
		{
			encryptedKey = new byte[0];
		}
		else
		{
			encryptedKey = KeyFile.encrypt(key, passphrase, random);
		}
		
		// store key file
		File keyFile = getKeyFile(dir);
		try
		{
			boolean created = keyFile.createNewFile();
			if(!created)
			{
				throw new Exception("failed to create " + keyFile);
			}
			// this is madness
			keyFile.setReadable(false, false);
			keyFile.setReadable(true, true);
			keyFile.setWritable(false, false);
			keyFile.setWritable(true, true);
			
			// TODO verify permissions
						
			CKit.write(encryptedKey, keyFile);
			
			// TODO verify permissions
			
			byte[] chk = CKit.readBytes(keyFile);
			if(CKit.notEquals(encryptedKey, chk))
			{
				throw new Exception("key file content mismatch");
			}
		}
		catch(SecException e)
		{
			throw e;
		}
		catch(Throwable e)
		{
			throw new SecException(SecErrorCode.FAILED_KEY_FILE_WRITE, e);
		}
		
		// TODO generate log key
		OpaqueBytes logKey = null;
		// TODO write key --> exception if unable
		
		// write log
		LogFile lf = LogFile.create(dir, logKey);
		lf.appendEvent(LogEventCode.HEAD, null);
		lf.appendEvent(LogEventCode.CLOSED);
	}
	
	
	public void checkPassword(OpaqueChars passphrase) throws Exception, SecException
	{
		OpaqueBytes b = decryptKey(dir, passphrase);
		b.clear();
	}
	
	
	private static OpaqueBytes decryptKey(File dir, OpaqueChars passphrase) throws SecException,Exception
	{
		byte[] encryptedKey;
		try
		{
			encryptedKey = CKit.readBytes(getKeyFile(dir));
		}
		catch(Throwable e)
		{
			throw new SecException(SecErrorCode.FAILED_KEY_FILE_READ, e);
		}
		
		return KeyFile.decrypt(encryptedKey, passphrase);
	}


	public static SecStore open(File dir, OpaqueChars passphrase) throws SecException,Exception
	{
		// check directories
		if(!dir.exists() || !dir.isDirectory())
		{
			throw new SecException(SecErrorCode.DIR_NOT_FOUND, dir);
		}
		
		CFileLock lock = new CFileLock(new File(dir, LOCK_FILE));
		lock.checkLock();
		
		try
		{
			// decrypt key -> missing key file, passphrase error
			OpaqueBytes key = decryptKey(dir, passphrase);
			
			// TODO think of a way to derive log key from the main key
			OpaqueBytes logKey = null;
			
			// read all logs
			// check if recovery is needed
			//   (perform recovery)
			// check version?
			List<LogFile> lfs = LogFile.open(dir, logKey);
			if(lfs.size() == 0)
			{
				throw new SecException(SecErrorCode.MISSING_LOG_FILE, dir);
			}
			
			LogFile lf = lfs.get(0);
			
			// TODO two or more files means unsuccessfull recovery
			// TODO check if recovery is needed
			if(lf.isRecoveryNeeded())
			{
				// TODO recover
				throw new SecException(SecErrorCode.RECOVERY_REQUIRED, lf.getName());
			}
			
			// TODO
			// get STATE event and initialize segment files
			
			// read root ref
			Ref root = lf.getRootRef();
			
			// TODO
			// load root node and do some checks
			
			return new SecStore(dir, lock, lf, root, key);
		}
		catch(Throwable e)
		{
			lock.unlock();
			throw e;
		}
	}


	// FIX does not close all the files (readers)
	public void close() throws IOException
	{
		// TODO
		// zero out the main key
		// clear all buffers
		
		try
		{
			if(currentSegment != null)
			{
				currentSegment.closeWriter();
			}
		}
		catch(IOException e)
		{
			throw e;
		}
		catch(Exception e)
		{
			throw new IOException(e);
		}
		
		boolean failedToCloseReaders = false;
		synchronized(segments)
		{
			for(SegmentFile sf: segments.values())
			{
				try
				{
					sf.closeReader();
				}
				catch(Throwable e)
				{
					failedToCloseReaders = true;
				}
			}
		}
		
		if(failedToCloseReaders)
		{
			throw new IOException("Failed to close readers");
		}
		
		try
		{
			logFile.appendEvent(LogEventCode.CLOSED);
		}
		catch(IOException e)
		{
			throw e;
		}
		catch(Exception e)
		{
			throw new IOException(e);
		}
		
		try
		{
			logFile.close();
		}
		catch(IOException e)
		{
			throw e;
		}
		catch(Exception e)
		{
			throw new IOException(e);
		}
		
		lock.unlock();
	}
	
	
	public long convertLength(long len, boolean whenEncrypting)
	{
		return encHelper.convertLength(len, whenEncrypting);
	}


	public Ref getRootRef()
	{
		return root;
	}


	public void setRootRef(Ref ref) throws Exception
	{
		// TODO synchronize?
		// TODO log HEAD
		root = ref;
		
		logFile.appendEvent(LogEventCode.HEAD, ref);
	}


	// TODO mutex
	public Ref store(IStream inp, boolean isTree) throws Exception
	{
		// if isTree, use the main key
		// if !isTree, generate a random data key
		byte[] key = null; // TODO
		
		// TODO nonce = segment(*1) + offset + segment count
		// *1: multi-segment blocks use the first segment name for nonce
		
		long len = inp.getLength();
		if(len <= 0)
		{
			throw new Error("invalid data length=" + len);
		}
		
		long storedLength = convertLength(len, true);
		
		InputStream in = inp.getStream();
				
		SegmentOutputStream ss = new SegmentOutputStream(this, storedLength, isTree, key);
		
		Ref ref = ss.getInitialRef();
		byte[] nonce = createNonce(ref);

		OutputStream out = encHelper.getEncryptionStream(nonce, ss);
		try
		{
			CKit.copy(in, out, BUFFER_SIZE);
		}
		finally
		{
			CKit.close(in);
			CKit.close(out);
		}
		
		Ref rv = ss.getRef();
		log.trace("STORE len=%d ref=%s", len,  rv);
		return rv;
	}
	
	
	protected File toSegmentFile(String name)
	{
		if(name.length() < 64)
		{
			throw new Error("illegal segment file name: " + name);
		}
		
		String subdir = name.substring(0, 2);
		return new File(dir, subdir + "/" + name);
	}
	
	
	protected static File getKeyFile(File dir)
	{
		return new File(dir, KEY_FILE);
	}
	
	
	protected SegmentFile newSegmentFile()
	{
		String name = GUID256.generateHexString();
		File f = toSegmentFile(name);
		SegmentFile sf =  new SegmentFile(f, name);

		synchronized(segments)
		{
			segments.put(name, sf);
		}
		return sf;
	}
	
	
	protected SegmentFile getSegmentFile(String name)
	{
		synchronized(segments)
		{
			SegmentFile sf = segments.get(name);
			if(sf == null)
			{
				File f = toSegmentFile(name);
				sf = new SegmentFile(f, name);
				segments.put(name, sf);
			}
			return sf;
		}
	}


	protected SegmentFile segmentForLength(long length, boolean isTree) throws Exception
	{
		if(currentSegment == null)
		{
			if(root == null)
			{
				currentSegment = newSegmentFile();
			}
			else
			{
				String name = root.getSegment(root.getSegmentCount() - 1);
				currentSegment = getSegmentFile(name);
				if(currentSegment == null)
				{
					throw new SecException(SecErrorCode.MISSING_SEGMENT_FILE, name);
				}
			}
		}
		
		if(currentSegment.getLength() >= SegmentFile.SEGMENT_SIZE)
		{
			currentSegment.closeWriter();
			currentSegment = newSegmentFile();
		}
		
		return currentSegment;
	}


	public IStream load(Ref ref) throws Exception
	{
		// TODO need to explicitly clear the ref (because of the data key)
		
		long len = ref.getLength();
		long dataLength = convertLength(len, false);
		
		return new IStream()
		{
			@SuppressWarnings("resource")
			public InputStream getStream()
			{
				InputStream in = new SegmentInputStream(SecStore.this, ref);
				byte[] nonce = createNonce(ref);
				in = encHelper.getDecryptionStream(nonce, in);
				return new BufferedInputStream(in, BUFFER_SIZE);
			}

			public long getLength()
			{
				return dataLength;
			}
		};
	}


	public void writeRef(Ref ref, DWriter wr) throws Exception
	{
		ref.write(wr);
	}


	public Ref readRef(DReader rd) throws Exception
	{
		return Ref.read(rd);
	}
	
	
	protected static File getSegmentDir(File dir, int x)
	{
		return new File(dir, Hex.toHexByte(x));
	}
	
	
	protected static byte[] createNonce(Ref ref)
	{
		// TODO name + offset of the first segment
		return new byte[0];
	}
}
