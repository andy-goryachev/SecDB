// Copyright © 2019-2022 Andy Goryachev <andy@goryachev.com>
package goryachev.secdb.segmented;
import goryachev.common.io.DReader;
import goryachev.common.io.DWriter;
import goryachev.common.log.Log;
import goryachev.common.util.CFileLock;
import goryachev.common.util.CKit;
import goryachev.common.util.CMap;
import goryachev.common.util.GUID;
import goryachev.common.util.Hex;
import goryachev.memsafecrypto.OpaqueBytes;
import goryachev.secdb.IStore;
import goryachev.secdb.IStream;
import goryachev.secdb.segmented.log.LogEventCode;
import goryachev.secdb.segmented.log.LogFile;
import goryachev.secdb.util.Utils;
import java.io.BufferedInputStream;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
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
	protected static final Log log = Log.get("SecStore");
	protected static final String LOCK_FILE = "lock";
	protected static final int BUFFER_SIZE = 4096;
	private static final int SEGMENT_FILE_LENGTH = 48;
	private final File dir;
	private final CFileLock lock;
	private final LogFile logFile;
	protected final IEncHelper encHelper;
	private final CMap<String,SegmentFile> segments = new CMap();
	private SegmentFile currentSegment;
	private Ref root;
	
	
	public SecStore(File dir, CFileLock lock, LogFile logFile, IEncHelper h, Ref root)
	{
		this.dir = dir;
		this.logFile = logFile;
		this.root = root;
		this.lock = lock;
		this.encHelper = h;
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
		
		return true;
	}
	
	
	/** might throw SecException which contains error code and additional information */
	public static void create(File dir, IEncHelper encHelper) throws SecException, Exception
	{
		// OpaqueBytes key, OpaqueChars passphrase
		
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

		// TODO generate log key
		OpaqueBytes logKey = null;
		// TODO write key --> exception if unable
		
		// write log
		LogFile lf = LogFile.create(dir, logKey);
		lf.appendEvent(LogEventCode.HEAD, null);
		lf.appendEvent(LogEventCode.CLOSED);
	}
	

	public static SecStore open(File dir, IEncHelper encHelper) throws SecException,Exception
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
			
			return new SecStore(dir, lock, lf, encHelper, root);
		}
		catch(Throwable e)
		{
			lock.unlock();
			throw e;
		}
	}


	public void close() throws IOException
	{
		Throwable err = null;
		
		try
		{
			if(currentSegment != null)
			{
				currentSegment.closeWriter();
			}
		}
		catch(Throwable e)
		{
			err = e;
		}
		
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
					err = (err == null ? e : new IOException(e));
				}
			}
		}
		
		try
		{
			logFile.appendEvent(LogEventCode.CLOSED);
		}
		catch(Throwable e)
		{
			err = (err == null ? e : new IOException(e));
		}
		
		try
		{
			logFile.close();
		}
		catch(Throwable e)
		{
			err = (err == null ? e : new IOException(e));
		}
		
		lock.unlock();
		
		if(err != null)
		{
			if(err instanceof IOException)
			{
				throw (IOException)err;
			}
			else
			{
				throw new IOException(err);
			}
		}
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
		long len = inp.getLength();
		if(len <= 0)
		{
			throw new Error("invalid data length=" + len);
		}
		
		long storedLength = convertLength(len, true);
		
		InputStream in = inp.getStream();
			
		SegmentOutputStream ss = new SegmentOutputStream(this, storedLength, isTree);
		Ref ref = ss.getInitialRef();
		String nonce = forNonce(ref);
		
		OutputStream out = encHelper.getEncryptionStream(nonce, storedLength, ss);
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
	
	
	protected SegmentFile newSegmentFile()
	{
		byte[] b = GUID.generate();
		String name = Hex.toHexString(b, 0, SEGMENT_FILE_LENGTH/2);
		File f = toSegmentFile(name);
		SegmentFile sf =  new SegmentFile(f, name);

		synchronized(segments)
		{
			segments.put(name, sf);
		}
		return sf;
	}
	
	
	protected File toSegmentFile(String name)
	{
		if(name.length() != SEGMENT_FILE_LENGTH)
		{
			throw new Error("illegal segment file name: " + name);
		}
		
		String subdir = name.substring(0, 2);
		return new File(dir, subdir + "/" + name);
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
				SegmentInputStream ss = new SegmentInputStream(SecStore.this, ref);
				String nonce = forNonce(ref);

				InputStream in = encHelper.getDecryptionStream(nonce, len, ss);
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
	
	
	protected static String forNonce(Ref r)
	{
		return r.getSegment(0) + "|" + r.getOffset(0); 
	}
	
	
	protected static File getSegmentDir(File dir, int x)
	{
		return new File(dir, Hex.toHexByte(x));
	}
}
