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
package net.sf.webcat.eclipse.cxxtest.ui;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;
import java.util.Vector;

import net.sf.webcat.eclipse.cxxtest.CxxTestPlugin;
import net.sf.webcat.eclipse.cxxtest.model.ICxxTestAssertion;
import net.sf.webcat.eclipse.cxxtest.model.ICxxTestBase;
import net.sf.webcat.eclipse.cxxtest.model.ICxxTestMethod;
import net.sf.webcat.eclipse.cxxtest.model.ICxxTestStackFrame;
import net.sf.webcat.eclipse.cxxtest.model.ICxxTestSuite;
import net.sf.webcat.eclipse.cxxtest.model.ICxxTestSuiteChild;
import net.sf.webcat.eclipse.cxxtest.model.ICxxTestSuiteError;
import net.sf.webcat.eclipse.cxxtest.model.IMemWatchInfo;
import net.sf.webcat.eclipse.cxxtest.model.IMemWatchLeak;

import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.debug.core.ICDTLaunchConfigurationConstants;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.ViewForm;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.part.ViewPart;

/**
 * The view part that represents the CxxTest view in the Eclipse IDE. This
 * view displays the results of the CxxTest execution and flags any tests
 * that failed.
 * 
 * Influenced greatly by the same JUnit class.
 * 
 * @author Tony Allowatt (Virginia Tech Computer Science)
 */
public class TestRunnerViewPart extends ViewPart
{
	private class DetailsContentProvider implements ITreeContentProvider
	{
		public Object[] getElements(Object inputElement)
		{
			if(inputElement == null)
				return new Object[0];
			else if(inputElement instanceof IMemWatchInfo)
			{
				IMemWatchInfo info = (IMemWatchInfo)inputElement;
				String[] array = new String[2];
				array[0] = "Total bytes allocated: " + info.getTotalBytesAllocated();
				array[1] = "Maximum bytes in use: " + info.getMaxBytesInUse();
				return array;
			}
			else
				return (Object[])inputElement;
		}

		public void dispose() { }

		public void inputChanged(
				Viewer viewer, Object oldInput, Object newInput) { }

		public Object[] getChildren(Object parentElement)
		{
			if(parentElement instanceof ICxxTestAssertion)
			{
				ICxxTestAssertion cta = (ICxxTestAssertion)parentElement;
				return cta.getStackTrace();
			}
			else if(parentElement instanceof ICxxTestSuiteError)
			{
				ICxxTestSuiteError cta = (ICxxTestSuiteError)parentElement;
				return cta.getStackTrace();
			}
			else if(parentElement instanceof IMemWatchLeak)
			{
				IMemWatchLeak mwl = (IMemWatchLeak)parentElement;
				return mwl.getStackTrace();
			}
			else
				return null;
		}

		public Object getParent(Object element)
		{
			return null;
		}

		public boolean hasChildren(Object element)
		{
			if(element instanceof ICxxTestAssertion)
			{
				ICxxTestAssertion cta = (ICxxTestAssertion)element;
				return cta.getStackTrace() != null;
			}
			else if(element instanceof ICxxTestSuiteError)
			{
				ICxxTestSuiteError cta = (ICxxTestSuiteError)element;
				return cta.getStackTrace() != null;
			}
			else
				return false;
		}
	}

	private class DetailsLabelProvider extends LabelProvider
	{
		public String getText(Object element)
		{
			if(element instanceof ICxxTestAssertion)
			{
				ICxxTestAssertion assertion = (ICxxTestAssertion)element;
				return assertion.getMessage(true);
			}
			else if(element instanceof ICxxTestSuiteError)
			{
				ICxxTestSuiteError assertion = (ICxxTestSuiteError)element;
				return assertion.getMessage();
			}
			else if(element instanceof IMemWatchLeak)
			{
				IMemWatchLeak leak = (IMemWatchLeak)element;
				if(leak.getStackTrace() != null && leak.getStackTrace().length > 0)
					return leak.toString() + ", allocated using:";
				else
					return leak.toString();
			}
			else if(element instanceof ICxxTestStackFrame)
			{
				ICxxTestStackFrame ste = (ICxxTestStackFrame)element;
				return ste.toString();
			}
			else
				return element.toString();
		}
		
		public Image getImage(Object element)
		{
			if(element instanceof ICxxTestAssertion ||
				element instanceof ICxxTestSuiteError)
			{
				ICxxTestBase test = (ICxxTestBase)element;
	
				switch(test.getStatus())
				{
					case ICxxTestBase.STATUS_OK:
						return assertTraceIcon;
						
					case ICxxTestBase.STATUS_WARNING:
						return assertWarnIcon;
	
					case ICxxTestBase.STATUS_FAILED:
						return assertFailureIcon;
						
					case ICxxTestBase.STATUS_ERROR:
						return assertErrorIcon;
						
					default:
						return null;
				}
			}
			else if(element instanceof ICxxTestStackFrame ||
					element instanceof IMemWatchLeak)	
			{
				return stackFrameIcon;
			}
			else
				return null;
		}
	}

	private final Image testOkIcon= TestRunnerViewPart.createImage("obj16/testok.gif");
	private final Image testWarnIcon= TestRunnerViewPart.createImage("obj16/testwarn.gif");
	private final Image testFailureIcon= TestRunnerViewPart.createImage("obj16/testfail.gif");
	private final Image testErrorIcon= TestRunnerViewPart.createImage("obj16/testerr.gif");
	private final Image assertTraceIcon= TestRunnerViewPart.createImage("obj16/asserttrace.gif");
	private final Image assertWarnIcon= TestRunnerViewPart.createImage("obj16/assertwarn.gif");
	private final Image assertFailureIcon= TestRunnerViewPart.createImage("obj16/assertfail.gif");
	private final Image assertErrorIcon= TestRunnerViewPart.createImage("obj16/asserterror.gif");
	private final Image stackFrameIcon= TestRunnerViewPart.createImage("obj16/stkfrm_obj.gif");

	public static final String ID = CxxTestPlugin.PLUGIN_ID + ".TestRunnerView";

	private Composite parent;

	private Composite counterComposite;

	private CounterPanel counterPanel;

	private CxxTestProgressBar progressBar;

	private CTabFolder tabFolder;

	private SashForm sashForm;

	private TreeViewer detailViewer;

	private int orientation = VIEW_ORIENTATION_AUTOMATIC;

	private int currentOrientation;

	private ILaunch currentLaunch;

	private ICProject launchedProject;

	protected Vector testRunTabs = new Vector();

	private TestRunTab activeRunTab;
	
	private Clipboard clipboard;

	private StopAction stopAction;
	private ToggleOrientationAction[] toggleOrientationActions;

	private static final int VIEW_ORIENTATION_VERTICAL= 0;
	private static final int VIEW_ORIENTATION_HORIZONTAL= 1;
	private static final int VIEW_ORIENTATION_AUTOMATIC= 2;

	private class StopAction extends Action
	{
		public StopAction()
		{
			setText("Terminate");
			setToolTipText("Terminate Test Runner");
			
			setDisabledImageDescriptor(CxxTestPlugin.getImageDescriptor("dlcl16/stop.gif"));
			setHoverImageDescriptor(CxxTestPlugin.getImageDescriptor("elcl16/stop.gif"));
			setImageDescriptor(CxxTestPlugin.getImageDescriptor("elcl16/stop.gif"));
		}

		public void run()
		{
			stopTest();
		}
	}

	private class ToggleOrientationAction extends Action
	{
		private final int actionOrientation;
		
		public ToggleOrientationAction(int orientation)
		{
			super("", AS_RADIO_BUTTON);
			
			if(orientation == TestRunnerViewPart.VIEW_ORIENTATION_HORIZONTAL)
			{
				setText("&Horizontal View Orientation"); 
				setImageDescriptor(CxxTestPlugin.getImageDescriptor("elcl16/th_horizontal.gif"));				
			}
			else if(orientation == TestRunnerViewPart.VIEW_ORIENTATION_VERTICAL)
			{
				setText("&Vertical View Orientation"); 
				setImageDescriptor(CxxTestPlugin.getImageDescriptor("elcl16/th_vertical.gif"));				
			}
			else if(orientation == TestRunnerViewPart.VIEW_ORIENTATION_AUTOMATIC)
			{
				setText("&Automatic View Orientation");  
				setImageDescriptor(CxxTestPlugin.getImageDescriptor("elcl16/th_automatic.gif"));				
			}

			actionOrientation = orientation;
		}
		
		public int getOrientation()
		{
			return actionOrientation;
		}
		
		public void run()
		{
			if(isChecked())
			{
				orientation = actionOrientation;
				computeOrientation();
			}
		}		
	}

	public void createPartControl(Composite parent)
	{
		this.parent = parent;
		parent.addControlListener(new ControlAdapter()
		{
			public void controlResized(ControlEvent e)
			{
				computeOrientation();
			}
		});

		setContentDescription("");

		clipboard = new Clipboard(Display.getCurrent());

		GridLayout layout = new GridLayout();
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		parent.setLayout(layout);

		configureToolBar();

		counterComposite = createProgressCountPanel(parent);
		counterComposite.setLayoutData(new GridData(
				GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_FILL));
		
		SashForm sashForm = createSashForm(parent);
		sashForm.setLayoutData(new GridData(GridData.FILL_BOTH));
	}

	private SashForm createSashForm(Composite parent)
	{
		sashForm = new SashForm(parent, SWT.HORIZONTAL);
		ViewForm top = new ViewForm(sashForm, SWT.NONE);
		tabFolder = createTestRunTabs(top);
		top.setContent(tabFolder);
		
		ViewForm bottom = new ViewForm(sashForm, SWT.NONE);
		CLabel label = new CLabel(bottom, SWT.NONE);
		label.setText("Details"); 
		bottom.setTopLeft(label);

		detailViewer = new TreeViewer(bottom, SWT.NONE);
		detailViewer.setContentProvider(new DetailsContentProvider());
		detailViewer.setLabelProvider(new DetailsLabelProvider());
		detailViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event)
			{
				IStructuredSelection selection =
					(IStructuredSelection)detailViewer.getSelection();

				if(selection.size() > 0)
					showTest(selection.getFirstElement());
			}
		});
		bottom.setContent(detailViewer.getTree()); 
		
		sashForm.setWeights(new int[] { 50, 50 });
		return sashForm;
	}

	private Composite createProgressCountPanel(Composite parent)
	{
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		composite.setLayout(layout);
		setCounterColumns(layout);
		
		counterPanel = new CounterPanel(composite);
		counterPanel.setLayoutData(new GridData(
				GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_FILL));
		progressBar = new CxxTestProgressBar(composite);
		progressBar.setLayoutData(new GridData(
				GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_FILL));
		
		return composite;
	}

	private void configureToolBar()
	{
		IActionBars actionBars = getViewSite().getActionBars();
		IToolBarManager toolBar = actionBars.getToolBarManager();
		IMenuManager viewMenu = actionBars.getMenuManager();

		toggleOrientationActions = new ToggleOrientationAction[] {
				new ToggleOrientationAction(VIEW_ORIENTATION_VERTICAL),
				new ToggleOrientationAction(VIEW_ORIENTATION_HORIZONTAL),
				new ToggleOrientationAction(VIEW_ORIENTATION_AUTOMATIC)
		};

		stopAction = new StopAction();
		stopAction.setEnabled(false);

		toolBar.add(stopAction);

		for(int i = 0; i < toggleOrientationActions.length; ++i)
			viewMenu.add(toggleOrientationActions[i]);

		viewMenu.add(new Separator());
		actionBars.updateActionBars();
	}

	public void dispose()
	{
		disposeIcons();
	}

	private void disposeIcons()
	{
		testOkIcon.dispose();
		testWarnIcon.dispose();
		testFailureIcon.dispose();
		testErrorIcon.dispose();
		assertTraceIcon.dispose();
		assertWarnIcon.dispose();
		assertFailureIcon.dispose();
		assertErrorIcon.dispose();
	}

	private CTabFolder createTestRunTabs(Composite parent)
	{
		CTabFolder tabFolder = new CTabFolder(parent, SWT.TOP);
		tabFolder.setLayoutData(new GridData(GridData.FILL_BOTH | GridData.GRAB_VERTICAL));

		TestHierarchyTab hierarchyTab = new TestHierarchyTab();
		hierarchyTab.createTabControl(tabFolder, clipboard, this);
		testRunTabs.addElement(hierarchyTab);
		
		TestMemoryTab memoryTab = new TestMemoryTab();
		memoryTab.createTabControl(tabFolder, clipboard, this);
		testRunTabs.addElement(memoryTab);

		if(tabFolder.getItemCount() > 0)
		{
			tabFolder.setSelection(0);				
			activeRunTab = (TestRunTab)testRunTabs.firstElement();
		}
				
		tabFolder.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event)
			{
				testTabChanged(event);
			}
		});

		return tabFolder;
	}

	private void testTabChanged(SelectionEvent event)
	{
		for(Enumeration e = testRunTabs.elements(); e.hasMoreElements(); )
		{
			TestRunTab v = (TestRunTab)e.nextElement();
			if(((CTabFolder)event.widget).getSelection().getText() == v.getName())
			{
//				v.setSelectedTest(activeRunTab.getSelectedTestObject());
				activeRunTab = v;
				activeRunTab.activate();
			}
		}
	}

	private void computeOrientation()
	{
		if(orientation != VIEW_ORIENTATION_AUTOMATIC)
		{
			currentOrientation = orientation;
			setOrientation(currentOrientation);
		}
		else
		{
			Point size = parent.getSize();
			if(size.x != 0 && size.y != 0)
			{
				if(size.x > size.y) 
					setOrientation(VIEW_ORIENTATION_HORIZONTAL);
				else 
					setOrientation(VIEW_ORIENTATION_VERTICAL);
			}
		}
	}

	private void setOrientation(int orientation)
	{
		if((sashForm == null) || sashForm.isDisposed())
			return;

		boolean horizontal = orientation == VIEW_ORIENTATION_HORIZONTAL;
		sashForm.setOrientation(horizontal ? SWT.HORIZONTAL : SWT.VERTICAL);
		
		for(int i = 0; i < toggleOrientationActions.length; ++i)
			toggleOrientationActions[i].setChecked(
					orientation == toggleOrientationActions[i].getOrientation());

		currentOrientation = orientation;
		GridLayout layout = (GridLayout)counterComposite.getLayout();
		setCounterColumns(layout); 
		parent.layout();
	}

	private void setCounterColumns(GridLayout layout)
	{
		if(currentOrientation == VIEW_ORIENTATION_HORIZONTAL)
			layout.numColumns = 2; 
		else
			layout.numColumns = 1;
	}

	public void setFocus()
	{
	}

	public void showTest(Object test)
	{
		new OpenTestAction(this, test).run();
	}
	
	public void stopTest()
	{
		if(currentLaunch != null)
		{
			try
			{
				currentLaunch.terminate();
			}
			catch (DebugException e) { }

			currentLaunch = null;
		}
		
		setContentDescription("");
		stopAction.setEnabled(false);
	}

	public void handleObjectSelected(Object obj)
	{
		showDetails(obj);
	}

	private void showDetails(Object obj)
	{
		if(obj == null)
			detailViewer.setInput(null);
		else
		{
			if(obj instanceof ICxxTestMethod)
			{
				ICxxTestMethod test = (ICxxTestMethod)obj;
				ICxxTestAssertion[] assertions = test.getFailedAssertions();
				detailViewer.setInput(assertions);
			}
			else if(obj instanceof ICxxTestSuiteError)
			{
				ICxxTestSuiteError test = (ICxxTestSuiteError)obj;
				detailViewer.setInput(new Object[] { test });
			}
			else if(obj instanceof IMemWatchLeak)
			{
				IMemWatchLeak leak = (IMemWatchLeak)obj;
				detailViewer.setInput(new Object[] { leak });
			}
			else if(obj instanceof IMemWatchInfo)
			{
				detailViewer.setInput(obj);
			}
			
			detailViewer.expandAll();
		}
	}

	public static Image createImage(String path)
	{
		try
		{
			URL base = Platform.getBundle(CxxTestPlugin.PLUGIN_ID).
				getEntry("/icons/full/");
			URL url = new URL(base, path);
			
			ImageDescriptor id= ImageDescriptor.createFromURL(url);
			return id.createImage();
		}
		catch (MalformedURLException e)
		{
		}

		return null;
	}

	public void testRunStarted(ICProject project, ILaunch launch)
	{
		launchedProject = project;

		for(Enumeration e = testRunTabs.elements(); e.hasMoreElements(); )
		{
			TestRunTab v = (TestRunTab)e.nextElement();
			v.testRunStarted(project, launch);
		}

		counterPanel.reset();
		progressBar.reset();

		currentLaunch = launch;

		String name = "";
		try
		{
			name = launch.getLaunchConfiguration().getAttribute(
					ICDTLaunchConfigurationConstants.ATTR_PROJECT_NAME, "");
		}
		catch (CoreException e) { }

		detailViewer.setInput(null);
		setContentDescription("Running " + name + "...");

		stopAction.setEnabled(true);
	}
	
	public ICProject getLaunchedProject()
	{
		return launchedProject;
	}

	public void testRunEnded()
	{
		currentLaunch = null;
		stopTest();

		for(Enumeration e = testRunTabs.elements(); e.hasMoreElements(); )
		{
			TestRunTab v = (TestRunTab)e.nextElement();
			v.testRunEnded();
		}
	}
	
	public void setSummary(ICxxTestSuite[] suites)
	{
		if(suites == null)
			return;

		if(suites.length > 0)
			getViewSite().getPage().activate(this);

		int totalTests = 0, failedTests = 0, errorTests = 0;

		for(int i = 0; i < suites.length; i++)
		{
			ICxxTestSuiteChild[] tests = suites[i].getChildren(true);			
			totalTests += tests.length;
			
			for(int j = 0; j < tests.length; j++)
			{
				if(tests[j].getStatus() == ICxxTestBase.STATUS_ERROR)
					errorTests++;
				else if(tests[j].getStatus() == ICxxTestBase.STATUS_FAILED)
					failedTests++;
			}
		}

		counterPanel.reset();
		counterPanel.setRunValue(totalTests);
		counterPanel.setFailureValue(failedTests);
		counterPanel.setErrorValue(errorTests);
		
		progressBar.reset();
		progressBar.setMaximum(totalTests);
		progressBar.step(failedTests + errorTests);
		progressBar.refresh(failedTests + errorTests > 0);
	}
}
