package net.sf.webcat.eclipse.cxxtest.model;

public interface ICxxTestStackFrame
{
	/**
	 * Gets the name and signature of the function for this stack
	 * trace entry.
	 * 
	 * @return A String containing the name and signature of the
	 * function.
	 */
	String getFunction();
	
	/**
	 * Returns the name of the source file in which the function is
	 * located.
	 * 
	 * @return A String containing the name of the source file, or null
	 * if this information was not available.
	 */
	String getFile();
	
	/**
	 * Returns the line number of the function in the source file where
	 * it is located.
	 * 
	 * @return An integer representing the line number of the function,
	 * or 0 if this information was not available.
	 */
	int getLineNumber();
}
