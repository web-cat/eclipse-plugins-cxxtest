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
package net.sf.webcat.eclipse.cxxtest;

import java.io.BufferedReader;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import net.sf.webcat.eclipse.cxxtest.internal.options.ExtraOptionsUpdater;
import net.sf.webcat.eclipse.cxxtest.options.IExtraOptionsUpdater;
import net.sf.webcat.eclipse.cxxtest.ui.TestRunnerViewPart;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.ConfigurationScope;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleManager;
import org.eclipse.ui.console.MessageConsole;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.eclipse.ui.preferences.ScopedPreferenceStore;
import org.osgi.framework.BundleContext;

/**
 * The main plugin class to be used in the desktop.
 * 
 * @author Tony Allevato (Virginia Tech Computer Science)
 */
public class CxxTestPlugin extends AbstractUIPlugin
{
	// The shared instance.
	private static CxxTestPlugin plugin;

	// Resource bundle.
	private ResourceBundle resourceBundle;

	private MessageConsole builderConsole;
	
	private ExtraOptionsUpdater extraOptionsUpdater;
	
	private boolean systemHasLibIntl;

	public static final String PLUGIN_ID = "net.sf.webcat.eclipse.cxxtest";

	public static final String CXXTEST_NATURE = PLUGIN_ID + ".cxxtestNature";

	public static final String CXXTEST_BUILDER = PLUGIN_ID + ".cxxtestbuilder";

	public static final String CXXTEST_ENABLED = PLUGIN_ID + ".enabled";

	public static final String CXXTEST_RUNNER = PLUGIN_ID + ".cxxtestrunner";

	public static final String CXXTEST_PREF_DRIVER_FILENAME = PLUGIN_ID
	        + ".preferences.driver";

	public static final String CXXTEST_PREF_TRACK_HEAP = PLUGIN_ID
	        + ".preferences.trackHeap";

	public static final String CXXTEST_PREF_TRAP_SIGNALS = PLUGIN_ID
	        + ".preferences.trapSignals";

	public static final String CXXTEST_PREF_TRACE_STACK = PLUGIN_ID
	        + ".preferences.traceStack";

	public static final String CXXTEST_CPREF_LOG_EXECUTION = "BINARY_EXECUTION_LOG";


	/**
	 * The constructor.
	 */
	public CxxTestPlugin()
	{
		super();
		plugin = this;
		try
		{
			resourceBundle = ResourceBundle
			        .getBundle("net.sf.webcat.eclipse.cxxtest.BuilderPluginResources");
		}
		catch(MissingResourceException x)
		{
			resourceBundle = null;
		}
	}


	/**
	 * This method is called upon plug-in activation
	 */
	public void start(BundleContext context) throws Exception
	{
		super.start(context);
		
		runPlatformSpecificStartups();
	}


	private void runPlatformSpecificStartups()
	{
		IExtensionRegistry registry = Platform.getExtensionRegistry();
		IExtensionPoint extensionPoint =
		        registry.getExtensionPoint(CxxTestPlugin.PLUGIN_ID
		                + ".platformSpecificStartup");

		IConfigurationElement[] elements =
		        extensionPoint.getConfigurationElements();

		for(IConfigurationElement element : elements)
		{
			if ("startup".equals(element.getName()))
			{
				try
				{
					Object obj = element.createExecutableExtension("class");
					if (obj instanceof IPlatformSpecificStartup)
					{
						IPlatformSpecificStartup startup =
							(IPlatformSpecificStartup) obj;
						
						startup.startup();
					}
				}
				catch (CoreException e)
				{
					// Do nothing.
				}
			}
		}
	}
	

	/**
	 * This method is called when the plug-in is stopped
	 */
	public void stop(BundleContext context) throws Exception
	{
		super.stop(context);
	}


	private void checkSystemForLibIntl()
	{
		systemHasLibIntl = false;

		try
		{
			String cmd = "gcc -print-search-dirs";
			Process process = Runtime.getRuntime().exec(cmd);
			process.waitFor();

			InputStream stdout = process.getInputStream();
			InputStreamReader isReader = new InputStreamReader(stdout);
			BufferedReader reader = new BufferedReader(isReader);

			String line;
			String libPathString = "";
			while ((line = reader.readLine()) != null)
			{
				if (line.startsWith("libraries:"))
				{
					line = line.substring("libraries:".length()).trim();
					if (line.startsWith("="))
					{
						line = line.substring(1);
					}

					libPathString = line;
					break;
				}
			}		
			
			String[] libPaths = libPathString.split(":");
			for (String libPath : libPaths)
			{
				File dir = new File(libPath);
				String[] candidates = dir.list(new FilenameFilter() {
					public boolean accept(File dir, String name)
					{
						return name.startsWith("libintl");
					}
				});
				
				if (candidates != null && candidates.length > 0)
				{
					systemHasLibIntl = true;
					break;
				}
			}
		}
		catch (IOException e)
		{
			// Do nothing.
		}
		catch (InterruptedException e)
		{
			// Do nothing.
		}
	}

	
	public boolean systemHasLibIntl()
	{
		return systemHasLibIntl;
	}


	/**
	 * Returns the shared instance.
	 */
	public static CxxTestPlugin getDefault()
	{
		return plugin;
	}


	/**
	 * Returns the string from the plugin's resource bundle, or 'key' if not
	 * found.
	 */
	public static String getResourceString(String key)
	{
		ResourceBundle bundle = CxxTestPlugin.getDefault().getResourceBundle();
		try
		{
			return (bundle != null) ? bundle.getString(key) : key;
		}
		catch(MissingResourceException e)
		{
			return key;
		}
	}


	/**
	 * Returns the plugin's resource bundle,
	 */
	public ResourceBundle getResourceBundle()
	{
		return resourceBundle;
	}


	public String getString(String id)
	{
		IPreferenceStore store = getPreferenceStore();
		return store.getString(id);
	}


	public boolean getBoolean(String id)
	{
		IPreferenceStore store = getPreferenceStore();
		return store.getBoolean(id);
	}


	public boolean getConfigurationBoolean(String id)
	{
		IPreferenceStore store = new ScopedPreferenceStore(
		        new ConfigurationScope(), PLUGIN_ID);
		return store.getBoolean(id);
	}


	public MessageConsole getBuilderConsole()
	{
		if(builderConsole == null)
		{
			builderConsole = new MessageConsole("CxxTest Driver Generator",
			        null);

			IConsoleManager manager = ConsolePlugin.getDefault()
			        .getConsoleManager();
			manager.addConsoles(new IConsole[] { builderConsole });
		}

		return builderConsole;
	}


	public static IWorkbenchWindow getActiveWorkbenchWindow()
	{
		if(plugin == null)
			return null;

		IWorkbench workBench = plugin.getWorkbench();

		if(workBench == null)
			return null;

		return workBench.getActiveWorkbenchWindow();
	}


	public static IWorkbenchPage getActivePage()
	{
		IWorkbenchWindow activeWorkbenchWindow = getActiveWorkbenchWindow();

		if(activeWorkbenchWindow == null)
			return null;

		return activeWorkbenchWindow.getActivePage();
	}


	public TestRunnerViewPart getTestRunnerView()
	{
		IWorkbenchPage page = getActivePage();

		if(page == null)
			return null;

		TestRunnerViewPart view = (TestRunnerViewPart)page
		        .findView(TestRunnerViewPart.ID);

		if(view == null)
		{
			try
			{
				view = (TestRunnerViewPart)page.showView(TestRunnerViewPart.ID);
			}
			catch(PartInitException e)
			{
			}
		}

		return view;
	}


	public IExtraOptionsUpdater getExtraOptionsUpdater()
	{
		if(extraOptionsUpdater == null)
			extraOptionsUpdater = new ExtraOptionsUpdater();
		
		return extraOptionsUpdater;
	}


	public static ImageDescriptor getImageDescriptor(String relativePath)
	{
		try
		{
			return ImageDescriptor.createFromURL(new URL(Platform.getBundle(
			        PLUGIN_ID).getEntry("/icons/full/"), relativePath));
		}
		catch(MalformedURLException e)
		{
			// should not happen
			return ImageDescriptor.getMissingImageDescriptor();
		}
	}
}
