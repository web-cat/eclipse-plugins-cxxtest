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

import java.util.Vector;

import org.xml.sax.Attributes;

import net.sf.webcat.eclipse.cxxtest.internal.model.DerefereeSummary;
import net.sf.webcat.eclipse.cxxtest.model.IDerefereeSummary;
import net.sf.webcat.eclipse.cxxtest.model.IDerefereeLeak;
import net.sf.webcat.eclipse.cxxtest.xml.ElementContext;

public class DocumentContext extends ElementContext
{
	private Vector<IDerefereeLeak> leaks;
	
	private DerefereeSummary summary;

	private int actualLeakCount;
	
	public DocumentContext()
	{
		leaks = new Vector<IDerefereeLeak>();
	}

	public ElementContext startElement(String uri, String localName,
			String qName, Attributes attributes)
	{
		if(localName.equals("dereferee"))
			return new DerefereeContext(this, attributes);
		else
			return null;
	}

	public void addLeak(IDerefereeLeak leak)
	{
		leaks.add(leak);
	}

	public IDerefereeLeak[] getLeaks()
	{
		return leaks.toArray(new IDerefereeLeak[leaks.size()]);
	}

	public void setSummary(DerefereeSummary info)
	{
		summary = info;
	}
	
	public IDerefereeSummary getSummary()
	{
		summary.setLeaks(getLeaks());
		summary.setActualLeakCount(actualLeakCount);
		return summary;
	}
	
	public void setActualLeakCount(int value)
	{
		actualLeakCount = value;
	}
}
