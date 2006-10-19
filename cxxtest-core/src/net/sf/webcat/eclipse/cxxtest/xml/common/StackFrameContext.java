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
package net.sf.webcat.eclipse.cxxtest.xml.common;

import org.xml.sax.Attributes;

import net.sf.webcat.eclipse.cxxtest.internal.model.CxxTestStackFrame;
import net.sf.webcat.eclipse.cxxtest.xml.ElementContext;

public class StackFrameContext extends ElementContext
{
	public StackFrameContext(IStackFrameConsumer frameConsumer, Attributes attributes)
	{
		String function = attributes.getValue("function");
		String file = null;
		int lineNumber = 0;
		
		String fileLine = attributes.getValue("location");
		if(fileLine != null)
		{
			int colonPos = fileLine.lastIndexOf(':');
			
			if(colonPos != -1)
			{
				file = fileLine.substring(0, colonPos);

				try
				{
					lineNumber = Integer.parseInt(fileLine.substring(colonPos + 1));
				}
				catch(NumberFormatException e)
				{
					file = fileLine;
					lineNumber = 0;
				}
			}
			else
			{
				file = fileLine;
			}
		}
			
		CxxTestStackFrame frame = new CxxTestStackFrame(function, file, lineNumber);
		frameConsumer.addStackFrame(frame);
	}
}
