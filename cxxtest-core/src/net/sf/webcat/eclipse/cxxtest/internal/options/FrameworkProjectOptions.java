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
package net.sf.webcat.eclipse.cxxtest.internal.options;

import org.eclipse.cdt.managedbuilder.core.BuildException;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.ITool;
import org.eclipse.core.resources.IProject;

import net.sf.webcat.eclipse.cxxtest.framework.FrameworkPlugin;
import net.sf.webcat.eclipse.cxxtest.options.IExtraProjectOptions;
import net.sf.webcat.eclipse.cxxtest.options.ProjectOptionsUtil;

/**
 * This project options extension adds the CxxTest include path to the project
 * when it is created.
 * 
 * @author Tony Allowatt (Virginia Tech Computer Science)
 */
public class FrameworkProjectOptions implements IExtraProjectOptions
{
	public void addOptions(IProject project, IConfiguration config)
	{
		try
		{
			String cxxTestPath = "\"" + FrameworkPlugin.getDefault().getFrameworkPath() + "\"";

			ITool[] tools = config.getToolsBySuperClassId("cdt.managedbuild.tool.gnu.cpp.compiler");
			for(int i = 0; i < tools.length; i++)
			{
				ProjectOptionsUtil.addToIncludes(config, tools[i],
						"gnu.cpp.compiler.option.include.paths",
						new String[] { cxxTestPath });
			}
		}
		catch(BuildException e)
		{
			e.printStackTrace();
		}
	}

	public void removeOptions(IProject project, IConfiguration configuration)
	{
	}
}
