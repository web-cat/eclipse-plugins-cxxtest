package net.sf.webcat.eclipse.cxxtest.internal;

import net.sf.webcat.eclipse.cxxtest.CxxTestPlugin;
import net.sf.webcat.eclipse.cxxtest.IPlatformSpecificStartup;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.eclipse.ui.IStartup;

public class CxxTestEarlyStartup implements IStartup
{
	public void earlyStartup()
	{
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
}
