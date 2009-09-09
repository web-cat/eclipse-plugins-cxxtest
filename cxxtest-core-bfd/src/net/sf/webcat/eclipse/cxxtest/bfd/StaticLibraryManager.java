package net.sf.webcat.eclipse.cxxtest.bfd;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import net.sf.webcat.eclipse.cxxtest.CxxTestPlugin;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.envvar.IEnvironmentVariable;
import org.eclipse.cdt.core.envvar.IEnvironmentVariableManager;
import org.eclipse.cdt.managedbuilder.gnu.cygwin.GnuCygwinConfigurationEnvironmentSupplier;
import org.eclipse.cdt.utils.spawner.ProcessFactory;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;

public class StaticLibraryManager
{
	private StaticLibraryManager()
	{
		isWindows = System.getProperty("os.name").toLowerCase().startsWith(
				"windows ");
	}


	public static StaticLibraryManager getInstance()
	{
		if (instance == null)
		{
			instance = new StaticLibraryManager();
		}
		
		return instance;
	}


	public String getMissingLibraryString()
	{
		ArrayList<String> list = new ArrayList<String>();

		if (!hasBfd)
		{
			list.add("libbfd");
		}
		
		if (!hasIntl)
		{
			list.add("libintl");
		}
		
		if (!hasIberty)
		{
			list.add("libiberty");
		}
		
		if (list.isEmpty())
		{
			return null;
		}

		StringBuffer buffer = new StringBuffer();
		buffer.append("The following linker libraries are missing: ");

		buffer.append(list.get(0));
		for (int i = 1; i < list.size(); i++)
		{
			buffer.append(", ");
			buffer.append(list.get(i));
		}
		
		return buffer.toString();
	}


	public void checkForDependencies(IProgressMonitor monitor)
	{
		monitor.beginTask("Checking for link library requirements", 5);

		// Check for libintl functions, first without explicitly linking
		// (in case they're part of glibc), and then by linking directly.

		monitor.subTask("Looking for built-in libintl functions");
		boolean hasIntlBuiltIn = tryToCompile("check-libintl.c", null);
		monitor.worked(1);

		if (!hasIntlBuiltIn)
		{
			monitor.subTask("Looking for libintl as a separate library");
			hasIntl = tryToCompile("check-libintl.c",
					new String[] { "intl" });
			needsLinkToIntl = true;
		}
		else
		{
			hasIntl = true;
			needsLinkToIntl = false;
		}
		
		monitor.worked(1);

		// Check for libiberty functions, first without explicitly linking
		// (in case they're part of glibc), and then by linking directly.

		monitor.subTask("Looking for built-in libiberty functions");
		boolean hasIbertyBuiltIn = tryToCompile("check-libiberty.c", null);
		monitor.worked(1);

		if (!hasIbertyBuiltIn)
		{
			monitor.subTask("Looking for libiberty as a separate library");
			hasIberty = tryToCompile("check-libiberty.c",
					new String[] { "iberty" });
			needsLinkToIberty = true;
		}
		else
		{
			hasIberty = true;
			needsLinkToIberty = false;
		}

		monitor.worked(1);
		
		// Now use what we know to try search for libbfd successfully. For
		// this to be successful we need to have support for several libintl
		// and libiberty functions, so use the information collected above to
		// link to them explicitly if necessary.

		monitor.subTask("Looking for libbfd");
		hasBfd = tryToCompile("check-libbfd.c", librariesNeededForBfd());
		monitor.worked(1);
		
		monitor.done();
	}


	private String[] librariesNeededForBfd()
	{
		ArrayList<String> libs = new ArrayList<String>();
		libs.add("bfd");
		
		if (shouldAddIntlToBuild())
		{
			libs.add("intl");
		}
		
		if (shouldAddIbertyToBuild())
		{
			libs.add("iberty");
		}

		return libs.toArray(new String[libs.size()]);
	}


	private String[] calculateEnvironment()
	{
		TreeMap<String, String> envMap;
		if (isWindows)
		{
			envMap = new TreeMap<String, String>(new Comparator<String>() {
				public int compare(String lhs, String rhs)
				{
					return lhs.compareToIgnoreCase(rhs);
				}
			});
		}
		else
		{
			envMap = new TreeMap<String, String>();
		}
		
		IEnvironmentVariableManager mngr = CCorePlugin.getDefault().getBuildEnvironmentManager();
		IEnvironmentVariable[] vars = mngr.getVariables(null, true);
		for(int i = 0; i < vars.length; i++)
		{
			envMap.put(vars[i].getName(), vars[i].getValue());
		}

		GnuCygwinConfigurationEnvironmentSupplier gnu =
			new GnuCygwinConfigurationEnvironmentSupplier();
		vars = gnu.getVariables(null, null);
		for(int i = 0; i < vars.length; i++)
		{
			String oldValue = envMap.get(vars[i].getName());
			
			if (oldValue != null)
			{
				oldValue = vars[i].getValue() + vars[i].getDelimiter() + oldValue;
			}
			else
			{
				oldValue = vars[i].getValue();
			}

			envMap.put(vars[i].getName(), oldValue);
		}

		List<String> strings = new ArrayList<String>(envMap.size());
		for (Map.Entry<String, String> entry : envMap.entrySet())
		{
			strings.add(entry.getKey() + "=" + entry.getValue());
		}
		
		return (String[]) strings.toArray(new String[strings.size()]);
	}


	private boolean tryToCompile(String sourceFile, String[] linkLibraries)
	{
		String[] envp = calculateEnvironment();
		sourceFile = getLibraryCheckPath(sourceFile);

		File tempOut = null;
		try
		{
			tempOut = File.createTempFile("libchkexe", null);
		}
		catch (IOException e)
		{
			e.printStackTrace();
			return false;
		}

		ArrayList<String> argList = new ArrayList<String>();
		argList.add("sh");
		argList.add("-c");

		String cmdLine = "gcc -o ";
		
		if (isWindows)
		{
			cmdLine += tempOut.getAbsolutePath().replace(File.separatorChar, '/');
			cmdLine += " ";
			cmdLine += sourceFile.replace(File.separatorChar, '/');
		}
		else
		{
			cmdLine += tempOut.getAbsolutePath();
			cmdLine += " ";
			cmdLine += sourceFile;
		}

		if (linkLibraries != null)
		{
			for (String lib : linkLibraries)
			{
				cmdLine += " -l" + lib;
			}
		}
		
		argList.add("\"" + cmdLine + "\"");

		String[] args = argList.toArray(new String[argList.size()]);

		boolean success = false;

		Process proc;
		try
		{
			proc = ProcessFactory.getFactory().exec(args, envp);
			ProcessClosure closure = new ProcessClosure(proc);
			closure.runBlocking();

			success = (proc.exitValue() == 0);
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}

		tempOut.delete();

		return success;
	}


	private String getLibraryCheckPath(String file)
	{
		String path = null;

		try
		{
			URL entry = FileLocator.find(
					CxxTestPlugin.getDefault().getBundle(),
					new Path("/library-checks/" + file), null);
			URL url = FileLocator.resolve(entry);
			path = url.getFile();

			// This special check is somewhat shady, but it looks like it's
			// the only way to handle a Windows path properly, since Eclipse
			// returns a string like "/C:/folder/...".
			if(path.charAt(2) == ':')
				path = path.substring(1);
			
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


	public boolean hasBfd()
	{
		return hasBfd;
	}


	public boolean shouldAddIntlToBuild()
	{
		return needsLinkToIntl && hasIntl;
	}


	public boolean shouldAddIbertyToBuild()
	{
		return needsLinkToIberty && hasIberty;
	}


	private boolean isWindows;

	private boolean hasBfd;
	private boolean hasIntl;
	private boolean needsLinkToIntl;
	private boolean hasIberty;
	private boolean needsLinkToIberty;

	private static StaticLibraryManager instance;
}
