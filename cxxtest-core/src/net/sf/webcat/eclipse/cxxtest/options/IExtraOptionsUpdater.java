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
package net.sf.webcat.eclipse.cxxtest.options;

import org.eclipse.core.resources.IProject;

/**
 * Maintains the table of extra options handlers defined by all plug-ins
 * currently loaded in Eclipse and provides operations that add and remove
 * options from a project based on version number.
 * 
 * @author Tony Allevato (Virginia Tech Computer Science)
 */
public interface IExtraOptionsUpdater
{
	// ------------------------------------------------------------------------
	/**
	 * Updates the compiler options in a project by invoking the removeOptions
	 * method on old versions of the option handlers, if necessary, then calling
	 * addOptions to set the latest versions of the options in the project.
	 * 
	 * @param project
	 *            the IProject resource that represents the managed C++ project
	 *            to which the options should be added
	 */
	void updateOptions(IProject project);


	// ------------------------------------------------------------------------
	/**
	 * Determines whether a project's compiler options need to be updated. A
	 * project is determined to need an update if:
	 * 
	 * 1) the version of an option set currently used by the project is less
	 * than the highest version of that set loaded by a plug-in 2) an option set
	 * is loaded by a plug-in that does not exist in the project
	 * 
	 * @param project
	 *            the IProject resource whose settings should be checked
	 * 
	 * @return true if the settings need to be updated; otherwise, false.
	 */
	boolean isUpdateNeeded(IProject project);


	// ------------------------------------------------------------------------
	/**
	 * Removes all the extra compiler options that have been injected into a
	 * project through the loaded extension points.
	 * 
	 * @param project
	 *            the IProject resource whose settings should be removed
	 */
	void removeAllOptions(IProject project);
}
