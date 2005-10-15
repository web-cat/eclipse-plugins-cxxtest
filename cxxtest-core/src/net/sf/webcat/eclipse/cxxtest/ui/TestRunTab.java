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
package net.sf.webcat.eclipse.cxxtest.ui;

import net.sf.webcat.eclipse.cxxtest.model.ICxxTestBase;
import net.sf.webcat.eclipse.cxxtest.model.ICxxTestSuite;

import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.dnd.Clipboard;

/**
 * The base class for all the CxxTest view tabs.  (Currently there is only
 * one, TestHierarchyTab).
 * 
 * Influenced greatly by the same JUnit class.
 */
public abstract class TestRunTab
{	
	/**
	 * Create the tab control
	 * @param tabFolder the containing tab folder
	 * @param clipboard the clipboard to be used by the tab
	 * @param runner the testRunnerViewPart containing the tab folder
	 */
	public abstract void createTabControl(CTabFolder tabFolder,
			Clipboard clipboard, TestRunnerViewPart runner);
	
	/**
	 * Returns the name of the currently selected Test in the View
	 */
	public abstract ICxxTestBase getSelectedTestObject();

	/**
	 * Activates the TestRunView
	 */
	public void activate()
	{
	}

	public void setSuites(ICxxTestSuite[] suites)
	{	
	}

	/**
	 * Sets the focus in the TestRunView
	 */
	public void setFocus()
	{
	}
	
	/**
	 * Returns the name of the RunView
	 */
	public abstract String getName();
	
	/**
	 * Sets the current Test in the View
	 */
	public void setSelectedTest(ICxxTestBase testObject)
	{
	}
	
	/**
	 * Select next test failure.
	 */
	public void selectNext()
	{
	}
	
	/**
	 * Select previous test failure.
	 */
	public void selectPrevious()
	{
	}
}
