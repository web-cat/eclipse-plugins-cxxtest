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

import net.sf.webcat.eclipse.cxxtest.model.ICxxTestBase;
import net.sf.webcat.eclipse.cxxtest.model.ICxxTestStackFrame;
import net.sf.webcat.eclipse.cxxtest.model.ICxxTestSuiteError;

public class CxxTestSuiteError implements ICxxTestSuiteError
{
	private CxxTestSuite suite;

	private String errorType;

	private int line;

	private String msg;
	
	private Vector stackTrace;

	public CxxTestSuiteError(CxxTestSuite suite, Attributes attributes)
	{
		this.suite = suite;
		stackTrace = new Vector();

		errorType = attributes.getValue("type");
		
		String lineStr = attributes.getValue("line");
		line = Integer.parseInt(lineStr);

		suite.addChild(this);
	}

	/* (non-Javadoc)
	 * @see net.sf.webcat.eclipse.cxxtest.model.ICxxTestSuiteChild#getLineNumber()
	 */
	public int getLineNumber()
	{
		return line;
	}

	/* (non-Javadoc)
	 * @see net.sf.webcat.eclipse.cxxtest.model.ICxxTestSuiteError#getMessage()
	 */
	public String getMessage()
	{
		return msg;
	}

	public void setMessage(String msg)
	{
		this.msg = msg;
	}

	/* (non-Javadoc)
	 * @see net.sf.webcat.eclipse.cxxtest.model.ICxxTestSuiteError#getName()
	 */
	public String getName()
	{
		if("init".equals(errorType))
			return "<initialization error>";
		else
			return errorType;
	}

	/* (non-Javadoc)
	 * @see net.sf.webcat.eclipse.cxxtest.model.ICxxTestSuiteError#getStackTrace()
	 */
	public ICxxTestStackFrame[] getStackTrace()
	{
		ICxxTestStackFrame[] frames =
			new ICxxTestStackFrame[stackTrace.size()];
		stackTrace.toArray(frames);
		return frames;
	}

	public void addStackFrame(ICxxTestStackFrame frame)
	{
		stackTrace.add(frame);
	}

	/* (non-Javadoc)
	 * @see net.sf.webcat.eclipse.cxxtest.model.ICxxTestBase#getParent()
	 */
	public ICxxTestBase getParent()
	{
		return suite;
	}

	/* (non-Javadoc)
	 * @see net.sf.webcat.eclipse.cxxtest.model.ICxxTestBase#getStatus()
	 */
	public int getStatus()
	{
		return ICxxTestBase.STATUS_ERROR;
	}

	public String toString()
	{
		return getName();
	}
}
