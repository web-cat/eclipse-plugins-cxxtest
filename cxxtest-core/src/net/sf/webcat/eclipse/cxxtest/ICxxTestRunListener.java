package net.sf.webcat.eclipse.cxxtest;

import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.debug.core.ILaunch;

public interface ICxxTestRunListener
{
	void testRunStarted(ICProject project, ILaunch launch);

	void testRunEnded();
}
