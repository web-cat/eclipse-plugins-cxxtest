package net.sf.webcat.eclipse.cxxtest.options;

import net.sf.webcat.eclipse.cxxtest.CxxTestPlugin;

import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.core.resources.IProject;

public class StackTraceEnabledNeedsLibIntlCondition extends
		StackTraceEnabledCondition
{
	public boolean shouldProcessOptions(IProject project,
	        IConfiguration configuration)
	{
		boolean enabled = super.shouldProcessOptions(project, configuration);
		boolean hasLibIntl = CxxTestPlugin.getDefault().systemHasLibIntl();
		
		return enabled && hasLibIntl;
	}
}
