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
import net.sf.webcat.eclipse.cxxtest.xml.ElementContext;

import org.xml.sax.Attributes;

public class SummaryContext extends ElementContext
{
	public SummaryContext(DerefereeContext parent, Attributes attributes)
	{
		int totalBytes = getAttrInt(attributes, "total-bytes-allocated");
		int maxBytes = getAttrInt(attributes, "max-bytes-in-use");

		int callsNew = getAttrInt(attributes, "calls-to-new");
		int callsDelete = getAttrInt(attributes, "calls-to-delete");
		int callsArrayNew = getAttrInt(attributes, "calls-to-array-new");
		int callsArrayDelete = getAttrInt(attributes, "calls-to-array-delete");
		int callsDeleteNull = getAttrInt(attributes, "calls-to-delete-null");
		
		DerefereeSummary info = new DerefereeSummary(totalBytes, maxBytes,
				callsNew, callsDelete, callsArrayNew,
				callsArrayDelete, callsDeleteNull);
		parent.setSummary(info);
	}
	
	private int getAttrInt(Attributes attributes, String name)
	{
		return Integer.parseInt(attributes.getValue(name));
	}
}
