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
import net.sf.webcat.eclipse.cxxtest.model.ICxxTestBase;

import org.xml.sax.Attributes;

public class CxxTestSuite implements ICxxTestSuite
{
	private Vector tests;

	private String name;
	
	private String file;
	
	private int line;

	public CxxTestSuite(Attributes attributes)
	{
		tests = new Vector();

		name = attributes.getValue("name");
		file = attributes.getValue("file");
		String lineStr = attributes.getValue("line");

		line = Integer.parseInt(lineStr);
	}
	
	public String getName()
	{
		return name;
	}
	
	public String getFile()
	{
		return file;
	}
	
	public int getLineNumber()
	{
		return line;
	}

	public ICxxTestMethod[] getTests()
	{
		return (ICxxTestMethod[])tests.toArray(new ICxxTestMethod[tests.size()]);
	}
	
	public void addTest(ICxxTestMethod test)
	{
		tests.add(test);
	}

	public ICxxTestBase getParent()
	{
		return null;
	}

	public int getStatus()
	{
		int maxStatus = STATUS_OK;

		for(int i = 0; i < tests.size(); i++)
		{
			ICxxTestMethod test = (ICxxTestMethod)tests.get(i);
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
