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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;

import net.sf.webcat.eclipse.cxxtest.CxxTestResultsHandler;
import net.sf.webcat.eclipse.cxxtest.ICxxTestConstants;
import net.sf.webcat.eclipse.cxxtest.model.ICxxTestAssertion;
import net.sf.webcat.eclipse.cxxtest.model.ICxxTestBase;
import net.sf.webcat.eclipse.cxxtest.model.ICxxTestMethod;
import net.sf.webcat.eclipse.cxxtest.model.ICxxTestSuite;

import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.util.ListenerList;
import org.eclipse.jface.util.OpenStrategy;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.texteditor.MarkerUtilities;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

/**
 * A tab that displays a hierarchical view of the test suites that were
 * executed by CxxTest.
 * 
 * Greatly influenced by the same JUnit class.
 * 
 * @author Tony Allowatt (Virginia Tech Computer Science)
 */
public class TestHierarchyTab extends TestRunTab
	implements IMenuListener, ISelectionProvider
{
	private TreeViewer viewer;

	private TestSuiteContentProvider viewerContent;

	private ICProject project;
	
	private ICxxTestSuite[] suites;

	private ListenerList selectionListeners= new ListenerList();
	
	private TestRunnerViewPart testRunnerView;
	
	private final Image testOkIcon = TestRunnerViewPart.createImage("obj16/testok.gif");
	private final Image testErrorIcon = TestRunnerViewPart.createImage("obj16/testerr.gif");
	private final Image testFailureIcon = TestRunnerViewPart.createImage("obj16/testfail.gif");
	private final Image testWarnIcon = TestRunnerViewPart.createImage("obj16/testwarn.gif");
	private final Image hierarchyIcon = TestRunnerViewPart.createImage("obj16/testhier.gif");
	private final Image suiteIcon = TestRunnerViewPart.createImage("obj16/tsuite.gif");
	private final Image suiteErrorIcon = TestRunnerViewPart.createImage("obj16/tsuiteerror.gif");
	private final Image suiteFailIcon = TestRunnerViewPart.createImage("obj16/tsuitefail.gif");
	private final Image suiteWarnIcon = TestRunnerViewPart.createImage("obj16/tsuitewarn.gif");
	private final Image testIcon = TestRunnerViewPart.createImage("obj16/test.gif");
	
	private class TestSuiteContentProvider implements ITreeContentProvider
	{
		public Object[] getChildren(Object parent)
		{
			if(parent instanceof ICxxTestSuite)
				return ((ICxxTestSuite)parent).getTests();
			else
				return null;
		}

		public Object getParent(Object element)
		{
			return ((ICxxTestBase)element).getParent();
		}

		public boolean hasChildren(Object element)
		{
			if(element instanceof ICxxTestSuite)
				return true;
			else
				return false;
		}

		public Object[] getElements(Object inputElement)
		{
			return (Object[])inputElement;
		}

		public void dispose() { }

		public void inputChanged(
				Viewer viewer, Object oldInput, Object newInput) { }
	}

	private class TestSuiteLabelProvider extends LabelProvider
	{
		public String getText(Object element)
		{
			return element.toString();
		}
		
		public Image getImage(Object element)
		{
			int status = ((ICxxTestBase)element).getStatus();
			
			if(element instanceof ICxxTestSuite)
			{
				switch(status)
				{
					case ICxxTestBase.STATUS_OK:
						return suiteIcon;

					case ICxxTestBase.STATUS_WARNING:
						return suiteWarnIcon;

					case ICxxTestBase.STATUS_FAILED:
						return suiteFailIcon;

					case ICxxTestBase.STATUS_ERROR:
						return suiteErrorIcon;
				}
			}
			else if(element instanceof ICxxTestMethod)
			{
				switch(status)
				{
					case ICxxTestBase.STATUS_OK:
						return testOkIcon;

					case ICxxTestBase.STATUS_WARNING:
						return testWarnIcon;

					case ICxxTestBase.STATUS_FAILED:
						return testFailureIcon;

					case ICxxTestBase.STATUS_ERROR:
						return testErrorIcon;
				}
			}

			return null;
		}
	}

	private class ExpandAllAction extends Action
	{
		public ExpandAllAction()
		{
			setText("Expand All");  
			setToolTipText("Expand All Test Suites");  
		}
		
		public void run()
		{
			expandAll();
		}
	}

	public void createTabControl(CTabFolder tabFolder, Clipboard clipboard, TestRunnerViewPart runner)
	{
		testRunnerView = runner;
		
		CTabItem hierarchyTab= new CTabItem(tabFolder, SWT.NONE);
		hierarchyTab.setText(getName());
		hierarchyTab.setImage(hierarchyIcon);
		
		Composite testTreePanel= new Composite(tabFolder, SWT.NONE);
		GridLayout gridLayout= new GridLayout();
		gridLayout.marginHeight= 0;
		gridLayout.marginWidth= 0;
		testTreePanel.setLayout(gridLayout);
		
		GridData gridData= new GridData(
				GridData.GRAB_HORIZONTAL | GridData.GRAB_VERTICAL);
		testTreePanel.setLayoutData(gridData);
		
		hierarchyTab.setControl(testTreePanel);
		hierarchyTab.setToolTipText("Test Hierarchy"); 
		
		viewer = new TreeViewer(testTreePanel, SWT.V_SCROLL | SWT.SINGLE);
		gridData= new GridData(GridData.FILL_BOTH |
				GridData.GRAB_HORIZONTAL | GridData.GRAB_VERTICAL);
		viewerContent = new TestSuiteContentProvider();
		viewer.setContentProvider(viewerContent);
		viewer.setLabelProvider(new TestSuiteLabelProvider());

		viewer.getTree().setLayoutData(gridData);
		OpenStrategy handler = new OpenStrategy(viewer.getTree());
		handler.addPostSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e)
			{
				fireSelectionChanged();
			}
		});

		initMenu();
		addListeners();
	}

	private void disposeIcons()
	{
		testOkIcon.dispose();
		testWarnIcon.dispose();
		testFailureIcon.dispose();
		testErrorIcon.dispose();

		hierarchyIcon.dispose();
		testIcon.dispose();

		suiteIcon.dispose();
		suiteWarnIcon.dispose(); 
		suiteFailIcon.dispose(); 
		suiteErrorIcon.dispose();
	}
	
	private void initMenu()
	{
		MenuManager menuMgr= new MenuManager("#PopupMenu"); //$NON-NLS-1$
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(this);
		testRunnerView.getSite().registerContextMenu(menuMgr, this);
		Menu menu= menuMgr.createContextMenu(viewer.getTree());
		viewer.getTree().setMenu(menu);	
	}
	
	public Object getSelectedObject()
	{
		IStructuredSelection sel =
			(IStructuredSelection)viewer.getSelection();

		if(sel.size() == 0)
			return null;
		else
			return sel.getFirstElement();
	}

	public String getName() {
		return "Hierarchy"; 
	}
	
	public void setSelectedTest(ICxxTestBase testObject)
	{
		viewer.setSelection(new StructuredSelection(testObject));
	}

	private void expandFailedTests()
	{
		if(suites == null)
			return;

		for(int i = 0; i < suites.length; i++)
		{
			boolean expand = (suites[i].getStatus() != ICxxTestBase.STATUS_OK); 
			viewer.setExpandedState(suites[i], expand);
		}
	}

	public void activate()
	{
		testRunnerView.handleObjectSelected(getSelectedObject());
	}
	
	public void setFocus()
	{
		viewer.getTree().setFocus();
	}

	private void addListeners()
	{
		viewer.getTree().addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				activate();
			}
			public void widgetDefaultSelected(SelectionEvent e) {
				handleDoubleClick(null);
			}
		});
		
		viewer.getTree().addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent e) {
				disposeIcons();
			}
		});

		viewer.getTree().addMouseListener(new MouseAdapter() {
			public void mouseDoubleClick(MouseEvent e) {
				handleDoubleClick(e);
			}
		});
	}
	
	private void handleDoubleClick(MouseEvent e)
	{
		ICxxTestBase test = (ICxxTestBase)getSelectedObject();
		
		if(test == null)
			return;

		OpenTestAction action = new OpenTestAction(testRunnerView, test);

		if(action.isEnabled())
			action.run();
	}

	public void menuAboutToShow(IMenuManager manager)
	{
		IStructuredSelection selection = (IStructuredSelection)viewer.getSelection();
		if(selection != null && selection.size() > 0)
		{
			ICxxTestBase test = (ICxxTestBase)selection.getFirstElement();

			manager.add(new OpenTestAction(testRunnerView, test));
			manager.add(new Separator());
			manager.add(new Separator());
			manager.add(new ExpandAllAction());
		}

		manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
		manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS + "-end"));
	}	

	public void setSuites(ICxxTestSuite[] suites)
	{
		this.suites = suites;

		viewer.setInput(suites);
		expandFailedTests();
	}
	
	protected void expandAll()
	{
		viewer.expandAll();
	}
	
	public void addSelectionChangedListener(ISelectionChangedListener listener)
	{
		selectionListeners.add(listener);
	}

	public ISelection getSelection()
	{
		return viewer.getSelection();
	}

	public void removeSelectionChangedListener(ISelectionChangedListener listener)
	{
		selectionListeners.remove(listener);
	}

	public void setSelection(ISelection selection)
	{
		viewer.setSelection(selection, true);
	}
	
	private void fireSelectionChanged()
	{
		SelectionChangedEvent event = new SelectionChangedEvent(this, getSelection());
		Object[] listeners = selectionListeners.getListeners();
		for (int i = 0; i < listeners.length; i++)
		{
			ISelectionChangedListener listener = (ISelectionChangedListener)listeners[i];
			listener.selectionChanged(event);
		}	
	}

	public void testRunStarted(ICProject project, ILaunch launch)
	{
		this.project = project;
		
		setSuites(null);
	}

	public void testRunEnded()
	{
		try
		{
			IFile resultsFile = project.getProject().getFile(
					ICxxTestConstants.TEST_RESULTS_FILE);
			File resultsPath = resultsFile.getLocation().toFile();
	
			// When the CxxTest results file is generated, Eclipse
			// autodetects this change in the project contents and
			// attempts to run the managed make process again
			// (which has no effect because there's nothing new to
			// make, but it does delete warning markers). But it
			// appears setting the derived flag (which does apply
			// here, as it's derived from the runner) will prevent
			// managed make from doing an extraneous run.
			try { resultsFile.setDerived(true); }
			catch (CoreException e1) { }
	
			FileInputStream stream = new FileInputStream(resultsPath);
			InputSource source = new InputSource(stream);
			final CxxTestResultsHandler handler = new CxxTestResultsHandler();
	
			try
			{
				XMLReader reader = XMLReaderFactory.createXMLReader();
				reader.setContentHandler(handler);
				reader.parse(source);
			}
			catch (SAXException e) { }
			catch (IOException e) { }
	
			stream.close();
	
			final ICxxTestSuite[] suiteArray = handler.getSuites();
			setSuites(suiteArray);
			testRunnerView.setSummary(suiteArray);
	
			setFailureMarkers(suiteArray);
		}
		catch(IOException e) { }
	}

	private void setFailureMarkers(ICxxTestSuite[] suites)
	{
		for(int i = 0; i < suites.length; i++)
		{
			ICxxTestSuite suite = suites[i];
			
			for(int j = 0; j < suite.getTests().length; j++)
			{
				ICxxTestMethod test = suite.getTests()[j];
				IFile file = project.getProject().getFile(suite.getFile());

				for(int k = 0; k < test.getFailedAssertions().length; k++)
				{
					ICxxTestAssertion assertion = test.getFailedAssertions()[k];
					setAssertionMarker(file, assertion);
				}
			}
		}
	}
	
	private void setAssertionMarker(IFile file, ICxxTestAssertion assertion)
	{
		try
		{
			HashMap attrs = new HashMap();
			attrs.put(IMarker.MESSAGE, assertion.getMessage(false));
			attrs.put(IMarker.LINE_NUMBER,
					new Integer(assertion.getLineNumber()));
			attrs.put(IMarker.SEVERITY, new Integer(IMarker.SEVERITY_INFO));
			attrs.put(ICxxTestConstants.ATTR_ASSERTIONTYPE,
					new Integer(assertion.getStatus()));

			MarkerUtilities.createMarker(file, attrs,
					ICxxTestConstants.MARKER_FAILED_TEST);
		}
		catch(CoreException e) { }
	}
}
