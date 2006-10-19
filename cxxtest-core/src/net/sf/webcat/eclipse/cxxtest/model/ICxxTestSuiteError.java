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
 * Represents a major error that affects an entire test suite during
 * execution. Currently, this is used to represent an error trapped during
 * initialization of a test suite (the constructor for one of its fields
 * raising a signal, for instance), but it could be expanded to other
 * types of errors in the future.
 *  
 * @author Tony Allowatt (Virginia Tech Computer Science)
 */
public interface ICxxTestSuiteError extends ICxxTestSuiteChild
{
	/**
	 * Gets the type of error that occurred.
	 * 
	 * @return a String indicating the type of error that occurred. 
	 */
	String getName();
	
	/**
	 * Gets the message associated with the error.
	 * 
	 * @return a String indicating the error message.
	 */
	String getMessage();
	
	/**
	 * Gets the stack trace that indicates where the error occurred.
	 * 
	 * @return an array of ICxxTestStackFrame objects that represent the
	 *     stack trace.
	 */
	ICxxTestStackFrame[] getStackTrace();
}
