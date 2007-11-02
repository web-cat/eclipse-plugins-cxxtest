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

import net.sf.webcat.eclipse.cxxtest.ICxxTestConstants;
import net.sf.webcat.eclipse.cxxtest.model.ICxxTestAssertion;
import net.sf.webcat.eclipse.cxxtest.model.ICxxTestBase;
import net.sf.webcat.eclipse.cxxtest.model.ICxxTestMethod;
import net.sf.webcat.eclipse.cxxtest.model.ICxxTestSuite;
import net.sf.webcat.eclipse.cxxtest.model.ICxxTestSuiteChild;
import net.sf.webcat.eclipse.cxxtest.model.ICxxTestSuiteError;
import net.sf.webcat.eclipse.cxxtest.xml.ContextualSAXHandler;
import net.sf.webcat.eclipse.cxxtest.xml.testresults.DocumentContext;

import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.internal.ui.util.EditorUtility;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.util.OpenStrategy;
import org.eclipse.jface.viewers.IFontProvider;
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
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledFormText;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.ui.texteditor.MarkerUtilities;
import org.xml.sax.InputSource;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

/**
 * A tab that displays a hierarchical view of the test suites that were
 * executed by CxxTest.
 * 
 * Greatly influenced by the same JUnit class.
 * 
 * @author Tony Allevato (Virginia Tech Computer Science)
 */
public class TestHierarchyTab extends TestRunTab
	implements IMenuListener, ISelectionProvider
{
	private StackLayout stackLayout;

	private TreeViewer viewer;
	
	private FormToolkit toolkit;

	private ScrolledFormText errorMsgField;

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
				return ((ICxxTestSuite)parent).getChildren(false);
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

	private class TestSuiteLabelProvider extends LabelProvider implements IFontProvider
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
			else if(element instanceof ICxxTestSuiteChild)
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

		public Font getFont(Object element)
		{
			if(element instanceof ICxxTestSuiteError)
			{
				FontData[] fd = JFaceResources.getDefaultFont().getFontData();
				fd[0].setStyle(fd[0].getStyle() | SWT.BOLD);
				return new Font(Display.getCurrent(), fd);
			}
			else
			{
				return null;
			}
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
		stackLayout = new StackLayout();
		testTreePanel.setLayout(stackLayout);
		
		GridData gridData = new GridData(SWT.FILL, SWT.FILL, false, false);
		testTreePanel.setLayoutData(gridData);
		
		hierarchyTab.setControl(testTreePanel);
		hierarchyTab.setToolTipText("Test Hierarchy"); 

		viewer = new TreeViewer(testTreePanel, SWT.V_SCROLL | SWT.SINGLE);
		gridData = new GridData(GridData.FILL_BOTH |
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

		Display display = tabFolder.getDisplay();

		toolkit = new FormToolkit(display);
		errorMsgField = new ScrolledFormText(testTreePanel, true);
		errorMsgField.setBackground(toolkit.getColors().getBackground());
		errorMsgField.getFormText().setColor("error",
				toolkit.getColors().createColor("error", 255, 0, 0));

		errorMsgField.getFormText().addHyperlinkListener(new HyperlinkAdapter()
		{
			public void linkActivated(HyperlinkEvent e)
			{
				String link = e.getHref().toString();
				openLink(link);
			}
		});

		stackLayout.topControl = viewer.getControl();

		initMenu();
		addListeners();
	}

	private void openLink(String link)
	{
		if(link.startsWith("cxxtest.log"))
		{
			String[] parts = link.split(":");
			int lineNumber = 1;

			if(parts.length == 2)
				lineNumber = Integer.parseInt(parts[1]);
			
			ICProject project = testRunnerView.getLaunchedProject();
			IFile file = project.getProject().getFile(ICxxTestConstants.TEST_RESULTS_FILE);
			
			try
			{
				ITextEditor editor =
					(ITextEditor)EditorUtility.openInEditor(file, true);

				try
				{
					IDocument document= editor.getDocumentProvider().
						getDocument(editor.getEditorInput());

					editor.selectAndReveal(
							document.getLineOffset(lineNumber - 1),
							document.getLineLength(lineNumber - 1));
				}
				catch(BadLocationException ex) { }
			}
			catch (PartInitException ex) { }
			catch (CModelException ex) { }
		}
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

		toolkit.dispose();
	}

	private void initMenu()
	{
		MenuManager menuMgr = new MenuManager("#PopupMenu"); //$NON-NLS-1$
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

	private void setSuites(ICxxTestSuite[] suites)
	{
		this.suites = suites;

		viewer.setInput(suites);
		expandFailedTests();

		errorMsgField.setText("");
		stackLayout.topControl = viewer.getControl();
		viewer.getControl().getParent().layout();
	}
	
	private String escapeXMLString(String str)
	{
		StringBuffer buf = new StringBuffer();
		
		for(int i = 0; i < str.length(); i++)
		{
			char ch = str.charAt(i);
			
			switch(ch)
			{
				case '&':  buf.append("&amp;"); break; 
				case '\'': buf.append("&apos;"); break; 
				case '"':  buf.append("&quot;"); break; 
				case '<':  buf.append("&lt;"); break; 
				case '>':  buf.append("&gt;"); break;
				default:   buf.append(ch); break;
			}
		}
		
		return buf.toString();
	}

	private void setParseError(Exception e)
	{
		StringBuffer msg = new StringBuffer();
		msg.append("<form>");

		msg.append("<p><b>Error:</b></p>");

		if(e instanceof SAXParseException)
		{
			SAXParseException spe = (SAXParseException)e;
			msg.append("<p>An unexpected error occurred while processing the ");
			msg.append("<a href=\"cxxtest.log:" + spe.getLineNumber() +
					"\">CxxTest results log</a>");
			msg.append(", line " + spe.getLineNumber() + ".</p>");
			msg.append("<p><span color=\"error\">");
			msg.append(escapeXMLString(e.getMessage()));
			msg.append("</span></p>");
		}
		else
		{
			msg.append("<p>An unexpected error occurred while processing the ");
			msg.append("<a href=\"cxxtest.log\">CxxTest results log</a>.</p>");
			msg.append("<p><span color=\"error\">");
			msg.append(escapeXMLString(e.getMessage()));
			msg.append("</span></p>");
		}

		msg.append("</form>");

		errorMsgField.setText(msg.toString());

		stackLayout.topControl = errorMsgField;
		errorMsgField.getParent().layout();
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
			try
			{
				resultsFile.setDerived(true);
			}
			catch (CoreException e1) { }

			FileInputStream stream = new FileInputStream(resultsPath);
			InputSource source = new InputSource(stream);
			
			DocumentContext docContext = new DocumentContext();
			final ContextualSAXHandler handler = new ContextualSAXHandler(docContext);
	
			try
			{
				XMLReader reader = XMLReaderFactory.createXMLReader();
				reader.setContentHandler(handler);
				reader.parse(source);
			}
			catch (Exception e)
			{
				setParseError(e);
				return;
			}
			finally
			{
				stream.close();
			}
	
			ICxxTestSuite[] suiteArray = docContext.getSuites();
			setSuites(suiteArray);
			testRunnerView.setSummary(suiteArray);
			setFailureMarkers(suiteArray);
		}
		catch(IOException e)
		{
			setParseError(e);
		}
	}

	private void setFailureMarkers(ICxxTestSuite[] suites)
	{
		for(int i = 0; i < suites.length; i++)
		{
			ICxxTestSuite suite = suites[i];
			ICxxTestSuiteChild[] children = suite.getChildren(true);

			for(int j = 0; j < children.length; j++)
			{
				ICxxTestMethod test = (ICxxTestMethod)children[j];
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
			HashMap<String, Object> attrs = new HashMap<String, Object>();

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
