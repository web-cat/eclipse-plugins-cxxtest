package net.sf.webcat.eclipse.cxxtest.bfd.options;

import net.sf.webcat.eclipse.cxxtest.bfd.StaticLibraryManager;
import net.sf.webcat.eclipse.cxxtest.options.StackTraceEnabledCondition;

import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.core.resources.IProject;

public class StackTraceEnabledAndNeedsLibIbertyCondition extends
		StackTraceEnabledCondition
{
	public boolean shouldProcessOptions(IProject project,
	        IConfiguration configuration)
	{
		boolean enabled = super.shouldProcessOptions(project, configuration);
		boolean hasLibIberty =
			StaticLibraryManager.getInstance().shouldAddIbertyToBuild();

		return enabled && hasLibIberty;
	}
}
