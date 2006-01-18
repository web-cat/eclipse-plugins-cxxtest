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

import java.io.File;
import java.io.IOException;
import java.util.Map;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.managedbuilder.core.IManagedBuildInfo;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jface.preference.IPreferenceStore;

/**
 * This builder iterates through the classes in the C++ project, determines
 * which ones represent CxxTest test suites, and generates the test runner
 * source file.
 * 
 * @author Tony Allowatt (Virginia Tech Computer Science)
 * @author Stephen Edwards  (Virginia Tech Computer Science)
 */
public class CxxTestDriverBuilder extends IncrementalProjectBuilder
{
	/**
	 * Called by the IDE when the project is built. This method traverses the
	 * project DOM, collects the test cases, and builds the test runner source
	 * file.
	 */
	protected IProject[] build(int kind, Map args, IProgressMonitor monitor)
			throws CoreException
	{
		IProject project = getProject();

		if(kind == AUTO_BUILD || kind == INCREMENTAL_BUILD)
		{
			if(!checkForRebuild())
			{
				// We don't need to rebuild the test case runner, so bail out.
				monitor.done();
				return null;
			}

			ICProject cproject = CCorePlugin.getDefault().getCoreModel().create(project);

			monitor.beginTask("Generating CxxTest Driver", 3);

			// Delete the existing test runner source file.
			String outputFile = getDriverFileName();
			IFile outputFileRsrc = project.getFile(outputFile);
			outputFileRsrc.delete(true, monitor);
			
			// Walk down the project DOM tree and find all the classes that
			// are CxxTest test suites.
			TestCaseVisitor visitor = new TestCaseVisitor();
			cproject.accept(visitor);
			monitor.worked(1);

			CxxTestSuiteInfo[] suites = visitor.getTestSuites();

			IPreferenceStore store = CxxTestPlugin.getDefault().getPreferenceStore();

			try
			{
				// Generate the .cpp file that will run all the tests.
				File projectDir = getProject().getLocation().toFile();
				String fullPath = projectDir.toString() + "/" + outputFile;
					
				boolean trackHeap = store.getBoolean(
							CxxTestPlugin.CXXTEST_PREF_TRACK_HEAP);
				boolean trapSignals = store.getBoolean(
							CxxTestPlugin.CXXTEST_PREF_TRAP_SIGNALS);

				CxxTestDriverGenerator generator = new CxxTestDriverGenerator(
						cproject, fullPath, suites);
				generator.setUsingStandardLibrary(visitor.isUsingStandardLibrary());
				generator.setTrackHeap(trackHeap);
				generator.setTrapSignals(trapSignals);
				generator.setMainProvided(visitor.getMainExists());
				
				generator.buildDriver();
			}
			catch(IOException e)
			{
				setProblemMarker(ICxxTestConstants.MARKER_INVOCATION_PROBLEM,
						"An error occurred when generating the test runner " +
						"source file: " + e.getMessage());
			}

			monitor.worked(1);

			// Refresh the project so it recognizes the new source file and
			// rebuilds appropriately.
			outputFileRsrc.refreshLocal(IResource.DEPTH_ZERO,
					new SubProgressMonitor(monitor, 1));
			
			monitor.done();
		}
		
		return null;
	}

	public boolean checkForRebuild()
	{
		IProject project = getProject();
		IResourceDelta delta = getDelta(project);

		boolean changeRequiresRebuild = false;

		// If the delta is null, the documentation says to assume
		// "unspecified changes" have occurred and do something
		// appropriate, so we'll go ahead and do a rebuild.
		if (delta != null)
		{
			IManagedBuildInfo buildInfo = ManagedBuildManager
					.getBuildInfo(project);
			String[] configurations = buildInfo.getConfigurationNames();

			// We don't want to rebuild the test-case runner if the
			// changes to the project were isolated to one of the
			// configuration binary directories, or we might get stuck
			// in an infinite loop.
			IResourceDelta[] children = delta.getAffectedChildren();

			for(int i = 0; i < children.length; i++)
			{
				// Now, search through the binary build dirs for a match
				int j;
				for (j = 0; j < configurations.length; j++)
				{
					IPath configPath = project.getFolder(configurations[j])
							.getFullPath();
					if (configPath.equals(children[i].getFullPath()))
						break;
				}

				// Next, check for pesky stackdump files
				IResource resource = children[i].getResource();
				String fileExt = resource.getFileExtension();
				boolean isStackDump = (fileExt != null && fileExt
						.endsWith("stackdump"))
						|| (resource.getType() == IResource.FILE && resource
								.getName().startsWith("core"));
				boolean notInConfigBuildDir = j == configurations.length;

				// Finally, check for the test results XML file.
				boolean isResultsFile = resource.getName().equals(
					ICxxTestConstants.TEST_RESULTS_FILE);

				// If the child isn't in any of the configuration-based
				// binary build directories in the project and isn't a
				// stack dump file ...
				if(notInConfigBuildDir && !isStackDump && !isResultsFile)
				{
					changeRequiresRebuild = true;
					break;
				}
			}
		}
		
		return changeRequiresRebuild;
	}

	/**
	 * Gets the name of the test runner source file as specified in the
	 * workbench preferences.
	 * 
	 * @return the name of the test runner source file that will be generated.
	 */
	private String getDriverFileName()
	{
		IPreferenceStore store = CxxTestPlugin.getDefault().getPreferenceStore();

		String outputFile = store.getString(
						CxxTestPlugin.CXXTEST_PREF_DRIVER_FILENAME);
	
		if (outputFile != null)
			outputFile = outputFile.trim();
	
		if (outputFile == null || outputFile.length() == 0)
			outputFile = "runAllTests.cpp";
		
		return outputFile;
	}

	/**
	 * Creates a generic problem marker in the project with the specified
	 * id and message.
	 *  
	 * @param id the id of the marker to create.
	 * @param message the message to create with the marker.
	 * 
	 * @throws CoreException if creating the marker fails
	 */
	private void setProblemMarker(String id, String message)
			throws CoreException
	{
		IMarker marker = getProject().createMarker(id);
		marker.setAttribute(IMarker.MESSAGE, message);
		marker.setAttribute(IMarker.SEVERITY, IMarker.SEVERITY_ERROR);
	}

	/**
	 * Called when the user cleans the project. Here we want to delete any
	 * markers that may have been generated by the test runner. 
	 */
	protected void clean(IProgressMonitor monitor) throws CoreException
	{
		super.clean(monitor);
	}
}
