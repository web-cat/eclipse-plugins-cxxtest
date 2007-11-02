package net.sf.webcat.eclipse.cxxtest.options;

import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.core.resources.IProject;

public interface IExtraOptionsEnablement
{
	boolean shouldProcessOptions(IProject project, IConfiguration configuration);
}
