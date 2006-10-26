package net.sf.webcat.eclipse.cxxtest.macosx;

import java.io.IOException;
import java.net.URL;

import net.sf.webcat.eclipse.cxxtest.CxxTestPlugin;
import net.sf.webcat.eclipse.cxxtest.options.IExtraProjectOptions;
import net.sf.webcat.eclipse.cxxtest.options.ProjectOptionsUtil;

import org.eclipse.cdt.managedbuilder.core.BuildException;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.ITool;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.preference.IPreferenceStore;

public class ExtraProjectOptions implements IExtraProjectOptions
{
	public String getEntryPath(String entry)
	{
		String path = null;

		try
		{
			URL entryURL = FileLocator.find(
					CxxTestPlugin.getDefault().getBundle(), new Path(entry), null);
			URL url = FileLocator.resolve(entryURL);
			path = url.getFile();
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}

		return path;
	}

	public void addOptions(IProject project, IConfiguration configuration)
	{
		if(!"Debug".equals(configuration.getName()))
			return;
		
		IPreferenceStore store = CxxTestPlugin.getDefault().getPreferenceStore();
		boolean stackTrace = store.getBoolean(CxxTestPlugin.CXXTEST_PREF_TRACE_STACK);
		
		if(stackTrace)
		{
			try
			{
				addStackTraceOptions(project, configuration);
			}
			catch(BuildException e)
			{
				e.printStackTrace();
			}
		}
	}
	
	public void removeOptions(IProject project, IConfiguration configuration)
	{
	}

	private void addStackTraceOptions(IProject project, IConfiguration config)
		throws BuildException
	{
		String frameworkPath = "\"/System/Library/PrivateFrameworks\"";
		String libsPath = "\"" + getEntryPath("/lib") + "\"";

		ITool[] tools = config.getToolsBySuperClassId("cdt.managedbuild.tool.gnu.cpp.compiler");
		for(int i = 0; i < tools.length; i++)
		{
			ProjectOptionsUtil.addToString(tools[i],
					"gnu.cpp.compiler.option.other.other",
					new String[] { "-finstrument-functions" });
		}
		
		tools = config.getToolsBySuperClassId("cdt.managedbuild.tool.macosx.cpp.linker");
		for(int i = 0; i < tools.length; i++)
		{
			ProjectOptionsUtil.addToStringList(tools[i],
					"macosx.cpp.link.option.paths",
					new String[] { libsPath });

			ProjectOptionsUtil.addToLibraries(tools[i],
					"macosx.cpp.link.option.libs",
					new String[] { "objc", "symreader" });

			ProjectOptionsUtil.addToString(tools[i],
					"macosx.cpp.link.option.flags",
					new String[] { "-F" + frameworkPath, "-framework vmutils" });
		}
	}
}
