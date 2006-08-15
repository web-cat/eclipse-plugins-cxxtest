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

/*
 * Change log:
 * 
 * 1.1.1:  Fixed a small bug that caused the test visitor to "forget" that a
 *         main() function was encountered. (aallowat)
 * 1.1.0:  Initial public release.
 */
package net.sf.webcat.eclipse.cxxtest;

import java.util.HashSet;
import java.util.Set;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICElementVisitor;
import org.eclipse.cdt.core.model.IFunctionDeclaration;
import org.eclipse.cdt.core.model.IMethodDeclaration;
import org.eclipse.cdt.core.model.IStructure;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.core.model.IUsing;
import org.eclipse.core.runtime.CoreException;

/**
 * A visitor class that traverses the DOM tree of a C++ project, collecting
 * information about the CxxTest test suites that are implemented in the
 * project.
 * 
 * @author Tony Allowatt (Virginia Tech Computer Science)
 */
public class TestCaseVisitor implements ICElementVisitor
{
	/**
	 * The current suite being processed.
	 */
	private CxxTestSuiteInfo currentSuite = null;

	/**
	 * A vector containing all the test suites processed.
	 */
	private Vector testSuites = new Vector();

	/**
	 * A regular expression that matches the name of the CxxTest suite
	 * base class in the superclass list.  This will match either
	 * TestSuite, CxxTest::TestSuite, or ::CxxTest::TestSuite.
	 */
	private Pattern superclassPattern =
		Pattern.compile("((::)?\\s*CxxTest\\s*::\\s*)?TestSuite");

	/**
	 * Keeps track of whether a "using namespace std" directive was
	 * encountered during the traversal.
	 */
	private boolean usesStandardLibrary = true;

	/**
	 * Keeps track of whether a main() function was encountered during
	 * the traversal.
	 */
	private boolean mainExists = false;

	/**
	 * Contains the IStructure handles of all the test suites encountered,
	 * for quick lookup, such that each suite will only be generated once
	 * (sometimes the tree traversal will encounter the same element twice,
	 * may have to do with resource deltas?)
	 */
	private Set generatedSuites = new HashSet();

	private String driverFileName = null;

	public TestCaseVisitor(String file)
	{
		driverFileName = file;
	}

	/**
	 * Called by the runtime when each element of the C++ project is
	 * processed.
	 */
	public boolean visit(ICElement element) throws CoreException
	{
		boolean visitChildren;

		switch(element.getElementType())
		{
			case ICElement.C_MODEL:
			case ICElement.C_PROJECT:
			case ICElement.C_CCONTAINER:
			case ICElement.C_NAMESPACE:
				/*
				 * Any of these container types should be unconditionally
				 * visited for child elements.
				 */
				visitChildren = true;
				currentSuite = null;
				break;

			case ICElement.C_UNIT:
				visitChildren = shouldVisitFile((ITranslationUnit)element);
				break;

			case ICElement.C_USING:
				/*
				 * Check to see if it's a reference to "std".
				 */
				visitChildren = false;
				checkForStandardLibrary((IUsing)element);
				break;

			case ICElement.C_CLASS:
			case ICElement.C_STRUCT:
				/*
				 * Only visit a class's or struct's children if it inherits
				 * from the CxxTest suite class.
				 */
				visitChildren = isClassTestSuite((IStructure)element);
				break;

			case ICElement.C_METHOD_DECLARATION:
			case ICElement.C_METHOD:
				/*
				 * Check the method to see if it's named properly, and if so
				 * add it to the list of tests for the current suite.
				 */
				visitChildren = false;
				
				if(currentSuite != null)
					checkMethod((IMethodDeclaration)element);

				break;

			case ICElement.C_FUNCTION_DECLARATION:
			case ICElement.C_FUNCTION:
				/*
				 * Check global functions to see if the user has created a
				 * main() function. If so, we need to generate the test
				 * runner as a static object instead.
				 */
				visitChildren = false;
				
				checkForMain((IFunctionDeclaration)element);
				break;

			default:
				visitChildren = false;
				break;
		}
		
		return visitChildren;
	}

	private boolean shouldVisitFile(ITranslationUnit unit)
	{
		String name = unit.getPath().lastSegment();
		if(name.equals(driverFileName))
			return false;
		else
			return true;
	}

	/**
	 * Gets the test suites that were processed by visiting the project DOM.
	 * 
	 * @return An array of CxxTestSuiteInfo objects that represent the test
	 *     suites in the project.
	 */
	public CxxTestSuiteInfo[] getTestSuites()
	{
		return (CxxTestSuiteInfo[])testSuites.toArray(
				new CxxTestSuiteInfo[testSuites.size()]);
	}

	/**
	 * Gets a value indicating whether or not a reference to namespace std
	 * was made.
	 * 
	 * @return true if namespace std was referenced; otherwise, false.
	 */
	public boolean isUsingStandardLibrary()
	{
		return usesStandardLibrary;
	}

	/**
	 * Gets a value indicating whether or not a main() function was
	 * encountered in the traversal.
	 * 
	 * @return true if main() was declared; otherwise, false.
	 */
	public boolean getMainExists()
	{
		return mainExists;
	}

	/**
	 * Check the specified "using" directive to see if it matches "std".
	 * 
	 * @param element The IUsing directive to check.
	 */
	private void checkForStandardLibrary(IUsing element)
	{
		if("std".equals(element.getElementName()))
			usesStandardLibrary = true;
	}

	/**
	 * Determines if a class is a valid CxxTest test suite by checking
	 * for CxxTest::TestSuite in the superclass list.
	 * 
	 * @param element the class handle to check
	 * 
	 * @return true if the class is a test suite; otherwise, false.
	 */
	private boolean isClassTestSuite(IStructure element)
	{
		if(hasBeenGenerated(element))
			return false;

		String[] supers = element.getSuperClassesNames();
		for(int i = 0; i < supers.length; i++)
		{
			Matcher matcher = superclassPattern.matcher(supers[i]);
			if(matcher.matches())
			{
				generatedSuites.add(element);

				currentSuite = new CxxTestSuiteInfo(element);
				testSuites.add(currentSuite);
				return true;
			}
		}

		return false;
	}
	
	/**
	 * Determines if the test suite for this class has already been
	 * discovered.
	 * 
	 * @param element the class handle to check
	 * 
	 * @return true if the suite was already discovered; otherwise, false.
	 */
	private boolean hasBeenGenerated(IStructure element)
	{
		return generatedSuites.contains(element);
	}

	/**
	 * Checks a method inside a test suite class to determine if it
	 * is a valid test method (void return value, no arguments, name
	 * begins with "test"). If so, it is added to the suite's list of
	 * tests.
	 * 
	 * @param element the method handle to be checked.
	 */
	private void checkMethod(IMethodDeclaration element)
	{
		String name = element.getElementName();
		int lineNum = getLineNumber(element);

		boolean isStatic = false;
		
		try
		{
			isStatic = element.isStatic();
		}
		catch(CModelException e) { }

		if(name.startsWith("Test") || name.startsWith("test"))
		{
			if("void".equals(element.getReturnType()) && isMethodParameterless(element))
					currentSuite.addTestMethod(element);
		}
		else if(name.equals("createSuite"))
		{
			if(isStatic &&
					element.getReturnType().indexOf('*') >= 0 &&
					isMethodParameterless(element))
				currentSuite.setCreateLineNumber(lineNum);
		}
		else if(name.equals("destroySuite"))
		{
			String[] params = element.getParameterTypes();
			
			if(isStatic && params.length == 1 &&
					params[0].indexOf('*') >= 0 &&
					"void".equals(element.getReturnType()))
				currentSuite.setDestroyLineNumber(lineNum);
		}
	}
	
	/**
	 * Checks a global function to determine if it is the main() function.
	 * 
	 * @param element the function handle to be checked.
	 */
	private void checkForMain(IFunctionDeclaration element)
	{
		if("main".equals(element.getElementName()))
			mainExists = true;
	}
	
	/**
	 * A convenience function to check if a function takes no arguments
	 * (CDT's DOM treats a function with no arguments differently from one
	 * with a single "void" argument).
	 * 
	 * @param element the method handle to check.
	 * 
	 * @return true if the function has no arguments; otherwise, false.
	 */
	private boolean isMethodParameterless(IMethodDeclaration element)
	{
		return (element.getNumberOfParameters() == 0 ||
				(element.getNumberOfParameters() == 1 &&
						"void".equals(element.getParameterTypes()[0])));
	}
	
	/**
	 * A convenience function to get the line number of a method.
	 * 
	 * @param method the method number to get the line number of.
	 * 
	 * @return An integer representing the line number of the method
	 *     in the source code. 
	 */
	private int getLineNumber(IMethodDeclaration method)
	{
		try
		{
			return method.getSourceRange().getStartLine();
		}
		catch (CModelException e)
		{
			return 1;
		}
	}
}
