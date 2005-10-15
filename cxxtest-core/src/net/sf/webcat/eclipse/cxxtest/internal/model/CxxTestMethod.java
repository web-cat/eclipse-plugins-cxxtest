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

import net.sf.webcat.eclipse.cxxtest.model.ICxxTestMethod;
import net.sf.webcat.eclipse.cxxtest.model.ICxxTestSuite;
import net.sf.webcat.eclipse.cxxtest.model.ICxxTestAssertion;
import net.sf.webcat.eclipse.cxxtest.model.ICxxTestBase;

import org.xml.sax.Attributes;

public class CxxTestMethod implements ICxxTestMethod
{
	private ICxxTestSuite suite;

	private Vector assertions;
	
	private String name;

	private int line;

	public CxxTestMethod(CxxTestSuite suite, Attributes attributes)
	{
		this.suite = suite;
		assertions = new Vector();
		
		suite.addTest(this);
		
		name = attributes.getValue("name");
		String lineStr = attributes.getValue("line");
		
		line = Integer.parseInt(lineStr);
	}

	public ICxxTestBase getParent()
	{
		return suite;
	}

	public String getName()
	{
		return name;
	}
	
	public int getLineNumber()
	{
		return line;
	}

	public ICxxTestAssertion[] getFailedAssertions()
	{
		return (ICxxTestAssertion[])assertions.toArray(new ICxxTestAssertion[assertions.size()]);
	}
	
	protected void addAssertion(ICxxTestAssertion assertion)
	{
		assertions.add(assertion);
	}
	
	public int getStatus()
	{
		int maxStatus = STATUS_OK;

		for(int i = 0; i < assertions.size(); i++)
		{
			ICxxTestAssertion test = (ICxxTestAssertion)assertions.get(i);
			if(test.getStatus() > maxStatus)
				maxStatus = test.getStatus();
		}
		
		return maxStatus;
	}
	
	public String toString()
	{
		return getName();
	}
}
