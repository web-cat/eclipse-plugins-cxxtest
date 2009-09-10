package net.sf.webcat.eclipse.cxxtest.internal;

import net.sf.webcat.eclipse.cxxtest.CxxTestPlugin;
import net.sf.webcat.eclipse.cxxtest.options.IExtraOptionsUpdater;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;

public class CxxTestPreferencesChangeListener implements
		IPropertyChangeListener
{
	public void propertyChange(PropertyChangeEvent event)
	{
		if (CxxTestPlugin.CXXTEST_PREF_TRACE_STACK.equals(
				event.getProperty()))
		{
			boolean oldValue = (Boolean) event.getOldValue();
			boolean newValue = (Boolean) event.getNewValue();
			
			if (oldValue != newValue)
			{
				stackTracingWasChanged(newValue);
			}
		}
	}
	
	
	private void stackTracingWasChanged(boolean enabled)
	{
		IExtraOptionsUpdater updater =
			CxxTestPlugin.getDefault().getExtraOptionsUpdater();

		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IProject[] projects = workspace.getRoot().getProjects();
		
		for (IProject project : projects)
		{
			if (!project.isOpen())
			{
				continue;
			}
			
			try
			{
				if (project.hasNature(CxxTestPlugin.CXXTEST_NATURE))
				{
					updater.updateOptions(project);
				}
			}
			catch (CoreException e)
			{
				e.printStackTrace();
			}
		}
	}
}
