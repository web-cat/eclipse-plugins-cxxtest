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
package net.sf.webcat.eclipse.cxxtest.xml.testresults;

import org.xml.sax.Attributes;

import net.sf.webcat.eclipse.cxxtest.internal.model.CxxTestMethod;
import net.sf.webcat.eclipse.cxxtest.internal.model.CxxTestSuite;
import net.sf.webcat.eclipse.cxxtest.xml.ElementContext;

public class TestContext extends ElementContext 
{
	private CxxTestMethod test;

	public TestContext(CxxTestSuite suite, Attributes attributes)
	{
		test = new CxxTestMethod(suite, attributes);
	}
	
	public ElementContext startElement(String uri, String localName, String qName, Attributes attributes)
	{
		return new AssertionContext(test, localName, attributes);
	}
}
