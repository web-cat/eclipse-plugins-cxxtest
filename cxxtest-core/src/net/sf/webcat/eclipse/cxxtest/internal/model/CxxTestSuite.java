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
import net.sf.webcat.eclipse.cxxtest.model.ICxxTestSuiteChild;

import org.xml.sax.Attributes;

public class CxxTestSuite implements ICxxTestSuite
{
	private Vector<ICxxTestSuiteChild> children;

	private String name;
	
	private String file;
	
	private int line;

	public CxxTestSuite(Attributes attributes)
	{
		children = new Vector<ICxxTestSuiteChild>();

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

	public ICxxTestSuiteChild[] getChildren(boolean onlyTests)
	{
		if(!onlyTests)
		{
			return children.toArray(new ICxxTestSuiteChild[children.size()]);
		}
		else
		{
			Vector<ICxxTestSuiteChild> vec = new Vector<ICxxTestSuiteChild>();
			for(int i = 0; i < children.size(); i++)
			{
				if(children.get(i) instanceof ICxxTestMethod)
					vec.add(children.get(i));
			}
			
			return vec.toArray(new ICxxTestSuiteChild[vec.size()]);
		}
	}
	
	public void addChild(ICxxTestSuiteChild child)
	{
		children.add(child);
	}

	public ICxxTestBase getParent()
	{
		return null;
	}

	public int getStatus()
	{
		int maxStatus = STATUS_OK;

		for(int i = 0; i < children.size(); i++)
		{
			ICxxTestSuiteChild child = children.get(i);
			if(child.getStatus() > maxStatus)
				maxStatus = child.getStatus();
		}
		
		return maxStatus;
	}
	
	public String toString()
	{
		return getName();
	}
}
