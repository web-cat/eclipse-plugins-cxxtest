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
package net.sf.webcat.eclipse.cxxtest.preferences;

import net.sf.webcat.eclipse.cxxtest.CxxTestPlugin;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;

/**
 * Provides default values for all the preferences used in this plug-in.
 * 
 * @author Tony Allowatt (Virginia Tech Computer Science)
 * @author Stephen Edwards (Virginia Tech Computer Science)
 */
public class CxxTestPreferenceInitializer extends AbstractPreferenceInitializer
{
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer#initializeDefaultPreferences()
	 */
	public void initializeDefaultPreferences()
	{
		IPreferenceStore store = CxxTestPlugin.getDefault()
				.getPreferenceStore();

		store.setDefault(CxxTestPlugin.CXXTEST_PREF_DRIVER_FILENAME,
				"runAllTests.cpp");

		store.setDefault(CxxTestPlugin.CXXTEST_PREF_TRAP_SIGNALS, true);
		store.setDefault(CxxTestPlugin.CXXTEST_PREF_TRACK_HEAP, true);
		store.setDefault(CxxTestPlugin.CXXTEST_PREF_TRACE_STACK, true);
	}
}
