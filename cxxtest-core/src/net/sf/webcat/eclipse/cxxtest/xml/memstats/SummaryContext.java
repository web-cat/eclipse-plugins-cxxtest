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

import net.sf.webcat.eclipse.cxxtest.internal.model.MemWatchInfo;
import net.sf.webcat.eclipse.cxxtest.xml.ElementContext;

import org.xml.sax.Attributes;

public class SummaryContext extends ElementContext
{
	public SummaryContext(MemStatsContext parent, Attributes attributes)
	{
		int totalBytes = getAttrInt(attributes, "total-bytes-allocated");
		int maxBytes = getAttrInt(attributes, "max-bytes-in-use");

/*		int maxBlocks = getAttrInt(attributes, "max-blocks");
		int callsNew = getAttrInt(attributes, "calls-to-new");
		int callsDelete = getAttrInt(attributes, "calls-to-delete");
		int callsNewArray = getAttrInt(attributes, "calls-to-new-array");
		int callsDeleteArray = getAttrInt(attributes, "calls-to-delete-array");*/
		
		MemWatchInfo info = new MemWatchInfo(totalBytes, maxBytes);
		parent.setSummary(info);
	}
	
	private int getAttrInt(Attributes attributes, String name)
	{
		return Integer.parseInt(attributes.getValue(name));
	}
}
