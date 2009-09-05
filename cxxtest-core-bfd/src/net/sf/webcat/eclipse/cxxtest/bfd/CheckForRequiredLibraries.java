package net.sf.webcat.eclipse.cxxtest.bfd;

import java.lang.reflect.InvocationTargetException;

import net.sf.webcat.eclipse.cxxtest.CxxTestPlugin;
import net.sf.webcat.eclipse.cxxtest.IPlatformSpecificStartup;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.progress.IProgressService;

public class CheckForRequiredLibraries implements IPlatformSpecificStartup
{
	public void startup()
	{
		Display.getDefault().syncExec(new Runnable() {
			public void run()
			{
				performCheck();
			}
		});
	}


	public void performCheck()
	{
		try
		{
			IProgressService service =
				PlatformUI.getWorkbench().getProgressService();

			service.run(true, false,
					new IRunnableWithProgress() {
						public void run(IProgressMonitor monitor)
								throws InvocationTargetException,
								InterruptedException
						{
							performCheck(monitor);
						}
			});
		}
		catch (InvocationTargetException e)
		{
			e.printStackTrace();
		}
		catch (InterruptedException e)
		{
			e.printStackTrace();
		}
	}
	
	
	private void performCheck(IProgressMonitor monitor)
	{
		StaticLibraryManager manager = StaticLibraryManager.getInstance();
		
		manager.checkForDependencies(monitor);
		String missingLibs = manager.getMissingLibraryString();
		
		if (missingLibs != null)
		{
			Shell shell = PlatformUI.getWorkbench().
				getActiveWorkbenchWindow().getShell();

			String message =
				"In order to support retrieving stack traces when failures " +
				"occur in CxxTest unit tests, you need to have the following " +
				"libraries installed on your system:\n\n      " +
				missingLibs + "\n\n" +
				"Stack traces will be disabled in any projects that you " +
				"create. Once you have installed these libraries, you can " +
				"re-enable this feature in your Eclipse preferences.";

			MessageDialog.openInformation(shell, "CxxTest", message);
			
			// Disable stack traces in preferences.
			IPreferenceStore store =
		        CxxTestPlugin.getDefault().getPreferenceStore();
			store.setValue(CxxTestPlugin.CXXTEST_PREF_TRACE_STACK, false);
		}		
	}
}
