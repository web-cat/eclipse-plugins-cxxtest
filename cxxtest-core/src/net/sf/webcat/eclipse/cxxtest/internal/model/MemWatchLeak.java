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

import java.util.Vector;

import org.xml.sax.Attributes;

import net.sf.webcat.eclipse.cxxtest.model.ICxxTestStackFrame;
import net.sf.webcat.eclipse.cxxtest.model.IMemWatchLeak;

public class MemWatchLeak implements IMemWatchLeak
{
	private String address;
	
	private int size;
	
	private boolean array;

	private Vector<ICxxTestStackFrame> stackTrace;

	public MemWatchLeak(Attributes attributes)
	{
		address = attributes.getValue("address");
		size = Integer.parseInt(attributes.getValue("size"));
		
		if("yes".equals(attributes.getValue("array")))
			array = true;
		else
			array = false;
		
		stackTrace = new Vector<ICxxTestStackFrame>();
	}

	public void addStackFrame(ICxxTestStackFrame frame)
	{
		stackTrace.add(frame);
	}

	public String getAddress()
	{
		return address;
	}

	public int getSize()
	{
		return size;
	}

	public boolean isArray()
	{
		return array;
	}

	public ICxxTestStackFrame[] getStackTrace()
	{
		ICxxTestStackFrame[] frames =
			new ICxxTestStackFrame[stackTrace.size()];
		stackTrace.toArray(frames);
		return frames;
	}
	
	public String toString()
	{
		String msg;
		
		if(isArray())
			msg = "Array";
		else
			msg = "Block";
		
		msg += " at " + getAddress();
		msg += ", " + getSize() + " bytes";
		
		return msg;
	}
}
