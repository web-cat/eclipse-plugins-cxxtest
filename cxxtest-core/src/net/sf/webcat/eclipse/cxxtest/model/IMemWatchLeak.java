package net.sf.webcat.eclipse.cxxtest.model;

public interface IMemWatchLeak
{
	String getAddress();
	
	int getSize();
	
	boolean isArray();

	/**
	 * Gets a stack trace representing the point of allocation of the memory
	 * that leaked, if available.
	 * 
	 * @return an array of IStackTraceEntry objects describing the
	 * stack trace.
	 */
	ICxxTestStackFrame[] getStackTrace();
}
