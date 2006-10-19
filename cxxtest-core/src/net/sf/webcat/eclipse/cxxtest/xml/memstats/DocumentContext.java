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

import net.sf.webcat.eclipse.cxxtest.internal.model.MemWatchInfo;
import net.sf.webcat.eclipse.cxxtest.model.IMemWatchInfo;
import net.sf.webcat.eclipse.cxxtest.model.IMemWatchLeak;
import net.sf.webcat.eclipse.cxxtest.xml.ElementContext;

public class DocumentContext extends ElementContext
{
	private Vector leaks;
	
	private MemWatchInfo summary;

	public DocumentContext()
	{
		leaks = new Vector();
	}

	public ElementContext startElement(String uri, String localName,
			String qName, Attributes attributes)
	{
		if(localName.equals("memwatch"))
			return new MemStatsContext(this, attributes);
		else
			return null;
	}

	public void addLeak(IMemWatchLeak leak)
	{
		leaks.add(leak);
	}

	public IMemWatchLeak[] getLeaks()
	{
		return (IMemWatchLeak[])leaks.toArray(
				new IMemWatchLeak[leaks.size()]);
	}

	public void setSummary(MemWatchInfo info)
	{
		summary = info;
	}
	
	public IMemWatchInfo getSummary()
	{
		summary.setLeaks(getLeaks());
		return summary;
	}
}
