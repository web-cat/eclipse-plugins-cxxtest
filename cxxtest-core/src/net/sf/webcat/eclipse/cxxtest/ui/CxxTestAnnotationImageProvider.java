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
package net.sf.webcat.eclipse.cxxtest.ui;

import net.sf.webcat.eclipse.cxxtest.CxxTestPlugin;
import net.sf.webcat.eclipse.cxxtest.ICxxTestConstants;
import net.sf.webcat.eclipse.cxxtest.model.ICxxTestBase;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.texteditor.IAnnotationImageProvider;
import org.eclipse.ui.texteditor.MarkerAnnotation;

/**
 * A class that provides the left margin images for CxxTest markers/annotations
 * based on the status code of the assertion they represent.
 * 
 * @author Tony Allevato (Virginia Tech Computer Science)
 */
public class CxxTestAnnotationImageProvider implements IAnnotationImageProvider
{
	private static ImageRegistry registry;
	
	public Image getManagedImage(Annotation annotation)
	{
		if(annotation instanceof MarkerAnnotation)
		{
			MarkerAnnotation ma = (MarkerAnnotation)annotation;
			IMarker marker = ma.getMarker();
			
			try
			{
				if(ICxxTestConstants.MARKER_FAILED_TEST.equals(marker.getType()))
				{
					int assertType = marker.getAttribute(
							ICxxTestConstants.ATTR_ASSERTIONTYPE,
							ICxxTestBase.STATUS_OK);
					
					return getImage(assertType);
				}
			}
			catch (CoreException e) { }
		}

		return null;
	}

	private Image getImage(int type)
	{
		if(registry == null)
			initializeRegistry();
		
		switch(type)
		{
			case ICxxTestBase.STATUS_OK:
				return registry.get("trace");

			case ICxxTestBase.STATUS_WARNING:
				return registry.get("warn");

			case ICxxTestBase.STATUS_FAILED:
				return registry.get("fail");

			case ICxxTestBase.STATUS_ERROR:
				return registry.get("error");
				
			default:
				return null;
		}
	}

	private static void initializeRegistry()
	{
		registry = new ImageRegistry(Display.getCurrent());

		registry.put("trace", CxxTestPlugin.getImageDescriptor(
			"/icons/full/obj16/asserttrace.gif"));
		registry.put("warn", CxxTestPlugin.getImageDescriptor(
			"/icons/full/obj16/assertwarn.gif"));
		registry.put("fail", CxxTestPlugin.getImageDescriptor(
			"/icons/full/obj16/assertfail.gif"));
		registry.put("error", CxxTestPlugin.getImageDescriptor(
			"/icons/full/obj16/asserterror.gif"));
	}

	public String getImageDescriptorId(Annotation annotation)
	{
		// not supported (managed images only)
		return null;
	}

	public ImageDescriptor getImageDescriptor(String imageDescritporId)
	{
		// not supported (managed images only)
		return null;
	}
}
