// Copyright © 2019 Andy Goryachev <andy@goryachev.com>
package research.secdb;
import java.io.IOException;


/**
 * Low Level Storage Interface.
 */
public interface IStore<R>
{
	/** returns the tree root reference or null */
	public R getRootRef();
	
	public R store(IStream in) throws Exception;
	
	public IStream load(R ref) throws Exception;
	
	public void open() throws Exception;
	
	public void close() throws IOException;
}
