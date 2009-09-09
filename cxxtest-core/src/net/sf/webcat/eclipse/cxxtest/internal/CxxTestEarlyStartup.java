package net.sf.webcat.eclipse.cxxtest.internal;

import net.sf.webcat.eclipse.cxxtest.CxxTestPlugin;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ui.IStartup;

public class CxxTestEarlyStartup implements IStartup
{
	public void earlyStartup()
	{
		IPreferenceStore store =
			CxxTestPlugin.getDefault().getPreferenceStore();

		// Force the check modally if this is the first time the plugin is
		// begin instantiated.

		boolean firstTime = store.getBoolean(
				CxxTestPlugin.CXXTEST_PREF_FIRST_TIME);

		StackTraceDependencyChecker.checkForDependencies(firstTime);

		store.setValue(CxxTestPlugin.CXXTEST_PREF_FIRST_TIME, false);
	}
}
