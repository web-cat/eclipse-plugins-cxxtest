package net.sf.webcat.eclipse.cxxtest.bfd.options;

import net.sf.webcat.eclipse.cxxtest.bfd.StaticLibraryManager;
import net.sf.webcat.eclipse.cxxtest.options.IExtraOptionsEnablement;

import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.core.resources.IProject;

public class NeedsLibIntlCondition implements IExtraOptionsEnablement
{
	public boolean shouldProcessOptions(IProject project,
	        IConfiguration configuration)
	{
		return StaticLibraryManager.getInstance().shouldAddIntlToBuild();
	}
}
