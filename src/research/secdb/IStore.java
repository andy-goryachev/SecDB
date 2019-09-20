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
	
	public void setRootRef(R ref) throws Exception;
	
	public R store(IStream in, boolean isTree) throws Exception;
	
	public IStream load(R ref) throws Exception;
}
