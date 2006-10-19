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
package net.sf.webcat.eclipse.cxxtest.bfd;

import java.io.IOException;
import java.net.URL;

import net.sf.webcat.eclipse.cxxtest.CxxTestPlugin;
import net.sf.webcat.eclipse.cxxtest.options.IExtraProjectOptions;
import net.sf.webcat.eclipse.cxxtest.options.ProjectOptionsUtil;

import org.eclipse.cdt.managedbuilder.core.BuildException;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.ITool;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.preference.IPreferenceStore;

public class ExtraProjectOptions implements IExtraProjectOptions
{
	public String getEntryPath(String entry)
	{
		String path = null;

		try
		{
			URL entryURL = FileLocator.find(
					CxxTestPlugin.getDefault().getBundle(), new Path(entry), null);
			URL url = FileLocator.resolve(entryURL);
			path = url.getFile();

			// This special check is somewhat shady, but it looks like it's
			// the only way to handle a Windows path properly, since Eclipse
			// returns a string like "/C:/folder/...".
			if(path.charAt(2) == ':')
				path = path.substring(1);
			
			path = new Path(path).toOSString();
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}

		return path;
	}

	public void addOptions(IProject project, IConfiguration configuration)
	{
		if(!"Debug".equals(configuration.getName()))
			return;
		
		IPreferenceStore store = CxxTestPlugin.getDefault().getPreferenceStore();
		boolean stackTrace = store.getBoolean(CxxTestPlugin.CXXTEST_PREF_TRACE_STACK);

		if(stackTrace)
		{
			try
			{
				addStackTraceOptions(project, configuration);
			}
			catch(BuildException e)
			{
				e.printStackTrace();
			}
		}
	}
	
	public void removeOptions(IProject project, IConfiguration configuration)
	{
	}

	private void addStackTraceOptions(IProject project, IConfiguration config)
		throws BuildException
	{
		String includePath = "\"" + getEntryPath("/symreader-src/") + "\"";

		ITool[] tools = config.getToolsBySuperClassId("cdt.managedbuild.tool.gnu.cpp.compiler");
		for(int i = 0; i < tools.length; i++)
		{
			ProjectOptionsUtil.addToString(config, tools[i],
					"gnu.cpp.compiler.option.other.other",
					new String[] { "-finstrument-functions" });

			ProjectOptionsUtil.addToIncludes(config, tools[i],
					"gnu.cpp.compiler.option.include.paths",
					new String[] { includePath });

			ProjectOptionsUtil.addToDefinedSymbols(config, tools[i],
					"gnu.cpp.compiler.option.preprocessor.def",
					new String[] { "CXXTEST_INCLUDE_SYMREADER_DIRECTLY" });
		}
		
		tools = config.getToolsBySuperClassId("cdt.managedbuild.tool.gnu.cpp.linker");
		for(int i = 0; i < tools.length; i++)
		{
			ProjectOptionsUtil.addToLibraries(config, tools[i],
					"gnu.cpp.link.option.libs",
					new String[] { "bfd", "iberty", "intl" });
		}
	}
}
