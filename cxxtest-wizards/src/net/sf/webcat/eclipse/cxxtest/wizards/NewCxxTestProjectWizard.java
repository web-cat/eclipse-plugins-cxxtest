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

import java.util.ArrayList;

import net.sf.webcat.eclipse.cxxtest.CxxTestNature;
import net.sf.webcat.eclipse.cxxtest.CxxTestPlugin;

import org.eclipse.cdt.managedbuilder.core.BuildException;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IManagedBuildInfo;
import org.eclipse.cdt.managedbuilder.core.IOption;
import org.eclipse.cdt.managedbuilder.core.ITool;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.cdt.managedbuilder.ui.wizards.NewManagedCCProjectWizard;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.preference.IPreferenceStore;

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
			
			IPreferenceStore store = CxxTestPlugin.getDefault().getPreferenceStore();
			boolean stackTrace = store.getBoolean(CxxTestPlugin.CXXTEST_PREF_TRACE_STACK);
			
			if(stackTrace)
				addStackTraceOptions(project);
		}
		catch (CoreException e) { }

		return retval;
	}
	
	private String[] addToArray(String[] array, String[] newEntries)
	{
		String[] newArray = new String[array.length + newEntries.length];
		
		System.arraycopy(array, 0, newArray, 0, array.length);
		System.arraycopy(newEntries, 0, newArray, array.length, newEntries.length);
		
		return newArray;
	}
	
	private String[] removeFromArray(String[] array, String[] remEntries)
	{
		ArrayList list = new ArrayList();
		for(int i = 0; i < array.length; i++)
			list.add(array[i]);

		for(int i = 0; i < remEntries.length; i++)
		{
			boolean removed = false;
			do { removed = list.remove(remEntries[i]); } while(removed);
		}
		
		String[] newArray = new String[list.size()];
		list.toArray(newArray);
		return newArray;
	}

	private void addStackTraceOptions(IProject project)
	{
		IManagedBuildInfo buildInfo = ManagedBuildManager.getBuildInfo(project);
		ITool[] tools;

		IConfiguration[] configs = buildInfo.getManagedProject().getConfigurations();
		for(int j = 0; j < configs.length; j++)
		{
			IConfiguration config = configs[j];
			if(!"Debug".equals(config.getName()))
				continue;

			tools = config.getToolsBySuperClassId("cdt.managedbuild.tool.gnu.cpp.compiler");
			for(int i = 0; i < tools.length; i++)
			{
				IOption otherOption = tools[i].getOptionById(
						"gnu.cpp.compiler.option.other.other");
				try
				{
					String other = otherOption.getStringValue();
					other += " -finstrument-functions";
	
					ManagedBuildManager.setOption(config, tools[i], otherOption, other);
				}
				catch(BuildException e)
				{
					e.printStackTrace();
				}							
			}
	
			tools = config.getToolsBySuperClassId("cdt.managedbuild.tool.gnu.cpp.linker");
			for(int i = 0; i < tools.length; i++)
			{
				IOption libsOption = tools[i].getOptionById(
						"gnu.cpp.link.option.libs");
				try
				{
					String[] libs = libsOption.getLibraries();
					libs = addToArray(libs, new String[] { "bfd", "iberty", "intl" });

					ManagedBuildManager.setOption(config, tools[i], libsOption, libs);
				}
				catch(BuildException e)
				{
					e.printStackTrace();
				}							
			}
		}
		ManagedBuildManager.saveBuildInfo(project, true);
	}
}
