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
package net.sf.webcat.eclipse.cxxtest.internal.options;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.SortedMap;
import java.util.TreeMap;

import net.sf.webcat.eclipse.cxxtest.CxxTestPlugin;
import net.sf.webcat.eclipse.cxxtest.options.IExtraOptionsEnablement;
import net.sf.webcat.eclipse.cxxtest.options.IExtraOptionsUpdater;

import org.eclipse.cdt.managedbuilder.core.BuildException;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IManagedBuildInfo;
import org.eclipse.cdt.managedbuilder.core.ITool;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.preference.IPreferenceStore;
import org.osgi.framework.Bundle;
import org.osgi.framework.Version;

/**
 * Maintains the table of extra options handlers defined by all plug-ins
 * currently loaded in Eclipse.
 * 
 * @author Tony Allevato (Virginia Tech Computer Science)
 */
public class ExtraOptionsUpdater implements IExtraOptionsUpdater
{
	// === Methods ============================================================

	// ------------------------------------------------------------------------
	/**
	 * Creates a new instance of the ExtraOptionsRegistry class.
	 */
	public ExtraOptionsUpdater()
	{
		store = CxxTestPlugin.getDefault().getPreferenceStore();

		isWindows = System.getProperty("os.name").toLowerCase().startsWith(
			"windows ");

		optionSets =
		        new HashMap<String, SortedMap<Version, IConfigurationElement>>();

		loadExtensions();
	}


	// ------------------------------------------------------------------------
	public boolean isUpdateNeeded(IProject project)
	{
		Properties versionProps = loadVersionProperties(project);

		// If there are a different number of option handlers loaded than there
		// are in the properties file for the project, then something is
		// mismatched and it needs an update.
		if(versionProps.size() != optionSets.size())
			return true;

		for(String loadedId : optionSets.keySet())
		{
			Version latestVersion = getLatestVersion(loadedId);
			Version projectVersion =
			        Version.parseVersion(versionProps.getProperty(loadedId));

			if(projectVersion.compareTo(latestVersion) < 0)
				return true;
		}

		return false;
	}


	// ------------------------------------------------------------------------
	public void updateOptions(IProject project)
	{
		Properties versionProps = loadVersionProperties(project);

		for(String loadedId : optionSets.keySet())
		{
			if(versionProps.containsKey(loadedId))
			{
				Version version =
				        Version.parseVersion(versionProps.getProperty(loadedId));
				removeOptions(project, loadedId, version);
			}

			addLatestOptions(project, loadedId);

			Version latestVersion = getLatestVersion(loadedId);
			versionProps.setProperty(loadedId, latestVersion.toString());
		}

		storeVersionProperties(project, versionProps);
	}


	public String[] getLatestCxxTestRunnerIncludes(IProject project)
	{
		ArrayList<String> includeList = new ArrayList<String>();

		for(String loadedId : optionSets.keySet())
		{
			SortedMap<Version, IConfigurationElement> optionsForId =
		        optionSets.get(loadedId);

			if(optionsForId == null)
				break;
	
			Version latestVersion = getLatestVersion(loadedId);
			if(latestVersion == null)
				break;
	
			IConfigurationElement optionSet = optionsForId.get(latestVersion);
	
			IConfigurationElement[] includeElems =
				optionSet.getChildren("runnerIncludes");

			for(IConfigurationElement includeElem : includeElems)
			{
				IConfigurationElement[] pathElems =
					includeElem.getChildren("includePath");
				
				for(IConfigurationElement pathElem : pathElems)
				{
					String path = pathElem.getAttribute("path");
					includeList.add(path);
				}
			}
		}
		
		return includeList.toArray(new String[includeList.size()]);
	}
	

	// ------------------------------------------------------------------------
	public void removeAllOptions(IProject project)
	{
		Properties versionProps = loadVersionProperties(project);

		for(String loadedId : optionSets.keySet())
		{
			if(versionProps.containsKey(loadedId))
			{
				Version version =
				        Version.parseVersion(versionProps.getProperty(loadedId));
				removeOptions(project, loadedId, version);
			}
		}

		versionProps.clear();
		storeVersionProperties(project, versionProps);
	}


	// ------------------------------------------------------------------------
	/**
	 * Gets the latest version number in use by option handlers with a
	 * particular unique identifier.
	 */
	private Version getLatestVersion(String id)
	{
		if(optionSets.containsKey(id))
			return optionSets.get(id).lastKey();
		else
			return null;
	}


	// ------------------------------------------------------------------------
	private void addLatestOptions(IProject project, String id)
	{
		SortedMap<Version, IConfigurationElement> optionsForId =
		        optionSets.get(id);
		if(optionsForId == null)
			return;

		Version latestVersion = getLatestVersion(id);
		if(latestVersion == null)
			return;

		IConfigurationElement optionSet = optionsForId.get(latestVersion);
		IConfigurationElement[] enablementElems = optionSet.getChildren("enablement");

		IManagedBuildInfo buildInfo = ManagedBuildManager.getBuildInfo(project);
		IConfiguration[] configs =
		        buildInfo.getManagedProject().getConfigurations();

		for(IConfiguration config : configs)
		{
			IConfigurationElement[] includeElems =
				optionSet.getChildren("runnerIncludes");
			
			for(IConfigurationElement includeElem : includeElems)
			{
				String pluginId = includeElem.getAttribute("pluginId");
				String path = includeElem.getAttribute("path");

				if(pluginId == null)
					pluginId = optionSet.getContributor().getName();

				String includePath =
					getBundleEntryPath(pluginId, path);

				try
				{
					addCxxTestRunnerInclude(config, includePath);
				}
				catch (BuildException e)
				{
					e.printStackTrace();
				}
			}

			for(IConfigurationElement enablementElem : enablementElems)
			{
				if(doesConfigurationMatch(project, config, enablementElem, false))
				{
					IConfigurationElement[] toolElems =
					        enablementElem.getChildren("tool");
					for(IConfigurationElement toolElem : toolElems)
					{
						try
						{
							addOptionsForTool(config, toolElem);
						}
						catch(BuildException e)
						{
							e.printStackTrace();
						}
					}
				}
			}
		}

		ManagedBuildManager.saveBuildInfo(project, true);
	}


	private void addOptionsForTool(IConfiguration config,
	        IConfigurationElement toolElem) throws BuildException
	{
		String superClassId = toolElem.getAttribute("superClassId");
		ITool[] tools = config.getToolsBySuperClassId(superClassId);
		IConfigurationElement[] optionElems = toolElem.getChildren();

		for(ITool tool : tools)
		{
			for(IConfigurationElement optionElem : optionElems)
			{
				String optionType = optionElem.getName();
				String optionId = optionElem.getAttribute("id");

				if(optionType.equals("includesOption"))
				{
					String[] newEntries = getPathsForOption(optionElem, true);
					ProjectOptionsUtil.addToIncludes(tool, optionId, newEntries);
				}
				else if(optionType.equals("librariesOption"))
				{
					String[] newEntries = getItemsForOption(optionElem);
					ProjectOptionsUtil.addToLibraries(tool, optionId,
					        newEntries);
				}
				else if(optionType.equals("definedSymbolsOption"))
				{
					String[] newEntries = getItemsForOption(optionElem);
					ProjectOptionsUtil.addToDefinedSymbols(tool, optionId,
					        newEntries);
				}
				else if(optionType.equals("stringListOption"))
				{
					String[] newEntries = getItemsForOption(optionElem);
					ProjectOptionsUtil.addToStringList(tool, optionId,
					        newEntries);
				}
				else if(optionType.equals("pathListOption"))
				{
					String[] newEntries = getPathsForOption(optionElem, true);
					ProjectOptionsUtil.addToStringList(tool, optionId,
					        newEntries);
				}
				else if(optionType.equals("splitStringOption"))
				{
					String[] newEntries =
					        getItemsAndPathsForOption(optionElem, true);
					ProjectOptionsUtil.addToString(tool, optionId, newEntries);
				}
				else if(optionType.equals("booleanOption"))
				{
					Boolean newValue =
					        Boolean.parseBoolean(optionElem.getAttribute("value"));
					ProjectOptionsUtil.setBoolean(tool, optionId, newValue);
				}
				else if(optionType.equals("stringOption"))
				{
				}
			}
		}
	}


	private void addCxxTestRunnerInclude(IConfiguration config,
			String path) throws BuildException
	{
		String superClassId = "cdt.managedbuild.tool.gnu.cpp.compiler";
		ITool[] tools = config.getToolsBySuperClassId(superClassId);

		String optionId = "gnu.cpp.compiler.option.include.paths";

		for(ITool tool : tools)
		{
			path = "\"" + path.replace('\\', '/') + "\"";

			String[] newEntries = new String[] { path };
			ProjectOptionsUtil.addToIncludes(tool, optionId, newEntries);
		}
	}


	private void removeOptionsForTool(IConfiguration config,
	        IConfigurationElement toolElem) throws BuildException
	{
		String superClassId = toolElem.getAttribute("superClassId");
		ITool[] tools = config.getToolsBySuperClassId(superClassId);
		IConfigurationElement[] optionElems = toolElem.getChildren();

		for(ITool tool : tools)
		{
			for(IConfigurationElement optionElem : optionElems)
			{
				String optionType = optionElem.getName();
				String optionId = optionElem.getAttribute("id");

				if(optionType.equals("includesOption"))
				{
					String[] newEntries = getPathPatternsForOption(optionElem, true);
					ProjectOptionsUtil.removeFromIncludesIf(tool, optionId,
					        new RegexOptionPredicate(newEntries));
				}
				else if(optionType.equals("librariesOption"))
				{
					String[] newEntries = getItemsForOption(optionElem);
					ProjectOptionsUtil.removeFromLibrariesIf(tool, optionId,
					        new ChoiceOptionPredicate(newEntries));
				}
				else if(optionType.equals("definedSymbolsOption"))
				{
					String[] newEntries = getItemsForOption(optionElem);
					ProjectOptionsUtil.removeFromDefinedSymbolsIf(tool,
					        optionId, new ChoiceOptionPredicate(newEntries));
				}
				else if(optionType.equals("stringListOption"))
				{
					String[] newEntries = getItemsForOption(optionElem);
					ProjectOptionsUtil.removeFromStringListIf(tool, optionId,
					        new ChoiceOptionPredicate(newEntries));
				}
				else if(optionType.equals("pathListOption"))
				{
					String[] newEntries = getItemsForOption(optionElem);
					ProjectOptionsUtil.removeFromStringListIf(tool, optionId,
							new ChoiceOptionPredicate(newEntries));

					newEntries = getPathPatternsForOption(optionElem, true);
					ProjectOptionsUtil.removeFromStringListIf(tool, optionId,
					        new RegexOptionPredicate(newEntries));
				}
				else if(optionType.equals("splitStringOption"))
				{
					String[] newEntries = getItemsForOption(optionElem);
					ProjectOptionsUtil.removeFromStringIf(tool, optionId,
					        new ChoiceOptionPredicate(newEntries));
				}
				else if(optionType.equals("booleanOption"))
				{
					Boolean newValue =
					        Boolean.parseBoolean(optionElem.getAttribute("value"));
					String ignore = optionElem.getAttribute("ignoreOnRemove");
					if(ignore != null && !Boolean.parseBoolean(ignore))
						ProjectOptionsUtil.setBoolean(tool, optionId, !newValue);
				}
			}
		}
	}


	private String[] getItemsForOption(IConfigurationElement optionElem)
	{
		IConfigurationElement[] itemElems = optionElem.getChildren("item");
		ArrayList<String> items = new ArrayList<String>();

		for(IConfigurationElement itemElem : itemElems)
			items.add(itemElem.getAttribute("value"));

		String[] itemArray = new String[items.size()];
		items.toArray(itemArray);
		return itemArray;
	}


	private String[] getItemsAndPathsForOption(
	        IConfigurationElement optionElem, boolean quoted)
	{
		IConfigurationElement[] itemElems = optionElem.getChildren();
		ArrayList<String> items = new ArrayList<String>();

		for(IConfigurationElement itemElem : itemElems)
		{
			if(itemElem.getName().equals("item"))
			{
				items.add(itemElem.getAttribute("value"));
			}
			else if(itemElem.getName().equals("path"))
			{
				String fullPath = getPathFromElement(itemElem, quoted);
				if(fullPath != null)
					items.add(fullPath);
			}
		}

		String[] itemArray = new String[items.size()];
		items.toArray(itemArray);
		return itemArray;
	}


	private String getBundleEntryPath(String pluginId, String relativePath)
	{
		String path = null;

		try
		{
			Bundle bundle = Platform.getBundle(pluginId);
			if(bundle == null)
				return null;

			URL entryURL =
			        FileLocator.find(bundle, new Path(relativePath), null);
			URL url = FileLocator.resolve(entryURL);
			path = url.getFile();

			// This special check is somewhat shady, but it looks like it's
			// the only way to handle a Windows path properly, since Eclipse
			// returns a string like "/C:/folder/...". The Cygwin make tools
			// don't like paths with colons in them, so we convert them to
			// the "/cygdrive/c/..." format instead.

			if(isWindows && path.charAt(2) == ':')
			{
				char letter = Character.toLowerCase(path.charAt(1));
				path = "/cygdrive/" + letter + path.substring(3);
			}

			path = new Path(path).toOSString();
			if(path.charAt(path.length() - 1) == File.separatorChar)
				path = path.substring(0, path.length() - 1);
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}

		return path;
	}


	private String getPathFromElement(IConfigurationElement pathElem,
	        boolean quoted)
	{
		String prefix = pathElem.getAttribute("prefix");
		if(prefix == null)
			prefix = "";

		String pluginId = pathElem.getAttribute("pluginId");
		String relPath = pathElem.getAttribute("relativePath");

		String fullPath;

		if(pluginId == null)
			fullPath = relPath;
		else
			fullPath = getBundleEntryPath(pluginId, relPath);

		if(fullPath != null)
		{
			fullPath = fullPath.replace('\\', '/');
			
			if(quoted)
				return (prefix + "\"" + fullPath + "\"");
			else
				return (prefix + fullPath);
		}

		return null;
	}


	private String[] getPathsForOption(IConfigurationElement optionElem,
	        boolean quoted)
	{
		IConfigurationElement[] pathElems = optionElem.getChildren("path");
		ArrayList<String> paths = new ArrayList<String>();

		for(IConfigurationElement pathElem : pathElems)
		{
			String fullPath = getPathFromElement(pathElem, quoted);
			if(fullPath != null)
				paths.add(fullPath);
		}

		String[] pathArray = new String[paths.size()];
		paths.toArray(pathArray);
		return pathArray;
	}


	private String[] getPathPatternsForOption(IConfigurationElement optionElem,
			boolean quoted)
	{
		IConfigurationElement[] pathElems = optionElem.getChildren("path");
		ArrayList<String> patterns = new ArrayList<String>();

		for(IConfigurationElement pathElem : pathElems)
		{
/*			String pluginId = pathElem.getAttribute("pluginId");
			String relPath = pathElem.getAttribute("relativePath");

			StringBuffer patternBuffer = new StringBuffer();
			patternBuffer.append(".*");

			if(pluginId != null)
			{
				for(int i = 0; i < pluginId.length(); i++)
				{
					char ch = pluginId.charAt(i);
					
					if (!Character.isLetterOrDigit(ch))
					{
						patternBuffer.append("\\");
					}

					patternBuffer.append(ch);
				}
	
				patternBuffer.append("[^\\\\]*\\\\");
			}

			for(int i = 0; i < relPath.length(); i++)
			{
				char ch = relPath.charAt(i);

				if (!Character.isLetterOrDigit(ch))
				{
					patternBuffer.append("\\");
				}

				patternBuffer.append(ch);
			}

			patternBuffer.append(".*");
			patterns.add(patternBuffer.toString());
*/
			String fullPath = getPathFromElement(pathElem, quoted);
			if(fullPath != null)
			{
				boolean isDir = new File(fullPath).isDirectory();
				String pattern =
					ShellStringUtils.patternForAnyVersionOfPluginRelativePath(
							fullPath, isDir);
				
				patterns.add(pattern);
			}
		}

		String[] patternArray = new String[patterns.size()];
		patterns.toArray(patternArray);
		return patternArray;
	}


	private boolean doesConfigurationMatch(IProject project,
	        IConfiguration config, IConfigurationElement enablementElem,
	        boolean ignoreStackTracing)
	{
		boolean satisfiesFilter, satisfiesCondition;

		String filter = enablementElem.getAttribute("configurationFilter");
		if(filter == null)
		{
			satisfiesFilter = true;
		}
		else
		{
			satisfiesFilter = false;
			String configName = config.getName();

			String[] filters = filter.split(",");
			for(String choice : filters)
			{
				if(choice.equals(configName))
				{
					satisfiesFilter = true;
					break;
				}
			}
		}

		satisfiesCondition = true;
		
		boolean isForStackTrace = Boolean.valueOf(
				enablementElem.getAttribute("isForStackTrace"));
		boolean stackTraceEnabled = store.getBoolean(
				CxxTestPlugin.CXXTEST_PREF_TRACE_STACK);

		if (!ignoreStackTracing && isForStackTrace && !stackTraceEnabled)
		{
			satisfiesCondition = false;
		}
		else
		{
			try
			{
				Object condition =
				        enablementElem.createExecutableExtension("conditionClass");
	
				if(condition instanceof IExtraOptionsEnablement)
				{
					IExtraOptionsEnablement cond =
					        (IExtraOptionsEnablement)condition;
					satisfiesCondition = cond.shouldProcessOptions(project, config);
				}
			}
			catch(CoreException e)
			{
				// Do nothing, conditionClass is an optional property.
			}
		}

		return (satisfiesFilter && satisfiesCondition);
	}


	// ------------------------------------------------------------------------
	private void removeOptions(IProject project, String id, Version version)
	{
		SortedMap<Version, IConfigurationElement> optionsForId =
		        optionSets.get(id);
		if(optionsForId == null)
			return;

		Version latestVersion = getLatestVersion(id);
		if(latestVersion == null)
			return;

		IConfigurationElement optionSet = optionsForId.get(latestVersion);
		IConfigurationElement[] enablementElems = optionSet.getChildren("enablement");

		IManagedBuildInfo buildInfo = ManagedBuildManager.getBuildInfo(project);
		IConfiguration[] configs =
		        buildInfo.getManagedProject().getConfigurations();

		for(IConfiguration config : configs)
		{
			for(IConfigurationElement enablementElem : enablementElems)
			{
				if(doesConfigurationMatch(project, config, enablementElem, true))
				{
					IConfigurationElement[] toolElems =
					        enablementElem.getChildren("tool");
					for(IConfigurationElement toolElem : toolElems)
					{
						try
						{
							removeOptionsForTool(config, toolElem);
						}
						catch(BuildException e)
						{
							e.printStackTrace();
						}
					}
				}
			}
		}

		ManagedBuildManager.saveBuildInfo(project, true);
	}


	// ------------------------------------------------------------------------
	/**
	 * Loads the extra options handlers from all currently loaded plug-ins.
	 */
	private void loadExtensions()
	{
		IExtensionRegistry registry = Platform.getExtensionRegistry();
		IExtensionPoint extensionPoint =
		        registry.getExtensionPoint(CxxTestPlugin.PLUGIN_ID
		                + ".extraProjectOptions");

		IConfigurationElement[] elements =
		        extensionPoint.getConfigurationElements();

		for(IConfigurationElement element : elements)
			loadExtraProjectOptions(element);
	}


	// ------------------------------------------------------------------------
	/**
	 * A helper function to populate the option handler table.
	 */
	private void loadExtraProjectOptions(IConfigurationElement element)
	{
		String optionsId = element.getAttribute("id");

		IConfigurationElement[] optionSetElems =
		        element.getChildren("optionSet");

		SortedMap<Version, IConfigurationElement> optionSetsForVersion =
		        new TreeMap<Version, IConfigurationElement>();

		for(IConfigurationElement optionSetElem : optionSetElems)
		{
			Version version =
			        new Version(optionSetElem.getAttribute("version"));

			optionSetsForVersion.put(version, optionSetElem);
		}

		if(optionSetsForVersion.size() != 0)
			optionSets.put(optionsId, optionSetsForVersion);
	}


	// ------------------------------------------------------------------------
	private Properties loadVersionProperties(IProject project)
	{
		Properties properties = new Properties();

		try
		{
			IFile propIFile = project.getFile(PROPERTIES_FILE);
			File propFile = propIFile.getRawLocation().toFile();

			if(propFile.exists())
				properties.load(new FileInputStream(propFile));
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}

		return properties;
	}


	// ------------------------------------------------------------------------
	private void storeVersionProperties(IProject project, Properties properties)
	{
		try
		{
			IFile propIFile = project.getFile(PROPERTIES_FILE);
			File propFile = propIFile.getRawLocation().toFile();
			properties.store(new FileOutputStream(propFile), PROPERTIES_COMMENT);
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
	}


	// === Static Variables ===================================================

	/**
	 * The name of the properties file that keeps track of the version of each
	 * option handler's settings.
	 */
	private static final String PROPERTIES_FILE =
	        ".cxxtest.versions.properties";

	/**
	 * A comment added to the top of the option version properties file.
	 */
	private static final String PROPERTIES_COMMENT =
	        "Automatically generated file -- DO NOT MODIFY";


	// === Instance Variables =================================================

	/**
	 * A table that holds all of the extra options handlers registered in loaded
	 * plug-ins, keyed by their unique identifier. For each version key, the
	 * value of that key is a sorted map that contains all of the
	 * IExtraProjectOptions instances available for that option set, keyed by
	 * their version number.
	 */
	private Map<String, SortedMap<Version, IConfigurationElement>> optionSets;
	
	private IPreferenceStore store;
	
	private boolean isWindows;
}
