package net.sf.webcat.eclipse.cxxtest;

import org.eclipse.core.runtime.IProgressMonitor;

public interface IStackTraceDependencyCheck
{
	boolean checkForDependencies(IProgressMonitor monitor);
	
	String missingDependencies();
}
