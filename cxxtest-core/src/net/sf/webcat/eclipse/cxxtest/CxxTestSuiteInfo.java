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
package net.sf.webcat.eclipse.cxxtest;

import java.util.Vector;

import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.IMethodDeclaration;
import org.eclipse.cdt.core.model.IStructure;
import org.eclipse.core.runtime.IPath;

/**
 * This class is used to keep track of CxxTest test suite classes during the
 * traversal of the project DOM tree.  The test runner source code is then
 * generated for a list of these objects.
 * 
 * @author Tony Allevato (Virginia Tech Computer Science)
 */
public class CxxTestSuiteInfo
{
	private IStructure element;

	private Vector<IMethodDeclaration> testMethods;

	private int createLineNumber;
	
	private int destroyLineNumber;

	/**
	 * Constructs a new CxxTestSuiteInfo object associated with the specified
	 * structure/class in the project DOM.
	 * 
	 * @param element the C++ structure/class handle
	 */
	public CxxTestSuiteInfo(IStructure element)
	{
		this.element = element;
		
		testMethods = new Vector<IMethodDeclaration>();
		
		createLineNumber = 0;
		destroyLineNumber = 0;
	}
	
	public String getName()
	{
		return element.getElementName();
	}

	public String getPath()
	{
		IPath projectPath = element.getCProject().getPath();
		IPath elementPath = element.getPath();
		int matchingSegments = elementPath.matchingFirstSegments(projectPath);
		return elementPath.removeFirstSegments(matchingSegments).toString();
	}

	public int getLineNumber()
	{
		try
		{
			return element.getSourceRange().getStartLine();
		}
		catch(CModelException e)
		{
			return 1;
		}
	}
	
	public String getObjectName()
	{
		return "suite_" + getName();
	}
	
	public String getDescriptionObjectName()
	{
		return "suiteDescription_" + getName();
	}
	
	public String getTestListName()
	{
		return "Tests_" + getName();
	}
	
	public void addTestMethod(IMethodDeclaration method)
	{
		testMethods.add(method);
	}
	
	public IMethodDeclaration[] getTestMethods()
	{
		return testMethods.toArray(new IMethodDeclaration[testMethods.size()]);
	}
	
	public void setCreateLineNumber(int value)
	{
		createLineNumber = value;
	}

	public int getCreateLineNumber()
	{
		return createLineNumber;
	}

	public void setDestroyLineNumber(int value)
	{
		destroyLineNumber = value;
	}

	public int getDestroyLineNumber()
	{
		return destroyLineNumber;
	}
	
	public boolean isDynamic()
	{
		return createLineNumber != 0;
	}
}
