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

import java.util.ArrayList;

import org.eclipse.cdt.managedbuilder.core.BuildException;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IOption;
import org.eclipse.cdt.managedbuilder.core.ITool;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;

public class ProjectOptionsUtil
{
	public static String[] mergeArrays(String[] array, String[] newEntries)
	{
		ArrayList list = new ArrayList();
		for(int i = 0; i < array.length; i++)
			list.add(array[i]);
		
		for(int i = 0; i < newEntries.length; i++)
			if(!list.contains(newEntries[i]))
				list.add(newEntries[i]);
		
		String[] newArray = new String[list.size()];
		return (String[])list.toArray(newArray);
	}

	public static String mergeStrings(String string, String[] newParts)
	{
		for(int i = 0; i < newParts.length; i++)
		{
			if(!string.contains(newParts[i]))
				string += (string.length() == 0 ? "" : " ") + newParts[i];
		}
		
		return string;
	}
	
/*	public static String[] removeFromArray(String[] array, String[] remEntries)
	{
		ArrayList list = new ArrayList();
		for(int i = 0; i < array.length; i++)
			list.add(array[i]);

		for(int i = 0; i < remEntries.length; i++)
		{
			boolean removed = false;
			do { removed = list.remove(remEntries[i]); } while(removed);
		}
		
		String[] newArray = new String[list.size()];
		list.toArray(newArray);
		return newArray;
	}*/

	public static void addToString(IConfiguration configuration, ITool tool,
			String optionId, String[] newEntries) throws BuildException
	{
		IOption option = tool.getOptionById(optionId);
		String other = mergeStrings(option.getStringValue(), newEntries);
		ManagedBuildManager.setOption(configuration, tool, option, other);
	}

	public static void addToIncludes(IConfiguration configuration, ITool tool,
			String optionId, String[] newEntries) throws BuildException
	{
		IOption option = tool.getOptionById(optionId);
		String[] array = mergeArrays(option.getIncludePaths(), newEntries);
		ManagedBuildManager.setOption(configuration, tool, option, array);
	}

	public static void addToLibraries(IConfiguration configuration, ITool tool,
			String optionId, String[] newEntries) throws BuildException
	{
		IOption option = tool.getOptionById(optionId);
		String[] array = mergeArrays(option.getLibraries(), newEntries);
		ManagedBuildManager.setOption(configuration, tool, option, array);
	}

	public static void addToStringList(IConfiguration configuration, ITool tool,
			String optionId, String[] newEntries) throws BuildException
	{
		IOption option = tool.getOptionById(optionId);
		String[] array = mergeArrays(option.getStringListValue(), newEntries);
		ManagedBuildManager.setOption(configuration, tool, option, array);
	}
}
