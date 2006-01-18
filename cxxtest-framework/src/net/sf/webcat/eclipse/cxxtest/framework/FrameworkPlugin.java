package net.sf.webcat.eclipse.cxxtest.framework;

import java.io.IOException;
import java.net.URL;

import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Plugin;
import org.osgi.framework.BundleContext;

/**
 * The main plugin class to be used in the desktop.
 */
public class FrameworkPlugin extends Plugin {

	//The shared instance.
	private static FrameworkPlugin plugin;
	
	/**
	 * The constructor.
	 */
	public FrameworkPlugin() {
		plugin = this;
	}

	/**
	 * This method is called upon plug-in activation
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
	}

	/**
	 * This method is called when the plug-in is stopped
	 */
	public void stop(BundleContext context) throws Exception {
		super.stop(context);
		plugin = null;
	}

	/**
	 * Returns the shared instance.
	 */
	public static FrameworkPlugin getDefault() {
		return plugin;
	}

	public String getFrameworkPath()
	{
		String path = null;

		try
		{
			URL entry = Platform.find(getBundle(), new Path("/cxxtest/"));
			URL url = Platform.resolve(entry);
			path = url.getFile();

			// This special check is somewhat shady, but it looks like it's
			// the only way to handle a Windows path properly, since Eclipse
			// returns a string like "/C:/folder/...".
			if(path.charAt(2) == ':')
				path = path.substring(1);
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}

		return path;
	}
}
