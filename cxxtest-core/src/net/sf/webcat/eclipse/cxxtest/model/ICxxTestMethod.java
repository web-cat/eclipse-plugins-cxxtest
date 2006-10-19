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
package net.sf.webcat.eclipse.cxxtest.model;

/**
 * Represents a CxxTest suite method.
 * 
 * @author Tony Allowatt (Virginia Tech Computer Science)
 */
public interface ICxxTestMethod extends ICxxTestSuiteChild
{
	/**
	 * Gets the local name of the method.
	 * 
	 * @return a String containing the name of the method.
	 */
	String getName();

	/**
	 * Gets a list of failed assertions, if any, that occurred in this method.
	 *  
	 * @return an array of ICxxTestAssertion objects.
	 */
	ICxxTestAssertion[] getFailedAssertions();
}
