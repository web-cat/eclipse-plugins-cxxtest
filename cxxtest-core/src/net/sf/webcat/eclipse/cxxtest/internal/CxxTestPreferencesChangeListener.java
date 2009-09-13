/*==========================================================================*\
 |  $Id$
 |*-------------------------------------------------------------------------*|
 |  Copyright (C) 2006-2009 Virginia Tech 
 |
 |	This file is part of Web-CAT Eclipse Plugins.
 |
 |	Web-CAT is free software; you can redistribute it and/or modify
 |	it under the terms of the GNU General Public License as published by
 |	the Free Software Foundation; either version 2 of the License, or
 |	(at your option) any later version.
 |
 |	Web-CAT is distributed in the hope that it will be useful,
 |	but WITHOUT ANY WARRANTY; without even the implied warranty of
 |	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 |	GNU General Public License for more details.
 |
 |	You should have received a copy of the GNU General Public License
 |	along with Web-CAT; if not, see <http://www.gnu.org/licenses/>.
\*==========================================================================*/

package net.sf.webcat.eclipse.cxxtest.internal;

import net.sf.webcat.eclipse.cxxtest.CxxTestPlugin;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;

/**
 * 
 * @author  Tony Allevato (Virginia Tech Computer Science)
 * @author  latest changes by: $Author$
 * @version $Revision$ $Date$
 */
public class CxxTestPreferencesChangeListener implements
		IPropertyChangeListener
{
	public void propertyChange(PropertyChangeEvent event)
	{
//		if (CxxTestPlugin.CXXTEST_PREF_TRACE_STACK.equals(
//				event.getProperty()))
		{
			boolean oldValue = (Boolean) event.getOldValue();
			boolean newValue = (Boolean) event.getNewValue();
			
			if (oldValue != newValue)
			{
				stackTracingWasChanged(newValue);
			}
		}
	}
	
	
	private void stackTracingWasChanged(boolean enabled)
	{
		IPreferenceStore store =
			CxxTestPlugin.getDefault().getPreferenceStore();
		String driverFile = store.getString(
				CxxTestPlugin.CXXTEST_PREF_DRIVER_FILENAME);

		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IProject[] projects = workspace.getRoot().getProjects();
		
		for (IProject project : projects)
		{
			if (!project.isOpen())
			{
				continue;
			}
			
			try
			{
				if (project.hasNature(CxxTestPlugin.CXXTEST_NATURE))
				{
					IFile driver = project.getFile(driverFile);

					if (driver != null)
					{
						driver.delete(true, null);
					}
				}
			}
			catch (CoreException e)
			{
				e.printStackTrace();
			}
		}
	}
}
