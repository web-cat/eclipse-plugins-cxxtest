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
package net.sf.webcat.eclipse.cxxtest.xml.memstats;

import net.sf.webcat.eclipse.cxxtest.internal.model.DerefereeSummary;
import net.sf.webcat.eclipse.cxxtest.model.IDerefereeLeak;
import net.sf.webcat.eclipse.cxxtest.xml.ElementContext;

import org.xml.sax.Attributes;

public class DerefereeContext extends ElementContext
{
	private DocumentContext document;

	public DerefereeContext(DocumentContext document, Attributes attributes)
	{
		this.document = document;
		
		document.setActualLeakCount(getAttrInt(attributes, "actual-leak-count"));
	}

	public ElementContext startElement(String uri, String localName,
			String qName, Attributes attributes)
	{
		if(localName.equals("summary"))
			return new SummaryContext(this, attributes);
		else if(localName.equals("leak"))
			return new LeakContext(this, attributes);
		else
			return null;
	}
	
	public void addLeak(IDerefereeLeak leak)
	{
		document.addLeak(leak);
	}
	
	public void setSummary(DerefereeSummary info)
	{
		document.setSummary(info);
	}
	
	private int getAttrInt(Attributes attributes, String name)
	{
		return Integer.parseInt(attributes.getValue(name));
	}
}
