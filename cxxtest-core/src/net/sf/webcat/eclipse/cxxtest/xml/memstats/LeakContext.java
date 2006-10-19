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

import org.xml.sax.Attributes;

import net.sf.webcat.eclipse.cxxtest.internal.model.MemWatchLeak;
import net.sf.webcat.eclipse.cxxtest.model.ICxxTestStackFrame;
import net.sf.webcat.eclipse.cxxtest.xml.ElementContext;
import net.sf.webcat.eclipse.cxxtest.xml.common.IStackFrameConsumer;
import net.sf.webcat.eclipse.cxxtest.xml.common.StackFrameContext;

public class LeakContext extends ElementContext implements
	IStackFrameConsumer
{
	private MemWatchLeak leak;

	public LeakContext(MemStatsContext memStats, Attributes attributes)
	{
		leak = new MemWatchLeak(attributes);
		memStats.addLeak(leak);
	}

	public ElementContext startElement(String uri, String localName,
			String qName, Attributes attributes)
	{
		if(localName.equals("stack-frame"))
			return new StackFrameContext(this, attributes);
		else
			return null;
	}

	public void addStackFrame(ICxxTestStackFrame frame)
	{
		leak.addStackFrame(frame);
	}
}
