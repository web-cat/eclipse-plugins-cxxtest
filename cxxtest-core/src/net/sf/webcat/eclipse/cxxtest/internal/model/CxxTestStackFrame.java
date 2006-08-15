/*
 *	This file is part of Web-CAT Eclipse Plugins.
 *
 *	Web-CAT is free software; you can redistribute it and/or modify
 *	it under the terms of the GNU General Public License as published by
 *	the Free Software Foundation; either version 2 of the License, or
 *	(at your option) any later version.
 *
 *	Web-CAT is distributed in the hope that it will be useful,
 *	but WITHOUT ANY WARRANTY; without even the implied warranty of
 *	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *	GNU General Public License for more details.
 *
 *	You should have received a copy of the GNU General Public License
 *	along with Web-CAT; if not, write to the Free Software
 *	Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
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