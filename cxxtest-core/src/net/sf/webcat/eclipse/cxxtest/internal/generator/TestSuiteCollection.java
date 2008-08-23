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

import java.util.ArrayList;
import java.util.List;

public class TestSuiteCollection
{
    public TestSuiteCollection()
    {
        this.suites = new ArrayList<TestSuite>();
        this.possibleTestFiles = new ArrayList<String>();
    }


    public List<TestSuite> getSuites()
    {
        return suites;
    }


    public List<String> getPossibleTestFiles()
    {
        return possibleTestFiles;
    }


    public boolean doesMainFunctionExist()
    {
        return mainExists;
    }

    
    public void addSuite(TestSuite suite)
    {
        suites.add(suite);
    }
    
    
    public TestSuite getSuite(String name)
    {
        for (TestSuite suite : suites)
        {
            if (name.equals(suite.getName()))
                return suite;
        }
        
        return null;
    }

    
    private List<TestSuite> suites;

    private List<String> possibleTestFiles;

    private boolean mainExists;
}
