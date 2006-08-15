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

import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.core.resources.IProject;

/**
 * Clients should implement this interface in order to add custom options
 * to a CxxTest project once the project has been created. The most likely
 * use of this interface is for platform-specific plugins that need to add
 * certain options for the compilation process to succeed (for example,
 * the symbol table functionality on Mac OS X requires different libraries
 * than it does on Windows or Linux).
 * 
 * @author Tony Allowatt
 */
public interface IExtraProjectOptions
{
	/**
	 * Adds extra options to the specified project.
	 * 
	 * @param project the IProject to which the options will be added
	 */
	void addOptions(IProject project, IConfiguration configuration);
	
	/**
	 * Remove the extra options from the specified project.
	 * 
	 * @param project the IProject from which the options will be removed
	 */
	void removeOptions(IProject project, IConfiguration configuration);
}
