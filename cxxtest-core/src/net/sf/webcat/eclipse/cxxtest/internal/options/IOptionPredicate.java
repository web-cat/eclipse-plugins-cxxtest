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
package net.sf.webcat.eclipse.cxxtest.internal.options;

/**
 * Used by ProjectOptionsUtil methods to remove options if they satisfy a
 * user-defined predicate.
 * 
 * @author Tony Allevato (Virginia Tech Computer Science)
 */
public interface IOptionPredicate
{
	// ------------------------------------------------------------------------
	/**
	 * Returns a value indicating whether the specified value satisfies the
	 * predicate.
	 * 
	 * @param value
	 *            the value to test
	 * 
	 * @return true if the value satisfies the predicate; otherwise, false.
	 */
	boolean accept(String value);
}