// Copyright © 2019-2020 Andy Goryachev <andy@goryachev.com>
package goryachev.secdb;
import goryachev.common.io.DReader;
import goryachev.common.io.DWriter;


/**
 * Low Level Storage Interface.
 * 
 * Stores variable size blocks, represented by IStream objects.
 */
public interface IStore<R>
{
	/** 
	 * returns the tree root reference or null.
	 * must be thread safe in regards to other operations such as 
	 * reading, storing, or setRootRef().
	 */
	public R getRootRef();
	
	/** 
	 * updates the tree root reference.
	 * any subsequent calls to getRootRef() must return the new value.
	 */
	public void setRootRef(R ref) throws Exception;
	
	/**
	 * stores a block.
	 * isTree tells whether the block is a part of the tree,
	 * as opposed to data value.
	 * The store may also take into account IStream.getLength().
	 */
	public R store(IStream in, boolean isTree) throws Exception;
	
	/**
	 * returns the stream that allows to read a block.
	 * The store must support multiple threads reading different or same
	 * blocks at the same time.
	 */
	public IStream load(R ref) throws Exception;
	
	
	public void writeRef(R ref, DWriter wr) throws Exception;

	
	public R readRef(DReader rd) throws Exception;
}
