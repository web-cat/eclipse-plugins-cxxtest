package net.sf.webcat.eclipse.cxxtest.wizards;

import net.sf.webcat.eclipse.cxxtest.wizards.dialogs.TranslationUnitSelectionDialog;
import net.sf.webcat.eclipse.cxxtest.wizards.ui.SWTUtil;

import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.ISourceRoot;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.internal.ui.wizards.SourceFolderSelectionDialog;
import org.eclipse.cdt.internal.ui.wizards.classwizard.NewClassWizardUtil;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

public class NewCxxTestSuiteWizardPageOne extends WizardPage
{
	private static final String PAGE_NAME = "NewCxxTestSuiteWizardPageOne";

	private static final String PAGE_TITLE = "CxxTest Suite";
	private static final String PAGE_DESCRIPTION =
		"Select the name of the new CxxTest suite. You have the options to specify the\nheader of the class under test and on the next page, the methods to be tested.";

	private IPath sourceFolder;
	private IPath headerUnderTest;
	private String suiteName;
	private String superClass;
	private boolean createSetUp;
	private boolean createTearDown;

	private Text sourceFolderField;
	private Text suiteNameField;
	private Text superClassField;
	private Text headerUnderTestField;
	private Button setUpButton;
	private Button tearDownButton;

	private NewCxxTestSuiteWizardPageTwo pageTwo;

	public NewCxxTestSuiteWizardPageOne(NewCxxTestSuiteWizardPageTwo pageTwo)
	{
		super(PAGE_NAME);
		this.pageTwo = pageTwo;

		sourceFolder = Path.EMPTY;
		headerUnderTest = Path.EMPTY;

		setTitle(PAGE_TITLE);
		setDescription(PAGE_DESCRIPTION);
	}

	public void init(IStructuredSelection selection)
	{
		ICElement element = getSelectedElement(selection);

		if(element != null)
		{
			ICProject cproject = element.getCProject();
			IPath projectPath = cproject.getProject().getFullPath();
			sourceFolder = projectPath.makeRelative();
			
			ICElement tuElement = element.getAncestor(ICElement.C_UNIT);
			if(tuElement != null)
			{
				ITranslationUnit unit = (ITranslationUnit)tuElement;
				headerUnderTest = unit.getResource().getFullPath().makeRelative();
			}
		}
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
		Composite composite = new Composite(parent, SWT.NONE);
		
		int numColumns = 4;
		
		GridLayout layout = new GridLayout();
		layout.numColumns = numColumns;
		composite.setLayout(layout);
		
		createContainerControls(composite, numColumns);
		createSeparator(composite, numColumns);
		createTypeNameControls(composite, numColumns);
		createSuperClassControls(composite, numColumns);
		createMethodStubSelectionControls(composite, numColumns);
		setSuperClass("CxxTest::TestSuite");
		createSeparator(composite, numColumns);
		createHeaderUnderTestControls(composite, numColumns);
		
		setControl(composite);
	}
	
	private void createSeparator(Composite composite, int numColumns)
	{
		Label sep = new Label(composite, SWT.SEPARATOR | SWT.HORIZONTAL);
		GridData gd = new GridData(SWT.FILL, SWT.NONE, true, false);
		gd.horizontalSpan = numColumns;
		sep.setLayoutData(gd);
	}
	
	private void createContainerControls(Composite composite, int numColumns)
	{
		Label label = new Label(composite, SWT.NONE);
		label.setText("Source Folder:");
		label.setLayoutData(gridDataForLabel(1));

		sourceFolderField = new Text(composite, SWT.SINGLE | SWT.BORDER);
		sourceFolderField.setLayoutData(gridDataForText(2));
		sourceFolderField.setText(sourceFolder.toString());
		sourceFolderField.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e)
			{
				checkForErrors();
			}			
		});

		Button browseButton = new Button(composite, SWT.NONE);
		browseButton.setText("Browse...");
		browseButton.setLayoutData(gridDataForButton(browseButton, 1));
		browseButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e)
			{
				selectSourceFolder();
			}
		});
	}
	
	private void selectSourceFolder()
	{
	    IPath oldFolderPath = sourceFolder;
		IPath newFolderPath = chooseSourceFolder(oldFolderPath);
		if (newFolderPath != null)
		{
			sourceFolder = newFolderPath.makeRelative();
			sourceFolderField.setText(sourceFolder.toString());
		}
	}

    private IPath chooseSourceFolder(IPath initialPath) {
        ICElement initElement = NewClassWizardUtil.getSourceFolder(initialPath);
        if (initElement instanceof ISourceRoot) {
            ICProject cProject = initElement.getCProject();
            ISourceRoot projRoot = cProject.findSourceRoot(cProject.getProject());
            if (projRoot != null && projRoot.equals(initElement))
                initElement = cProject;
        }
        
        SourceFolderSelectionDialog dialog = new SourceFolderSelectionDialog(getShell());
        dialog.setInput(CoreModel.create(NewClassWizardUtil.getWorkspaceRoot()));
        dialog.setInitialSelection(initElement);
        
        if (dialog.open() == Window.OK) {
            Object result = dialog.getFirstResult();
            if (result instanceof ICElement) {
                ICElement element = (ICElement)result;
                if (element instanceof ICProject) {
                    ICProject cproject = (ICProject)element;
                    ISourceRoot folder = cproject.findSourceRoot(cproject.getProject());
                    if (folder != null)
                        return folder.getResource().getFullPath();
                }
                return element.getResource().getFullPath();
            }
        }
        return null;
    }   

	private void selectHeaderUnderTest()
	{
	    IPath oldFolderPath = headerUnderTest;
		IPath newFolderPath = chooseHeaderUnderTest(oldFolderPath);
		if (newFolderPath != null)
		{
			headerUnderTest = newFolderPath.makeRelative();
			headerUnderTestField.setText(headerUnderTest.toString());
		}
	}

    private IPath chooseHeaderUnderTest(IPath initialPath)
    {
        ICElement initElement = NewClassWizardUtil.getSourceFolder(initialPath);
        if (initElement instanceof ISourceRoot) {
            ICProject cProject = initElement.getCProject();
            ISourceRoot projRoot = cProject.findSourceRoot(cProject.getProject());
            if (projRoot != null && projRoot.equals(initElement))
                initElement = cProject;
        }
        
        TranslationUnitSelectionDialog dialog = new TranslationUnitSelectionDialog(getShell());
        dialog.setInput(CoreModel.create(NewClassWizardUtil.getWorkspaceRoot()));
        dialog.setInitialSelection(initElement);
        
        if (dialog.open() == Window.OK) {
            Object result = dialog.getFirstResult();
            if (result instanceof ICElement) {
                ICElement element = (ICElement)result;
                if (element instanceof ICProject) {
                    ICProject cproject = (ICProject)element;
                    ISourceRoot folder = cproject.findSourceRoot(cproject.getProject());
                    if (folder != null)
                        return folder.getResource().getFullPath();
                }
                return element.getResource().getFullPath();
            }
        }

        return null;
    }  

    private void createTypeNameControls(Composite composite, int numColumns)
	{
		Label label = new Label(composite, SWT.NONE);
		label.setText("Name:");
		label.setLayoutData(gridDataForLabel(1));

		suiteNameField = new Text(composite, SWT.SINGLE | SWT.BORDER);
		suiteNameField.setLayoutData(gridDataForText(2));
		suiteNameField.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e)
			{
				checkForErrors();
			}			
		});

		label = new Label(composite, SWT.NONE);
		label.setLayoutData(gridDataForLabel(1));
	}

	private void createSuperClassControls(Composite composite, int numColumns)
	{
		Label label = new Label(composite, SWT.NONE);
		label.setText("Superclass:");
		label.setLayoutData(gridDataForLabel(1));

		superClassField = new Text(composite, SWT.SINGLE | SWT.BORDER);
		superClassField.setLayoutData(gridDataForText(2));
		superClassField.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e)
			{
				checkForErrors();
			}			
		});

		label = new Label(composite, SWT.NONE);
		label.setLayoutData(gridDataForLabel(1));
	}

	private void createMethodStubSelectionControls(Composite composite, int numColumns)
	{
		Label label = new Label(composite, SWT.NONE);
		label.setText("Which method stubs would you like to create?");
		label.setLayoutData(gridDataForLabel(4));

		label = new Label(composite, SWT.NONE);
		label.setLayoutData(gridDataForLabel(1));

		setUpButton = new Button(composite, SWT.CHECK);
		setUpButton.setText("setUp()");
		setUpButton.setLayoutData(gridDataForButton(setUpButton, 3));

		label = new Label(composite, SWT.NONE);
		label.setLayoutData(gridDataForLabel(1));

		tearDownButton = new Button(composite, SWT.CHECK);
		tearDownButton.setText("tearDown()");
		tearDownButton.setLayoutData(gridDataForButton(tearDownButton, 3));
	}

	private void createHeaderUnderTestControls(Composite composite, int numColumns)
	{
		Label label = new Label(composite, SWT.NONE);
		label.setText("Header under test:");
		label.setLayoutData(gridDataForLabel(1));

		headerUnderTestField = new Text(composite, SWT.SINGLE | SWT.BORDER);
		headerUnderTestField.setLayoutData(gridDataForText(2));
		headerUnderTestField.setText(headerUnderTest.toString());
		headerUnderTestField.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e)
			{
				checkForErrors();
			}			
		});

		Button browseButton = new Button(composite, SWT.NONE);
		browseButton.setText("Browse...");
		browseButton.setLayoutData(gridDataForButton(browseButton, 1));
		browseButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e)
			{
				selectHeaderUnderTest();
			}
		});
	}

	private void checkForErrors()
	{
		String msg = null;

		if(sourceFolderField.getText().length() == 0)
			msg = "Please enter the source folder in which the test suite will be created.";
		else if(suiteNameField.getText().length() == 0)
			msg = "Please enter a name for the test suite class.";
		else if(superClassField.getText().length() == 0)
			msg = "Please enter the name of the superclass of the test suite.";
		else if(headerUnderTestField.getText().length() == 0)
			msg = "Please enter the location of the header file containing the class to be tested.";
		
		setErrorMessage(msg);
		setPageComplete(msg == null);
	}

	private static GridData gridDataForLabel(int span)
	{
		GridData gd = new GridData();
		gd.horizontalAlignment = SWT.FILL;
		gd.horizontalSpan = span;
		return gd;
	}

	private static GridData gridDataForText(int span) {
		GridData gd = new GridData();
		gd.horizontalAlignment = SWT.FILL;
		gd.grabExcessHorizontalSpace = true;
		gd.horizontalSpan = span;
		return gd;
	}

	private static GridData gridDataForButton(Button button, int span)
	{
		GridData gd = new GridData();
		gd.horizontalAlignment = SWT.FILL;
		gd.grabExcessHorizontalSpace = false;
		gd.horizontalSpan = span;
		gd.widthHint = SWTUtil.getButtonWidthHint(button);
		return gd;
	}
	
	private void setSuperClass(String name)
	{
		superClass = name;
		superClassField.setText(name);
	}

	public void collectFields()
	{
		if(suiteNameField.getText().length() == 0)
			suiteName = null;
		else
			suiteName = suiteNameField.getText();

		if(superClassField.getText().length() == 0)
			superClass = null;
		else
			superClass = superClassField.getText();

		if(sourceFolderField.getText().length() == 0)
			sourceFolder = null;
		else
			sourceFolder = new Path(sourceFolderField.getText());
		
		if(headerUnderTestField.getText().length() == 0)
			headerUnderTest = null;
		else
			headerUnderTest = new Path(headerUnderTestField.getText());

		createSetUp = setUpButton.getSelection();
		createTearDown = tearDownButton.getSelection();
	}
	
	public IPath getSourceFolder()
	{
		return sourceFolder;
	}
	
	public IPath getHeaderUnderTest()
	{
		return headerUnderTest;
	}
	
	public String getSuiteName()
	{
		return suiteName;
	}
	
	public String getSuperClass()
	{
		return superClass;
	}

	public boolean getCreateSetUp()
	{
		return createSetUp;
	}
	
	public boolean getCreateTearDown()
	{
		return createTearDown;
	}

	public IWizardPage getNextPage()
	{
		collectFields();
		pageTwo.setHeaderUnderTestPath(headerUnderTest);

		return super.getNextPage();
	}
}
