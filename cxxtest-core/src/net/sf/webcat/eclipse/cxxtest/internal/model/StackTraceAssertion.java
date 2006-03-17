/**
 * 
 */
package net.sf.webcat.eclipse.cxxtest.internal.model;

import java.text.MessageFormat;
import java.util.Vector;

import net.sf.webcat.eclipse.cxxtest.model.ICxxTestAssertion;
import net.sf.webcat.eclipse.cxxtest.model.ICxxTestBase;
import net.sf.webcat.eclipse.cxxtest.model.ICxxTestStackFrame;

public class StackTraceAssertion implements ICxxTestAssertion
{
	private static final String MSG_FAILED_TEST = "Failed test{0}: {1}";

	private CxxTestMethod parent;
	
	private int status;
	
	private String message;
	
	private Vector stackTrace;
	
	private int lineNumber;

	public StackTraceAssertion(CxxTestMethod parent, int lineNumber, int status)
	{
		this.parent = parent;
		this.status = status;
		this.lineNumber = lineNumber;

		parent.addAssertion(this);
		
		stackTrace = new Vector();
	}

	public String getMessage(boolean includeLine)
	{
		String[] realArgs = new String[2];
		realArgs[0] = Integer.toString(lineNumber);
		realArgs[1] = message;

		if(includeLine)
			realArgs[0] = " (line " + realArgs[0] + ")";
		else
			realArgs[0] = "";

		return MessageFormat.format(MSG_FAILED_TEST, realArgs);
	}

	public void setMessage(String msg)
	{
		message = msg;
	}

	public void addStackFrame(ICxxTestStackFrame frame)
	{
		stackTrace.add(frame);
	}

	public ICxxTestBase getParent()
	{
		return parent;
	}

	public int getLineNumber()
	{
		return lineNumber;
	}

	public int getStatus()
	{
		return status;
	}
	
	public ICxxTestStackFrame[] getStackTrace()
	{
		ICxxTestStackFrame[] frames =
			new ICxxTestStackFrame[stackTrace.size()];
		stackTrace.toArray(frames);
		return frames;
	}
}