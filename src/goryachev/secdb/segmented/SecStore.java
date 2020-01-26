// Copyright © 2019-2020 Andy Goryachev <andy@goryachev.com>
package goryachev.secdb.segmented;
import goryachev.common.io.DReader;
import goryachev.common.io.DWriter;
import goryachev.common.util.CFileLock;
import goryachev.common.util.CMap;
import goryachev.common.util.Hex;
import goryachev.secdb.IStore;
import goryachev.secdb.IStream;
import goryachev.secdb.segmented.log.LogEventCode;
import goryachev.secdb.segmented.log.LogFile;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
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
	private final File dir;
	private final CFileLock lock;
	private final LogFile logFile;
	private final CMap<String,SegmentFile> segments = new CMap();
	private SegmentFile treeSegment;
	private SegmentFile dataSegment;
	private Ref root;
	
	
	public SecStore(File dir, CFileLock lock, LogFile logFile, Ref root)
	{
		this.dir = dir;
		this.logFile = logFile;
		this.root = root;
		this.lock = lock;
	}
	
	
	/** likely to throw DbException which contains error code and additional information */
	public static void create(File dir, char[] passphrase) throws Exception
	{
		if(!dir.exists())
		{
			dir.mkdirs();
			if(!dir.exists())
			{
				throw new DBException(DBErrorCode.DIR_UNABLE_TO_CREATE, dir);
			}
		}
		
		if(dir.exists())
		{
			if(!Utils.isEmptyDir(dir))
			{
				throw new DBException(DBErrorCode.DIR_NOT_EMPTY, dir);
			}
		}
		
		// TODO 16 dirs?  on demand?
//		// create dirs
//		for(int i=0; i<0x100; i++)
//		{
//			File d = getSegmentDir(dir, i);
//			d.mkdir();
//		}
//		
//		// check dirs
//		for(int i=0; i<0x100; i++)
//		{
//			File d = getSegmentDir(dir, i);
//			if(!d.isDirectory() || !d.exists())
//			{
//				throw new DBException(DBErrorCode.DIR_UNABLE_TO_CREATE, d);
//			}
//		}
		
		// TODO encrypt key
		// TODO generate log key
		byte[] logKey = null;
		// TODO write key --> exception if unable
		
		// TODO write log
		LogFile lf = LogFile.create(dir, logKey);
		lf.appendEvent(LogEventCode.HEAD, null);
		lf.appendEvent(LogEventCode.CLOSED);
	}
	
	
	// TODO OpaqueString
	public static SecStore open(File dir, char[] passphrase) throws Exception
	{
		// check directories
		if(!dir.exists() || !dir.isDirectory())
		{
			throw new DBException(DBErrorCode.DIR_NOT_FOUND, dir);
		}
		
		CFileLock lock = new CFileLock(new File(dir, LOCK_FILE));
		lock.checkLock();
		try
		{
			// TODO
			// decrypt key -> missing key file, passphrase error
			
			
			// read all logs
			// check if recovery is needed
			//   (perform recovery)
			// check version?
			List<LogFile> lfs = LogFile.open(dir, null);
			if(lfs.size() == 0)
			{
				throw new DBException(DBErrorCode.MISSING_LOG_FILE, dir);
			}
			
			// TODO two or more files means unsuccessfull recovery
			// TODO check if recovery is needed
	//		if(lf.isRecoveryNeeded())
	//		{
	//			// TODO recover
	//		}
	//		else
	//		{
	//			// TODO is recovery is not needed, create a new log file and delete all	
	//		}
			
			LogFile lf = lfs.get(0);
			
			// TODO 
			
			// write new log
			//lf.appendEvent(new LogEvent(LogEventCode.OPENED));
			
			// read root ref
			Ref root = lf.getRootRef();
			
			// TODO
			// load root node and do some checks
			
			return new SecStore(dir, lock, lf, root);
		}
		catch(Exception e)
		{
			lock.unlock();
			throw e;
		}
	}


	public void close() throws IOException
	{
		// TODO
		// zero out the main key
		// clear all buffers
		
		lock.unlock();
	}


	public Ref getRootRef()
	{
		return root;
	}


	public void setRootRef(Ref ref) throws Exception
	{
		// TODO synchronize
		root = ref;
	}


	public Ref store(IStream in, boolean isTree) throws Exception
	{
		InputStream is = in.getStream();
		
		// if isTree, use the main key
		// if !isTree, generate a random data key
		byte[] key = null; // TODO
		// TODO nonce = segment + offset

		long len = in.getLength();
		Ref ref = null;
		
		for(;;)
		{
			SegmentFile sf = segmentForLength(len, isTree);
			String name = sf.getName();
			long off = sf.getLength();
			
			if(ref == null)
			{
				ref = new Ref.Single(name, off, len, key);
			}
			else
			{
				ref = ref.addSegment(name, off);
			}

			len = sf.write(is, key); 
			if(len == 0)
			{
				return ref;
			}
		}
	}


	protected SegmentFile segmentForLength(long length, boolean isTree)
	{
		return null;
	}


	public IStream load(Ref ref) throws Exception
	{
		String seg = ref.getSegment();
		long off = ref.getOffset();
		long len = ref.getLength();
		// TODO data key
		// TODO need to explicitly clear the ref (because of the data key)
		
		// TODO
		throw new Error();
	}


	public void writeRef(Ref ref, DWriter wr) throws Exception
	{
		// TODO delegate to Ref
		wr.writeString(ref.getSegment());
		wr.writeLong(ref.getOffset());
		wr.writeLong(ref.getLength());
		wr.writeByteArray(ref.getDataKey());
	}


	public Ref readRef(DReader rd) throws Exception
	{
		// TODO multiple segments
		// TODO reuse text version maybe?
		String segment = rd.readString();
		long offset = rd.readLong();
		long length = rd.readLong();
		byte[] dataKey = rd.readByteArray(1024);
		
		return new Ref.Single(segment, offset, length, dataKey);
	}
	
	
	protected static File getSegmentDir(File dir, int x)
	{
		return new File(dir, Hex.toHexByte(x));
	}
}
