package net.sf.webcat.eclipse.cxxtest.bfd;

import net.sf.webcat.eclipse.cxxtest.IStackTraceDependencyCheck;

import org.eclipse.core.runtime.IProgressMonitor;

public class CheckForRequiredLibraries implements IStackTraceDependencyCheck
{
	public boolean checkForDependencies(IProgressMonitor monitor)
	{
		StaticLibraryManager manager = StaticLibraryManager.getInstance();
		
		manager.checkForDependencies(monitor);
		missingDependencies = manager.getMissingLibraryString();

		return (missingDependencies == null);
	}


	public String missingDependencies()
	{
		return missingDependencies;
	}


	private String missingDependencies = null;
}
