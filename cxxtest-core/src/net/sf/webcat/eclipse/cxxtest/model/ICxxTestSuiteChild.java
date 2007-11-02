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
 * This interface represents a type of message that can occur as the direct
 * child of a CxxTest test suite. Currently this is used to represent test
 * methods and test suite initialization errors.
 * 
 * @author Tony Allevato (Virginia Tech Computer Science)
 */
public interface ICxxTestSuiteChild extends ICxxTestBase
{
	/**
	 * Gets the line number at which this element starts.
	 * 
	 * @return the line number at which this element starts.
	 */
	public int getLineNumber();
}
