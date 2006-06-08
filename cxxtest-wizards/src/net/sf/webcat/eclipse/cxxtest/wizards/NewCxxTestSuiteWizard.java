package net.sf.webcat.eclipse.cxxtest.wizards;

import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.cdt.core.model.IMethodDeclaration;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.wizards.newresource.BasicNewResourceWizard;

public class NewCxxTestSuiteWizard extends Wizard implements INewWizard
{
	private IWorkbench workbench;
	private IStructuredSelection selection;

	private NewCxxTestSuiteWizardPageOne pageOne;
	private NewCxxTestSuiteWizardPageTwo pageTwo;

	public NewCxxTestSuiteWizard()
	{
		setNeedsProgressMonitor(true);
		setWindowTitle("New CxxTest Suite");
		initializeDefaultPageImageDescriptor();
	}

	public void init(IWorkbench workbench, IStructuredSelection selection)
	{
		this.workbench = workbench;
		this.selection = selection;
	}

	private void initializeDefaultPageImageDescriptor()
	{
		try
		{
			ImageDescriptor id = ImageDescriptor.createFromURL(
					new URL(Platform.getBundle(CxxTestWizardsPlugin.PLUGIN_ID).
							getEntry("/icons/"), "newtest_wiz.gif"));
			setDefaultPageImageDescriptor(id);
		}
		catch (MalformedURLException e)
		{
		}
	}

	public void addPages()
	{
		pageTwo = new NewCxxTestSuiteWizardPageTwo();
		pageOne = new NewCxxTestSuiteWizardPageOne(pageTwo);

		addPage(pageOne);
		pageOne.init(selection);
		addPage(pageTwo);
	}

	public boolean performFinish()
	{
		pageOne.collectFields();

		IPath suitePath = Path.fromPortableString(
				pageOne.getSourceFolder().toPortableString());
		suitePath = suitePath.append(pageOne.getSuiteName() + ".h");
		IMethodDeclaration[] methodStubs = pageTwo.getCheckedMethods();

		final CxxTestSuiteGenerator generator = new CxxTestSuiteGenerator(
				pageOne.getSuiteName(), suitePath,
				pageOne.getHeaderUnderTest(), pageOne.getSuperClass(),
				pageOne.getCreateSetUp(), pageOne.getCreateTearDown(),
				methodStubs);

		try {
			getContainer().run(false, true, new IRunnableWithProgress() {
				public void run(IProgressMonitor monitor)
					throws InvocationTargetException, InterruptedException
				{
					try {
						generator.generate(monitor);
					} catch (CoreException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			});
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		IFile source = generator.getCreatedSuiteFile();
    	if (source != null)
    	{
    		selectAndReveal(source);
    		openResource(source);
    	}

    	return true;
	}

	private void selectAndReveal(IResource newResource)
	{
		BasicNewResourceWizard.selectAndReveal(newResource,
				workbench.getActiveWorkbenchWindow());
	}
	
	private void openResource(final IFile resource)
	{
		final IWorkbenchPage activePage =
			workbench.getActiveWorkbenchWindow().getActivePage();
		
		if (activePage != null)
		{
			final Display display= getShell().getDisplay();

			if(display != null)
			{
				display.asyncExec(new Runnable() {
					public void run() {
						try
						{
							IDE.openEditor(activePage, resource, true);
						}
						catch (PartInitException e)
						{
						}
					}
				});
			}
		}
	}
}
