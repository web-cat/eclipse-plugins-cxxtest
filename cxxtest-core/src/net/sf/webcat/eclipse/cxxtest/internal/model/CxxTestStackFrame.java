/**
 * 
 */
package net.sf.webcat.eclipse.cxxtest.internal.model;

import net.sf.webcat.eclipse.cxxtest.model.ICxxTestStackFrame;

public class CxxTestStackFrame implements ICxxTestStackFrame
{
	private String function;
	
	private String file;
	
	private int lineNumber;

	public CxxTestStackFrame(String function, String file, int lineNumber)
	{
		this.function = function;
		this.file = file;
		this.lineNumber = lineNumber;
	}

	public String getFunction()
	{
		return function;
	}

	public String getFile()
	{
		return file;
	}

	public int getLineNumber()
	{
		return lineNumber;
	}
	
	public String toString()
	{
		String str = function;
		
		if(file != null)
		{
			str += " in " + file;
			
			if(lineNumber != 0)
				str += ":" + lineNumber;
		}
		
		return str;
	}
}