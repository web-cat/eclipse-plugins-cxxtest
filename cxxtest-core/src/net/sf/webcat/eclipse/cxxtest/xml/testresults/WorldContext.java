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

import net.sf.webcat.eclipse.cxxtest.model.ICxxTestSuite;
import net.sf.webcat.eclipse.cxxtest.xml.ElementContext;

import org.xml.sax.Attributes;

public class WorldContext extends ElementContext
{
	private DocumentContext document;

	public WorldContext(DocumentContext document, Attributes attributes)
	{
		this.document = document;
	}

	public ElementContext startElement(String uri, String localName,
			String qName, Attributes attributes)
	{
		if(localName.equals("suite"))
			return new SuiteContext(this, attributes);
		else
			return null;
	}
	
	public void addSuite(ICxxTestSuite suite)
	{
		document.addSuite(suite);
	}

	public ICxxTestSuite[] getSuites()
	{
		return document.getSuites();
	}
}
