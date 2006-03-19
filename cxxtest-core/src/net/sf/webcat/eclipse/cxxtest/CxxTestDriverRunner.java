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

import java.util.Map;

import net.sf.webcat.eclipse.cxxtest.ui.TestRunnerViewPart;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.debug.core.ICDTLaunchConfigurationConstants;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IManagedBuildInfo;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.swt.widgets.Display;

/**
 * This builder executes the generates test-case executable and adds problem
 * markers to the source file for any failed test cases. It also populates the
 * CxxTest view with information about the executed tests.
 * 
 * @author Tony Allowatt (Virginia Tech Computer Science)
 */
public class CxxTestDriverRunner extends IncrementalProjectBuilder
{
	private class RunnerLaunchThread extends Thread
	{
		private IProject project;

		public RunnerLaunchThread(IProject project)
		{
			this.project = project;
			start();
		}

		public void run()
		{
			try
			{
				final ICProject cproject =
					CCorePlugin.getDefault().getCoreModel().create(project);

				IPath exePath = getExecutableFile().getProjectRelativePath();

				ILaunchManager launchManager = DebugPlugin.getDefault().getLaunchManager();
				ILaunchConfigurationType type = launchManager.getLaunchConfigurationType(
						ICDTLaunchConfigurationConstants.ID_LAUNCH_C_APP);
				ILaunchConfigurationWorkingCopy config = type.newInstance(null, "CxxTestRunner");
	
				config.setAttribute(IDebugUIConstants.ATTR_PRIVATE, true);
				config.setAttribute(IDebugUIConstants.ATTR_LAUNCH_IN_BACKGROUND, false);

				config.setAttribute(ICDTLaunchConfigurationConstants.ATTR_PROJECT_NAME,
						project.getName());
				config.setAttribute(ICDTLaunchConfigurationConstants.ATTR_PROGRAM_NAME,
						exePath.toString());
	
				final ILaunch launch = config.launch(ILaunchManager.RUN_MODE, null);
	
				Display.getDefault().syncExec(new Runnable() {
					public void run()
					{
						TestRunnerViewPart runnerPart =
							CxxTestPlugin.getDefault().getTestRunnerView();
						runnerPart.testRunStarted(cproject, launch);
					}
				});

				// Wait for the launched process to complete.
				waitForProcess(launch);

				// Parse the CxxTest results file and enter the results in the
				// CxxTest view.
				Display.getDefault().syncExec(new Runnable() {
					public void run()
					{
						TestRunnerViewPart runnerPart =
							CxxTestPlugin.getDefault().getTestRunnerView();
						runnerPart.testRunEnded();
					}
				});				
			}
			catch(CoreException e)
			{
			}
		}
		
		private void waitForProcess(ILaunch launch)
		{
			try
			{
				while(!launch.isTerminated())
					Thread.sleep(250);
			}
			catch(InterruptedException e) { }
		}
	}

	private IFile getExecutableFile()
	{
		IProject project = getProject();

		IManagedBuildInfo buildInfo =
			ManagedBuildManager.getBuildInfo(project);

		IConfiguration configuration = buildInfo.getDefaultConfiguration();

		String exeName = buildInfo.getBuildArtifactName();
		String exeExtension = buildInfo.getBuildArtifactExtension();

		if(exeExtension.length() > 0)
			exeName += "." + exeExtension;

		IFile file = project.getFile(configuration.getName() + "/" + exeName);
		return file;
	}

	private long lastModifiedStamp = 0;

	/* (non-Javadoc)
	 * @see org.eclipse.core.internal.events.InternalBuilder#build(int, java.util.Map, org.eclipse.core.runtime.IProgressMonitor)
	 */
	protected IProject[] build(int kind, Map args, IProgressMonitor monitor)
			throws CoreException
	{
		IProject project = getProject();

   		long currModifiedStamp = getExecutableFile().getModificationStamp();
       	if(currModifiedStamp == lastModifiedStamp)
		{
			// We don't need to rebuild the test case runner, so bail out.
			monitor.done();
			return null;
		}

        monitor.beginTask("Running CxxTest driver", 1);
		deleteMarkers();

		// If there are any problems in the project after it has been
		// built, we probably shouldn't try to run the driver code.
        // UPDATE: We need to be more specific here, even warnings were
        // preventing the runner from executing.
		IMarker[] problems = project.findMarkers(IMarker.PROBLEM, true,
				IResource.DEPTH_INFINITE);
		if(problems.length > 0)
		{
			for(int i = 0; i < problems.length; i++)
			{
				if(problems[i].getAttribute(IMarker.SEVERITY,
						IMarker.SEVERITY_INFO) == IMarker.SEVERITY_ERROR)
				{
					monitor.done();
					return null;
				}
			}
		}

		monitor.worked(1);
		new RunnerLaunchThread(project);

		monitor.done();
		
		// Cache the last modified timestamp of the test runner executable.
		// Next time this builder is invoked, we won't execute the
		// auto-runner if the executable hasn't changed.
		lastModifiedStamp = getExecutableFile().getModificationStamp();

		return null;
	}

	protected void clean(IProgressMonitor monitor) throws CoreException
	{
		super.clean(monitor);
		
		lastModifiedStamp = 0;
		deleteMarkers();
	}

	/**
	 * Delete any markers that were generated by the test runner.
	 * 
	 * @throws CoreException if a problem occurs when deleting the markers.
	 */
	private void deleteMarkers() throws CoreException
	{
		final IProject project = getProject();

		IWorkspaceRunnable runnable = new IWorkspaceRunnable() {
			public void run(IProgressMonitor monitor)
			{
				try
				{
					project.deleteMarkers(ICxxTestConstants.MARKER_INVOCATION_PROBLEM,
							true, IResource.DEPTH_INFINITE);
					project.deleteMarkers(ICxxTestConstants.MARKER_FAILED_TEST, true,
							IResource.DEPTH_INFINITE);
				}
				catch (CoreException e) { }
			}
		};

		project.getWorkspace().run(runnable, new NullProgressMonitor());
	}
}
