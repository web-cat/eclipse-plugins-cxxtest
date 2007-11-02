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

import java.text.MessageFormat;
import java.util.Enumeration;
import java.util.Stack;

import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * A SAX content handler that keeps a stack of "contexts", where each context
 * is pushed upon entering a new tag and popped upon exiting the tag. Users
 * of this class should subclass an ElementContext for each tag type in their
 * document and put appropriate attributes/children/content handling in that
 * context class.
 * 
 * @author Tony Allevato (Virginia Tech Computer Science)
 */
public class ContextualSAXHandler extends DefaultHandler
{
	private class ContextEntry
	{
		private ElementContext context;
		private String localName;
		
		public ContextEntry(ElementContext context, String localName)
		{
			this.context = context;
			this.localName = localName;
		}
		
		public ElementContext getContext()
		{
			return context;
		}
		
		public String getLocalName()
		{
			return localName;
		}
	}

	private Stack<ContextEntry> contextStack;
	
	private Locator locator;
	
	private ElementContext initialContext;
	
	public ContextualSAXHandler(ElementContext initialContext)
	{
		this.initialContext = initialContext;

		contextStack = new Stack<ContextEntry>();
	}

	public void setDocumentLocator(Locator locator)
	{
		this.locator = locator;
	}

	private static final String E_PREMATURE_EMPTY =
		"Context stack prematurely empty (unexpected end tags?)";

	private static final String E_NOT_EMPTY =
		"Context stack not empty at end of document (missing end tags?); current context: {0}";

	private static final String E_UNRECOGNIZED_TAG =
		"Unrecognized tag <{0}> in this context: {1}";

	public void startDocument()
	{
		ContextEntry entry = new ContextEntry(initialContext, "");
		contextStack.push(entry);
	}

	public void endDocument() throws SAXException
	{
		if(contextStack.size() != 1)
		{
			String ctxPath = getCurrentContextPath();

			String msg = MessageFormat.format(E_NOT_EMPTY,
					new Object[] { ctxPath });

			throw new SAXParseException(msg, locator);
		}
	}

	public void startElement(String uri, String localName, String qName,
			Attributes attributes) throws SAXException
	{
		ContextEntry entry = contextStack.peek();
		ElementContext newContext = null;
		
		try
		{
			newContext = entry.getContext().startElement(
					uri, localName, qName, attributes);
		}
		catch(SAXException e)
		{
			throw new SAXParseException(null, locator, e);
		}

		if(newContext == null)
		{
			String ctxPath = getCurrentContextPath();
			
			String msg = MessageFormat.format(E_UNRECOGNIZED_TAG,
					new Object[] { localName, ctxPath });

			throw new SAXParseException(msg, locator);
		}

		ContextEntry newEntry = new ContextEntry(newContext, localName);
		contextStack.push(newEntry);
	}

	private String getCurrentContextPath()
	{
		StringBuffer buf = new StringBuffer();

		boolean first = true;

		Enumeration<ContextEntry> e = contextStack.elements();
		while(e.hasMoreElements())
		{
			ContextEntry entry = e.nextElement();
			
			if(first)
				first = false;
			else
				buf.append('/');

			buf.append(entry.getLocalName());
		}
		
		return buf.toString();
	}

	public void endElement(String uri, String localName, String qName)
	throws SAXException
	{
		if(contextStack.size() == 1)
		{
			throw new SAXParseException(E_PREMATURE_EMPTY, locator);
		}

		ContextEntry entry = contextStack.pop();
		
		try
		{
			entry.getContext().endElement(uri, localName, qName);
		}
		catch(SAXException e)
		{
			throw new SAXParseException(null, locator, e);
		}
	}
	
	public void characters(char[] chars, int start, int length)
	throws SAXException
	{
		ContextEntry entry = contextStack.peek();
		
		try
		{
			entry.getContext().characters(chars, start, length);
		}
		catch(SAXException e)
		{
			throw new SAXParseException(null, locator, e);
		}
	}
}
