package net.sf.webcat.eclipse.cxxtest.wizards;

import java.util.ArrayList;
import java.util.Vector;

import net.sf.webcat.eclipse.cxxtest.wizards.ui.SWTUtil;

import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.IMethodDeclaration;
import org.eclipse.cdt.core.model.IStructure;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.core.parser.ast.ASTAccessVisibility;
import org.eclipse.cdt.ui.CElementLabelProvider;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.dialogs.ContainerCheckedTreeViewer;

public class NewCxxTestSuiteWizardPageTwo extends WizardPage
{
	private static final String PAGE_NAME = "NewCxxTestSuiteWizardPageTwo";

	private static final String PAGE_TITLE = "Test Methods";
	private static final String PAGE_DESCRIPTION =
		"Select methods for which test method stubs should be created.";

	private ContainerCheckedTreeViewer methodsTree;
	private Button selectAllButton;
	private Button deselectAllButton;
	private Label selectedMethodsLabel;

	private IPath headerUnderTestPath;
	private Object[] checkedObjects;

	public NewCxxTestSuiteWizardPageTwo()
	{
		super(PAGE_NAME);
		
		setTitle(PAGE_TITLE);
		setDescription(PAGE_DESCRIPTION);
	}

	public ICElement getSelectedElement(IStructuredSelection selection)
	{
		ICElement element = null;
		if(selection != null && !selection.isEmpty())
		{
			Object selElem = selection.getFirstElement();
			
			if(selElem instanceof IAdaptable)
			{
				IAdaptable adaptable = (IAdaptable)selElem;
				element = (ICElement)adaptable.getAdapter(ICElement.class);
				
				if(element == null)
				{
					IResource resource = (IResource)adaptable.getAdapter(IResource.class);
					if(resource != null && resource.getType() != IResource.ROOT)
					{
						while(element == null && resource.getType() != IResource.PROJECT)
						{
							resource = resource.getParent();
							element = (ICElement)resource.getAdapter(ICElement.class);
						}
						
						if(element == null)
						{
							element = CoreModel.getDefault().create(resource);
						}
					}
				}
			}
		}
		
		return element;
	}

	public void createControl(Composite parent)
	{
		Composite container = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		container.setLayout(layout);

		createMethodsTreeControls(container);

		setControl(container);
	}

	private void createMethodsTreeControls(Composite container)
	{
		Label label= new Label(container, SWT.LEFT | SWT.WRAP);
		label.setFont(container.getFont());
		label.setText("Available methods:"); 
		GridData gd = new GridData();
		gd.horizontalSpan = 2;
		label.setLayoutData(gd);

		methodsTree = new ContainerCheckedTreeViewer(container, SWT.BORDER);
		gd = new GridData(GridData.FILL_BOTH | GridData.GRAB_HORIZONTAL | GridData.GRAB_VERTICAL);
		gd.heightHint = 180;
		methodsTree.getTree().setLayoutData(gd);

		methodsTree.setLabelProvider(new CElementLabelProvider());
		methodsTree.setAutoExpandLevel(2);			
		methodsTree.addCheckStateListener(new ICheckStateListener() {
			public void checkStateChanged(CheckStateChangedEvent event) {
				doCheckedStateChanged();
			}	
		});
/*		methodsTree.addFilter(new ViewerFilter() {
			public boolean select(Viewer viewer, Object parentElement, Object element)
			{
				try
				{
					if (element instanceof IMethodDeclaration)
					{
						IMethodDeclaration method = (IMethodDeclaration) element;
						return !method.isStatic();
					}
				}
				catch(CModelException e)
				{
					e.printStackTrace();
				}

				return true;
			}
		});
*/

		Composite buttonContainer = new Composite(container, SWT.NONE);
		gd = new GridData(GridData.FILL_VERTICAL);
		buttonContainer.setLayoutData(gd);
		GridLayout buttonLayout = new GridLayout();
		buttonLayout.marginWidth = 0;
		buttonLayout.marginHeight = 0;
		buttonContainer.setLayout(buttonLayout);

		selectAllButton = new Button(buttonContainer, SWT.PUSH);
		selectAllButton.setText("Select All"); 
		gd= new GridData(GridData.FILL_HORIZONTAL | GridData.VERTICAL_ALIGN_BEGINNING);
		selectAllButton.setLayoutData(gd);
		selectAllButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				methodsTree.setCheckedElements((Object[]) methodsTree.getInput());
				doCheckedStateChanged();
			}
		});
		SWTUtil.setButtonDimensionHint(selectAllButton);

		deselectAllButton = new Button(buttonContainer, SWT.PUSH);
		deselectAllButton.setText("Deselect All"); 
		gd= new GridData(GridData.FILL_HORIZONTAL | GridData.VERTICAL_ALIGN_BEGINNING);
		deselectAllButton.setLayoutData(gd);
		deselectAllButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				methodsTree.setCheckedElements(new Object[0]);
				doCheckedStateChanged();
			}
		});
		SWTUtil.setButtonDimensionHint(deselectAllButton);

		/* No of selected methods label */
		selectedMethodsLabel = new Label(container, SWT.LEFT);
		selectedMethodsLabel.setFont(container.getFont());
		doCheckedStateChanged();
		gd= new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 1;
		selectedMethodsLabel.setLayoutData(gd);
		
		Label emptyLabel = new Label(container, SWT.LEFT);
		gd= new GridData();
		gd.horizontalSpan = 1;
		emptyLabel.setLayoutData(gd);
	}

	public void setVisible(boolean visible)
	{
		super.setVisible(visible);

		if (visible)
		{
			if(headerUnderTestPath == null)
				return;
			
			ArrayList types = null;
			try
			{
				types = new ArrayList();
				
				ICElement element = CoreModel.getDefault().create(headerUnderTestPath);
				if(element instanceof ITranslationUnit)
				{
					ITranslationUnit unit = (ITranslationUnit)element;
					types.addAll(unit.getChildrenOfType(ICElement.C_CLASS));
				}
			}
			catch(CModelException e)
			{
				e.printStackTrace();
			}

			methodsTree.setContentProvider(new MethodsTreeContentProvider(types.toArray()));
			
			if (types == null)
				types= new ArrayList();

			methodsTree.setInput(types.toArray());
//			methodsTree.setSelection(new StructuredSelection(fClassToTest), true);
			doCheckedStateChanged();
			
			methodsTree.getControl().setFocus();
		}
		else
		{
			//saveWidgetValues();
		}
	}

	public void setHeaderUnderTestPath(IPath path)
	{
		headerUnderTestPath = path;
	}

	private void doCheckedStateChanged()
	{
		Object[] checked = methodsTree.getCheckedElements();
		checkedObjects = checked;
		
		int checkedMethodCount= 0;
		for(int i= 0; i < checked.length; i++)
		{
			if(checked[i] instanceof IMethodDeclaration)
				checkedMethodCount++;
		}

		String label = Integer.toString(checkedMethodCount);
		if(checkedMethodCount == 1)
			label += " method selected."; 
		else
			label += " methods selected."; 

		selectedMethodsLabel.setText(label);
	}

	public IMethodDeclaration[] getCheckedMethods()
	{
		int methodCount= 0;
		for(int i = 0; i < checkedObjects.length; i++)
		{
			if(checkedObjects[i] instanceof IMethodDeclaration)
				methodCount++;
		}
		
		IMethodDeclaration[] checkedMethods= new IMethodDeclaration[methodCount];
		int j = 0;
		for(int i = 0; i < checkedObjects.length; i++)
		{
			if(checkedObjects[i] instanceof IMethodDeclaration)
			{
				checkedMethods[j]= (IMethodDeclaration)checkedObjects[i];
				j++;
			}
		}

		return checkedMethods;
	}
	
	private static class MethodsTreeContentProvider implements ITreeContentProvider
	{
		private Object[] fTypes;
		private IMethodDeclaration[] fMethods;
		private final Object[] fEmpty = new Object[0];

		public MethodsTreeContentProvider(Object[] types)
		{
			fTypes= types;
			Vector methods= new Vector();
			for (int i = types.length-1; i > -1; i--) {
				Object object = types[i];
				if (object instanceof IStructure) {
					IStructure type = (IStructure) object;

					try
					{
						IMethodDeclaration[] currMethods = type.getMethods();
						for_currMethods:
						for (int j = 0; j < currMethods.length; j++)
						{
							IMethodDeclaration currMethod = currMethods[j];
							if (!currMethod.isDestructor() &&
									currMethod.getVisibility() == ASTAccessVisibility.PUBLIC)
							{
								for (int k = 0; k < methods.size(); k++)
								{
									IMethodDeclaration m= ((IMethodDeclaration)methods.get(k));
									if (m.getElementName().equals(currMethod.getElementName())
										&& m.getSignature().equals(currMethod.getSignature()))
									{
										methods.set(k,currMethod);
										continue for_currMethods;
									}
								}
								methods.add(currMethod);
							}
						}
					}
					catch(CModelException e)
					{
						e.printStackTrace();
					}
				}
			}
			fMethods= new IMethodDeclaration[methods.size()];
			methods.copyInto(fMethods);
		}
		
		/*
		 * @see ITreeContentProvider#getChildren(Object)
		 */
		public Object[] getChildren(Object parentElement)
		{
			if (parentElement instanceof IStructure)
			{
				ArrayList result= new ArrayList(fMethods.length);
				for (int i= 0; i < fMethods.length; i++)
				{
					result.add(fMethods[i]);
				}

				return result.toArray();
			}
			return fEmpty;
		}

		/*
		 * @see ITreeContentProvider#getParent(Object)
		 */
		public Object getParent(Object element)
		{
			if (element instanceof IMethodDeclaration) 
				return ((IMethodDeclaration)element).getParent();
			return null;
		}

		/*
		 * @see ITreeContentProvider#hasChildren(Object)
		 */
		public boolean hasChildren(Object element) {
			return getChildren(element).length > 0;
		}

		/*
		 * @see IStructuredContentProvider#getElements(Object)
		 */
		public Object[] getElements(Object inputElement) {
			return fTypes;
		}

		/*
		 * @see IContentProvider#dispose()
		 */
		public void dispose() {
		}

		/*
		 * @see IContentProvider#inputChanged(Viewer, Object, Object)
		 */
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}
		
		public IMethodDeclaration[] getAllMethods() {
			return fMethods;
		}
	}
}
