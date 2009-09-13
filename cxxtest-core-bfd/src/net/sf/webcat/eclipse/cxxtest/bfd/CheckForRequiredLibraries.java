/*==========================================================================*\
 |  $Id$
 |*-------------------------------------------------------------------------*|
 |  Copyright (C) 2006-2009 Virginia Tech 
 |
 |	This file is part of Web-CAT Eclipse Plugins.
 |
 |	Web-CAT is free software; you can redistribute it and/or modify
 |	it under the terms of the GNU General Public License as published by
 |	the Free Software Foundation; either version 2 of the License, or
 |	(at your option) any later version.
 |
 |	Web-CAT is distributed in the hope that it will be useful,
 |	but WITHOUT ANY WARRANTY; without even the implied warranty of
 |	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 |	GNU General Public License for more details.
 |
 |	You should have received a copy of the GNU General Public License
 |	along with Web-CAT; if not, see <http://www.gnu.org/licenses/>.
\*==========================================================================*/

package net.sf.webcat.eclipse.cxxtest.bfd;

import net.sf.webcat.eclipse.cxxtest.IStackTraceDependencyCheck;

import org.eclipse.core.runtime.IProgressMonitor;

//------------------------------------------------------------------------
/**
 * TODO: real description
 *  
 * @author  Tony Allevato (Virginia Tech Computer Science)
 * @author  latest changes by: $Author$
 * @version $Revision$ $Date$
 */
public class CheckForRequiredLibraries implements IStackTraceDependencyCheck
{
	public boolean checkForDependencies(IProgressMonitor monitor)
	{
		StaticLibraryManager manager = StaticLibraryManager.getInstance();
		
		manager.checkForDependencies(monitor);
		missingDependencies = manager.getMissingLibraryString();

		return (missingDependencies == null);
	}


	public String missingDependencies()
	{
		return missingDependencies;
	}


	private String missingDependencies = null;
}
