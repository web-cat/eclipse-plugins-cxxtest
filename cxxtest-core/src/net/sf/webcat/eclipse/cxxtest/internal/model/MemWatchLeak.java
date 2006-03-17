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

	private Vector stackTrace;

	public MemWatchLeak(Attributes attributes)
	{
		address = attributes.getValue("address");
		size = Integer.parseInt(attributes.getValue("size"));
		
		if("yes".equals(attributes.getValue("array")))
			array = true;
		else
			array = false;
		
		stackTrace = new Vector();
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
