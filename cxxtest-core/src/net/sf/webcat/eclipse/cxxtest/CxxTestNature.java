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
package net.sf.webcat.eclipse.cxxtest;

import org.eclipse.cdt.managedbuilder.core.BuildException;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IManagedBuildInfo;
import org.eclipse.cdt.managedbuilder.core.IManagedProject;
import org.eclipse.cdt.managedbuilder.core.IOption;
import org.eclipse.cdt.managedbuilder.core.ITool;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.core.resources.ICommand;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IProjectNature;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

/**
 * The project nature attached to CxxTest projects.
 * 
 * @author Tony Allowatt (Virginia Tech Computer Science)
 */
public class CxxTestNature implements IProjectNature
{
	private IProject project;

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.resources.IProjectNature#configure()
	 */
	public void configure() throws CoreException
	{
		addBuilders(getProject());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.resources.IProjectNature#deconfigure()
	 */
	public void deconfigure() throws CoreException
	{
		removeBuilder(getProject(), CxxTestPlugin.CXXTEST_BUILDER);
		removeBuilder(getProject(), CxxTestPlugin.CXXTEST_RUNNER);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.resources.IProjectNature#getProject()
	 */
	public IProject getProject()
	{
		return project;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.resources.IProjectNature#setProject(org.eclipse.core.resources.IProject)
	 */
	public void setProject(IProject project)
	{
		this.project = project;
	}

	public static boolean hasNature(IProject project) throws CoreException
	{
		String natureId = CxxTestPlugin.CXXTEST_NATURE;
		IProjectDescription description = project.getDescription();
		String[] natureIds = description.getNatureIds();
		int index = -1;

		for(int i = 0; i < natureIds.length; ++i)
		{
			if(natureIds[i].equals(natureId))
			{
				index = i;
			}
		}

		return index != -1;
	}

	public static boolean addNature(IProject project, IProgressMonitor monitor)
			throws CoreException
	{
		IProjectDescription description = project.getDescription();
		String[] natureIds = description.getNatureIds();

		int index = -1;

		for(int i = 0; i < natureIds.length; ++i)
		{
			if(natureIds[i].equals(CxxTestPlugin.CXXTEST_NATURE))
			{
				index = i;
				break;
			}
		}

		try
		{
			if(index == -1)
			{
				String[] newNatureIds = new String[natureIds.length + 1];
				System.arraycopy(natureIds, 0, newNatureIds, 1,
						natureIds.length);

				newNatureIds[0] = CxxTestPlugin.CXXTEST_NATURE;

				description.setNatureIds(newNatureIds);
				project.setDescription(description, monitor);
			}
		}
		catch(CoreException ex)
		{
			if(description != null && natureIds != null)
			{
				description.setNatureIds(natureIds);
				project.setDescription(description, monitor);
			}

			throw ex;
		}

		addIncludePath(project);

		return index == -1;
	}

	private static void addIncludePath(IProject project)
	{
		String includePath = "\"" +
			CxxTestPlugin.getDefault().getCxxTestInstallPath() + "\"";

		IManagedBuildInfo buildInfo = ManagedBuildManager.getBuildInfo(project);
		IManagedProject managedProject = buildInfo.getManagedProject();

		IConfiguration[] configs = managedProject.getConfigurations();
		for(int confIndex = 0; confIndex < configs.length; confIndex++)
		{
			ITool[] tools = configs[confIndex].getTools();

			for(int toolIndex = 0; toolIndex < tools.length; toolIndex++)
			{
				ITool tool = tools[toolIndex];
				if(tool.getId().startsWith("cdt.managedbuild.tool.gnu.cpp.compiler"))
				{
					IOption includesOption = tool
						.getOptionById("gnu.cpp.compiler.option.include.paths");

					try
					{
						String[] includes = includesOption.getIncludePaths();

						// Don't do anything if the path is already in the
						// include path list.
						boolean includeFound = false;
						for(int incIndex = 0; incIndex < includes.length; incIndex++)
						{
							if(includes[incIndex].equals(includePath))
							{
								includeFound = true;
								break;
							}
						}

						if(!includeFound)
						{
							String[] newIncludes = new String[includes.length + 1];
							System.arraycopy(includes, 0, newIncludes, 0,
									includes.length);
							newIncludes[includes.length] = includePath;
	
							ManagedBuildManager.setOption(configs[confIndex],
									tool, includesOption, newIncludes);
	
							ManagedBuildManager.saveBuildInfo(project, true);
						}
					}
					catch(BuildException e)
					{
						e.printStackTrace();
					}							
				}
			}
		}		
	}

	public static void addBuilders(IProject project) throws CoreException
	{
		IProjectDescription description = project.getDescription();
		ICommand[] commands = description.getBuildSpec();

		for(int i = 0; i < commands.length; i++)
		{
			if(commands[i].getBuilderName().equals(
					CxxTestPlugin.CXXTEST_BUILDER))
				return;
		}

		ICommand builderCommand = description.newCommand();
		builderCommand.setBuilderName(CxxTestPlugin.CXXTEST_BUILDER);

		ICommand runnerCommand = description.newCommand();
		runnerCommand.setBuilderName(CxxTestPlugin.CXXTEST_RUNNER);

		ICommand[] newCommands = new ICommand[commands.length + 2];
		System.arraycopy(commands, 0, newCommands, 1, commands.length);

		newCommands[0] = builderCommand;
		newCommands[newCommands.length - 1] = runnerCommand;

		description.setBuildSpec(newCommands);
		project.setDescription(description, null);
	}

	public static boolean removeNature(IProject project)
			throws CoreException
	{
		IProjectDescription description = project.getDescription();
		String[] natureIds = description.getNatureIds();

		for(int i = 0; i < natureIds.length; ++i)
		{
			if(natureIds[i].equals(CxxTestPlugin.CXXTEST_NATURE))
			{
				String[] newNatureIds = new String[natureIds.length - 1];

				System.arraycopy(natureIds, 0, newNatureIds, 0, i);
				System.arraycopy(natureIds, i + 1, newNatureIds, i,
						natureIds.length - i - 1);

				try
				{
					description.setNatureIds(newNatureIds);
					project.setDescription(description, null);
				}
				catch(CoreException ex)
				{
					if(description != null && natureIds != null)
					{
						description.setNatureIds(natureIds);
						project.setDescription(description, null);
					}

					throw ex;
				}

				return true;
			}
		}

		return false;
	}

	protected static boolean removeBuilder(IProject project, String builderId)
			throws CoreException
	{
		IProjectDescription description = project.getDescription();
		ICommand[] commands = description.getBuildSpec();

		for(int i = 0; i < commands.length; ++i)
		{
			if(commands[i].getBuilderName().equals(builderId))
			{
				ICommand[] newCommands = new ICommand[commands.length - 1];

				System.arraycopy(commands, 0, newCommands, 0, i);
				System.arraycopy(commands, i + 1, newCommands, i,
						commands.length - i - 1);

				description.setBuildSpec(newCommands);
				project.setDescription(description, null);

				return true;
			}
		}

		return false;
	}
}
