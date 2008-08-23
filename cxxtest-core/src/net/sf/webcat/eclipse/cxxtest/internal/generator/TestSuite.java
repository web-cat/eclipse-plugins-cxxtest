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

   public class TestSuite
   {
       //~ Constructors .....................................................

       // ------------------------------------------------------
       /// <summary>
       /// Creates a new test suite based on the specified class in the VC
       /// code model.
       /// </summary>
       /// <param name="testClass">
       /// The VC code model class object from which to create the test
       /// suite.
       /// </param>
       public TestSuite(String name, String fullPath, int lineNumber)
       {
           this.name = name;
           this.fullPath = fullPath;
           this.lineNumber = lineNumber;

           testCases = new ArrayList<TestCase>();

           createLineNumber = 0;
           destroyLineNumber = 0;
       }


       //~ Properties .......................................................

       // ------------------------------------------------------
       /// <summary>
       /// Gets the name of the test suite class.
       /// </summary>
       public String getName()
       {
           return name;
       }


       // ------------------------------------------------------
       /// <summary>
       /// Gets the full path to the source file that contains this test
       /// suite.
       /// </summary>
       public String getFullPath()
       {
           return fullPath;
       }


       // ------------------------------------------------------
       /// <summary>
       /// Gets the line number in the source code at which this test suite
       /// class starts.
       /// </summary>
       public int getLineNumber()
       {
           return lineNumber;
       }


       // ------------------------------------------------------
       /// <summary>
       /// Gets the name of the C++ object that will be generated to
       /// represent this test suite.
       /// </summary>
       public String getObjectName()
       {
           return "suite_" + getName();
       }


       // ------------------------------------------------------
       /// <summary>
       /// Gets the name of the C++ object that will be generated to
       /// represent this test suite description.
       /// </summary>
       public String getDescriptionObjectName()
       {
           return "suiteDescription_" + getName();
       }


       // ------------------------------------------------------
       /// <summary>
       /// Gets the name of the C++ object that will be generated to
       /// represent the list of test cases found in this test suite.
       /// </summary>
       public String getTestListName()
       {
           return "Tests_" + getName();
       }


       // ------------------------------------------------------
       /// <summary>
       /// Gets the array of test cases in this test suite.
       /// </summary>
       public List<TestCase> getTestCases()
       {
           return testCases;
       }


       // ------------------------------------------------------
       /// <summary>
       /// Gets or sets the line number that contains a createSuite static
       /// method for this test suite, or 0 if there is none.
       /// </summary>
       public int getCreateLineNumber()
       {
           return createLineNumber;
       }
       
       
       public void setCreateLineNumber(int value)
       {
           createLineNumber = value;
       }


       // ------------------------------------------------------
       /// <summary>
       /// Gets or sets the line number that contains a destroySuite static
       /// method for this test suite, or 0 if there is none.
       /// </summary>
       public int getDestroyLineNumber()
       {
           return destroyLineNumber;
       }
       
       
       public void setDestroyLineNumber(int value)
       {
           destroyLineNumber = value;
       }


       // ------------------------------------------------------
       /// <summary>
       /// Gets a value indicating whether the test suite is dynamic or not
       /// (that is, it contains a createSuite static method).
       /// </summary>
       public boolean isDynamic()
       {
           return (createLineNumber != 0);
       }


       //~ Methods ..........................................................

       // ------------------------------------------------------
       /// <summary>
       /// Adds a test case method to this test suite.
       /// </summary>
       /// <param name="testCase">
       /// The test case to add to this test suite.
       /// </param>
       public void addTestCase(TestCase testCase)
       {
           testCases.add(testCase);
       }


       //~ Instance variables ...............................................

       private String name;
       private String fullPath;
       private int lineNumber;

       // The list of test case methods contained in this test suite.
       private List<TestCase> testCases;

       // The line number of the createSuite method, or 0 if there is none.
       private int createLineNumber;

       // The line number of the destroySuite method, or 0 if there is none.
       private int destroyLineNumber;
   }
