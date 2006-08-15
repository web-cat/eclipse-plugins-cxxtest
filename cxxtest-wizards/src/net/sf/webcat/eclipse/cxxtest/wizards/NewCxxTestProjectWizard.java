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
package net.sf.webcat.eclipse.cxxtest.wizards;

import net.sf.webcat.eclipse.cxxtest.CxxTestNature;

import org.eclipse.cdt.managedbuilder.ui.wizards.NewManagedCCProjectWizard;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;

/**
 * Extends the standard Managed C++ Project wizard to add the CxxTest nature and
 * other settings to the new project.
 * 
 * @author Tony Allowatt (Virginia Tech Computer Science)
 */
public class NewCxxTestProjectWizard extends NewManagedCCProjectWizard
{
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.wizard.IWizard#performFinish()
	 */
	public boolean performFinish()
	{
		boolean retval = super.performFinish();

		// Add our own nature to the project.
		IProject project = getNewProject();

		// Add the CxxTest nature to the project. This will also
		// add the include path to the gcc settings.
		try
		{
			CxxTestNature.addNature(project, new NullProgressMonitor());
		}
		catch (CoreException e)
		{
			e.printStackTrace();
		}

		return retval;
	}
}
