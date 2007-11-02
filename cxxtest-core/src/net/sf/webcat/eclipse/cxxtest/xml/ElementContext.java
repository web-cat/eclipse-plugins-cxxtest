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
package net.sf.webcat.eclipse.cxxtest.xml;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/**
 * Represents a tag context used by the stack-based contextual SAX parser.
 *  
 * @author Tony Allevato (Virginia Tech Computer Science)
 */
public abstract class ElementContext
{
	public ElementContext startElement(
			String uri, String localName, String qName, Attributes attributes)
	throws SAXException
	{
		return null;
	}
	
	public void endElement(String uri, String localName, String qName)
	throws SAXException
	{
	}
	
	public void characters(char[] chars, int start, int length)
	throws SAXException
	{
	}
}
