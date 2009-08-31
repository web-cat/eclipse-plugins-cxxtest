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
package net.sf.webcat.eclipse.cxxtest.internal.generator;

//--------------------------------------------------------------------------
/**
 * Represents a test case method that will have code generated to execute it.
 * 
 * @author Tony Allevato
 * @version $Id$
 */
public class TestCase
{
    //~ Constructor ...........................................................

    // ----------------------------------------------------------
    /**
     * Creates a new test case at the specified line in a source file.
     */
    public TestCase(String name, int lineNumber)
    {
        this.name = name;
        this.lineNumber = lineNumber;
    }


    //~ Methods ...............................................................

    // ----------------------------------------------------------
    /**
     * Gets the name of the test case method.
     */
    public String getName()
    {
        return name;
    }


    // ----------------------------------------------------------
    /**
     * Gets the line number at which the test case method appears in the
     * source.
     */
    public int getLineNumber()
    {
        return lineNumber;
    }


    //~ Static/instance variables .............................................

    private String name;
    private int lineNumber;
}
