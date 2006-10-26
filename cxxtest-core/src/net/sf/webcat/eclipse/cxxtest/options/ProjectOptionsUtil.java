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
import org.eclipse.cdt.managedbuilder.core.IOption;
import org.eclipse.cdt.managedbuilder.core.ITool;

/**
 * This class contains a set of helper methods that ease access to project
 * properties.
 * 
 * @author Tony Allowatt (Virginia Tech Computer Science)
 */
public class ProjectOptionsUtil
{
	/**
	 * Returns the union of the two arrays specified. There will be no
	 * duplicates in the resultant array.
	 * 
	 * @param array the first array.
	 * @param newEntries the second array.
	 * @return the union of the two arrays specified.
	 */
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

	/**
	 * Treats a string as an array of elements that are separated by spaces,
	 * and returns the union of these two "arrays" as a space-separated
	 * string.
	 * 
	 * @param string the String that contains the original options.
	 * @param newParts an array of new options to be added to the string.
	 * @return a String with the merged options.
	 */
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

	/**
	 * Adds an array of string options to the specified compiler option.
	 *
	 * @param tool the tool whose option will be modified
	 * @param optionId the identifier of the option to be modified
	 * @param newEntries the new options to be added to the tool's option
	 */
	public static void addToString(ITool tool,
			String optionId, String[] newEntries) throws BuildException
	{
		IOption option = tool.getOptionById(optionId);
		option = tool.getOptionToSet(option, false);
		String other = mergeStrings(option.getStringValue(), newEntries);
		option.setValue(other);
	}

	/**
	 * Adds an array of paths to an Includes-type compiler option.
	 * 
	 * @param tool the tool whose option will be modified
	 * @param optionId the identifier of the option to be modified
	 * @param newEntries the new options to be added to the tool's option
	 */
	public static void addToIncludes(ITool tool,
			String optionId, String[] newEntries) throws BuildException
	{
		IOption option = tool.getOptionById(optionId);
		option = tool.getOptionToSet(option, false);
		String[] array = mergeArrays(option.getIncludePaths(), newEntries);
		option.setValue(array);
	}

	/**
	 * Adds an array of library names to a Libraries-type compiler option.
	 * 
	 * @param tool the tool whose option will be modified
	 * @param optionId the identifier of the option to be modified
	 * @param newEntries the new options to be added to the tool's option
	 */
	public static void addToLibraries(ITool tool,
			String optionId, String[] newEntries) throws BuildException
	{
		IOption option = tool.getOptionById(optionId);
		option = tool.getOptionToSet(option, false);
		String[] array = mergeArrays(option.getLibraries(), newEntries);
		option.setValue(array);
	}

	/**
	 * Adds an array of preprocessor symbols to a DefinedSymbols-type
	 * compiler option.
	 * 
	 * @param tool the tool whose option will be modified
	 * @param optionId the identifier of the option to be modified
	 * @param newEntries the new options to be added to the tool's option
	 */
	public static void addToDefinedSymbols(ITool tool,
			String optionId, String[] newEntries) throws BuildException
	{
		IOption option = tool.getOptionById(optionId);
		option = tool.getOptionToSet(option, false);
		String[] array = mergeArrays(option.getDefinedSymbols(), newEntries);
		option.setValue(array);
	}

	/**
	 * Adds an array of strings to a StringList-type compiler option.
	 * 
	 * @param tool the tool whose option will be modified
	 * @param optionId the identifier of the option to be modified
	 * @param newEntries the new options to be added to the tool's option
	 */
	public static void addToStringList(ITool tool,
			String optionId, String[] newEntries) throws BuildException
	{
		IOption option = tool.getOptionById(optionId);
		option = tool.getOptionToSet(option, false);
		String[] array = mergeArrays(option.getStringListValue(), newEntries);
		option.setValue(array);
	}
	
	/**
	 * Sets the value of a Boolean-type compiler option.
	 * 
	 * @param tool the tool whose option will be modified
	 * @param optionId the identifier of the option to be modified
	 * @param value the new value of the option
	 */
	public static void setBoolean(ITool tool, String optionId, boolean value)
		throws BuildException
	{
		IOption option = tool.getOptionById(optionId);
		option = tool.getOptionToSet(option, false);
		option.setValue(value);
	}
}
