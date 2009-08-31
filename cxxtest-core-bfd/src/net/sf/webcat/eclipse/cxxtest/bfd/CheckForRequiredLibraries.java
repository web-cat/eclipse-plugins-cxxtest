package net.sf.webcat.eclipse.cxxtest.bfd;

import net.sf.webcat.eclipse.cxxtest.IPlatformSpecificStartup;

public class CheckForRequiredLibraries implements IPlatformSpecificStartup
{
	public void startup()
	{
	}
	
	
	public static boolean hasRequiredLibraries()
	{
		if (hasRequiredLibraries != null)
		{
			return hasRequiredLibraries;
		}
		else
		{
			return false;
		}
	}


	private static Boolean hasRequiredLibraries = null;
}
