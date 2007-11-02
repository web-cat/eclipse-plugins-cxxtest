package net.sf.webcat.eclipse.cxxtest.options;

import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.core.resources.IProject;
import org.eclipse.jface.preference.IPreferenceStore;

import net.sf.webcat.eclipse.cxxtest.CxxTestPlugin;

public class StackTraceEnabledCondition implements IExtraOptionsEnablement
{
	public boolean shouldProcessOptions(IProject project,
	        IConfiguration configuration)
	{
		IPreferenceStore store =
	        CxxTestPlugin.getDefault().getPreferenceStore();
		
		boolean stackTrace =
	        store.getBoolean(CxxTestPlugin.CXXTEST_PREF_TRACE_STACK);
		
		return stackTrace;
	}
}
