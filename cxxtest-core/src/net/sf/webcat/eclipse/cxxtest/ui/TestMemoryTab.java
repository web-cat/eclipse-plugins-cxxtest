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

import net.sf.webcat.eclipse.cxxtest.ICxxTestConstants;
import net.sf.webcat.eclipse.cxxtest.MemWatchResultsHandler;
import net.sf.webcat.eclipse.cxxtest.model.ICxxTestBase;
import net.sf.webcat.eclipse.cxxtest.model.IMemWatchInfo;
import net.sf.webcat.eclipse.cxxtest.model.IMemWatchLeak;

import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.debug.core.ILaunch;
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
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

/**
 * A tab that displays a summary of memory allocation and leaks that occurred
 * during the program execution. 
 * 
 * @author Tony Allowatt (Virginia Tech Computer Science)
 */
public class TestMemoryTab extends TestRunTab
	implements ISelectionProvider
{
	private TreeViewer viewer;

	private IMemWatchInfo memWatchInfo;

	private MemWatchContentProvider viewerContent;

	private ICProject project;
	
	private ListenerList selectionListeners= new ListenerList();
	
	private TestRunnerViewPart testRunnerView;
	
	private final Image leakNoArrayIcon = TestRunnerViewPart.createImage("obj16/leak_noarray.gif");
	private final Image leakArrayIcon = TestRunnerViewPart.createImage("obj16/leak_array.gif");
	private final Image memoryTabIcon = TestRunnerViewPart.createImage("obj16/memory.gif");

	private class MemWatchInfoInput
	{
		public IMemWatchInfo info;
		
		public MemWatchInfoInput(IMemWatchInfo info)
		{
			this.info = info;
		}
	}

	private class MemWatchContentProvider implements ITreeContentProvider
	{
		public Object[] getChildren(Object parent)
		{
			if(parent instanceof IMemWatchInfo)
				return ((IMemWatchInfo)parent).getLeaks();
			else
				return null;
		}

		public Object getParent(Object element)
		{
			return null;
		}

		public boolean hasChildren(Object element)
		{
			if(element instanceof IMemWatchInfo)
				return true;
			else
				return false;
		}

		public Object[] getElements(Object inputElement)
		{
			MemWatchInfoInput input = (MemWatchInfoInput)inputElement;
			
			if(input.info == null)
				return new Object[0];
			else
				return new Object[] { input.info };
		}

		public void dispose() { }

		public void inputChanged(
				Viewer viewer, Object oldInput, Object newInput) { }
	}

	private class MemWatchLabelProvider extends LabelProvider
	{
		public String getText(Object element)
		{
			if(element instanceof IMemWatchInfo)
			{
				String msg = "" + memWatchInfo.getLeaks().length +
					" memory leaks found";
				return msg;
			}
			else if(element instanceof IMemWatchLeak)
			{
				IMemWatchLeak leak = (IMemWatchLeak)element;
				return leak.toString();
			}
			else
				return element.toString();
		}
		
		public Image getImage(Object element)
		{
			if(element instanceof IMemWatchInfo)
			{
				return memoryTabIcon;
			}
			else if(element instanceof IMemWatchLeak)
			{
				IMemWatchLeak leak = (IMemWatchLeak)element;
				
				if(leak.isArray())
					return leakArrayIcon;
				else
					return leakNoArrayIcon;
			}

			return null;
		}
	}

	public void createTabControl(CTabFolder tabFolder, Clipboard clipboard, TestRunnerViewPart runner)
	{
		testRunnerView = runner;
		
		CTabItem memoryTab = new CTabItem(tabFolder, SWT.NONE);
		memoryTab.setText(getName());
		memoryTab.setImage(memoryTabIcon);
		
		Composite testTreePanel= new Composite(tabFolder, SWT.NONE);
		GridLayout gridLayout= new GridLayout();
		gridLayout.marginHeight= 0;
		gridLayout.marginWidth= 0;
		testTreePanel.setLayout(gridLayout);
		
		GridData gridData= new GridData(
				GridData.GRAB_HORIZONTAL | GridData.GRAB_VERTICAL);
		testTreePanel.setLayoutData(gridData);
		
		memoryTab.setControl(testTreePanel);
		memoryTab.setToolTipText("Memory Usage"); 
		
		viewer = new TreeViewer(testTreePanel, SWT.V_SCROLL | SWT.SINGLE);
		gridData= new GridData(GridData.FILL_BOTH |
				GridData.GRAB_HORIZONTAL | GridData.GRAB_VERTICAL);
		viewerContent = new MemWatchContentProvider();
		viewer.setContentProvider(viewerContent);
		viewer.setLabelProvider(new MemWatchLabelProvider());

		viewer.getTree().setLayoutData(gridData);
		OpenStrategy handler = new OpenStrategy(viewer.getTree());
		handler.addPostSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e)
			{
				fireSelectionChanged();
			}
		});

		addListeners();
	}

	private void disposeIcons()
	{
		leakNoArrayIcon.dispose();
		leakArrayIcon.dispose();

		memoryTabIcon.dispose();
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
		return "Memory"; 
	}
	
	public void setSelectedTest(ICxxTestBase testObject)
	{
		viewer.setSelection(new StructuredSelection(testObject));
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
		viewer.getTree().addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				activate();
			}
		});
		
		viewer.getTree().addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent e) {
				disposeIcons();
			}
		});
	}
	
	public void setMemWatchInfo(IMemWatchInfo mwInfo)
	{
		this.memWatchInfo = mwInfo;

		viewer.setInput(new MemWatchInfoInput(mwInfo));
		viewer.expandAll();
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
		
		setMemWatchInfo(null);
	}

	public void testRunEnded()
	{
		try
		{
			IFile resultsFile = project.getProject().getFile(
					ICxxTestConstants.MEMWATCH_RESULTS_FILE);
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
			final MemWatchResultsHandler handler = new MemWatchResultsHandler();
	
			try
			{
				XMLReader reader = XMLReaderFactory.createXMLReader();
				reader.setContentHandler(handler);
				reader.parse(source);
			}
			catch (SAXException e) { }
			catch (IOException e) { }
	
			stream.close();
	
			IMemWatchInfo mwInfo = handler.getInfo();
			setMemWatchInfo(mwInfo);
		}
		catch(IOException e) { }
	}
}
