package net.sf.webcat.eclipse.cxxtest.wizards;

import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.IMethodDeclaration;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.core.model.IWorkingCopy;
import org.eclipse.cdt.internal.ui.wizards.classwizard.NewClassWizardMessages;
import org.eclipse.cdt.internal.ui.wizards.filewizard.NewSourceFileGenerator;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;

public class CxxTestSuiteGenerator
{
	private String suiteName;
	private IPath suitePath;
	private IPath headerUnderTestPath;
	private String superClass;
	private boolean createSetUp;
	private boolean createTearDown;
	private IMethodDeclaration[] methodStubs;

	private IFile suiteFile;
	private ITranslationUnit createdSuite;

	public CxxTestSuiteGenerator(String suiteName, IPath suitePath,
			IPath headerUnderTestPath, String superClass,
			boolean createSetUp, boolean createTearDown,
			IMethodDeclaration[] methodStubs)
	{
		this.suiteName = suiteName;
		this.suitePath = suitePath;
		this.headerUnderTestPath = headerUnderTestPath;
		this.superClass = superClass;
		this.createSetUp = createSetUp;
		this.createTearDown = createTearDown;
		this.methodStubs = methodStubs;
	}
	
	public void generate(IProgressMonitor monitor) throws CoreException, InterruptedException
	{
        suiteFile = NewSourceFileGenerator.createHeaderFile(
        		suitePath, true, new SubProgressMonitor(monitor, 50));
        
        if(suiteFile != null)
            createdSuite = (ITranslationUnit) CoreModel.getDefault().create(suiteFile);

        // create a working copy with a new owner
        IWorkingCopy suiteWorkingCopy = createdSuite.getWorkingCopy();

        String suiteContent = constructHeaderFileContent(createdSuite,
        		"", //suiteWorkingCopy.getBuffer().getContents(),
        		new SubProgressMonitor(monitor, 100));
        suiteWorkingCopy.getBuffer().setContents(suiteContent);

        if (monitor.isCanceled()) {
            throw new InterruptedException();
        }

        suiteWorkingCopy.reconcile();
        suiteWorkingCopy.commit(true, monitor);
        monitor.worked(50);
	}
	
	public IFile getCreatedSuiteFile()
	{
		return suiteFile;
	}

    private String constructHeaderFileContent(ITranslationUnit headerTU, String oldContents, IProgressMonitor monitor)
    {
        monitor.beginTask(NewClassWizardMessages.getString("NewClassCodeGeneration.createType.task.header"), 100); //$NON-NLS-1$
        
        if (oldContents != null && oldContents.length() == 0)
            oldContents = null;
        
        StringBuffer text = new StringBuffer();
        
        appendFilePrologue(text);
        appendClassPrologue(text);
        
        if(createSetUp)
        	appendSetUp(text);
        if(createTearDown)
        	appendTearDown(text);

        if(methodStubs != null)
        	appendMethodStubs(text);

        appendClassEpilogue(text);
        appendFileEpilogue(text);

        String newContents = text.toString();
        monitor.done();
        return newContents;
    }
    
    private void appendFilePrologue(StringBuffer text)
    {
    	String guardSymbol = suiteName.toUpperCase() + "_H";
    	text.append("#ifndef " + guardSymbol + "\n");
    	text.append("#define " + guardSymbol + "\n\n");
    	
    	text.append("#include <cxxtest/TestSuite.h>\n\n");
    	
    	if(headerUnderTestPath != null)
    	{
    		IPath headerAbsPath = headerUnderTestPath.makeAbsolute();
    		IPath suiteAbsPath = suitePath.makeAbsolute();
    		int matchingSegs = headerAbsPath.matchingFirstSegments(suiteAbsPath);
    		IPath includePath = headerAbsPath.removeFirstSegments(matchingSegs);

    		text.append("#include \"" + includePath.toOSString() + "\"\n\n");
    	}
    }

    private void appendClassPrologue(StringBuffer text)
    {
    	text.append("class " + suiteName + " : public " + superClass + "\n");
    	text.append("{\n");
    	text.append("public:\n");
    }

    private void appendSetUp(StringBuffer text)
    {
    	text.append("\tvoid setUp()\n");
    	text.append("\t{\n");
    	text.append("\t\t// TODO: Implement setUp() function.\n");
    	text.append("\t}\n\n");
    }

    private void appendTearDown(StringBuffer text)
    {
    	text.append("\tvoid tearDown()\n");
    	text.append("\t{\n");
    	text.append("\t\t// TODO: Implement tearDown() function.\n");
    	text.append("\t}\n\n");
    }

    private void appendMethodStubs(StringBuffer text)
    {
    	for(int i = 0; i < methodStubs.length; i++)
    	{
    		IMethodDeclaration method = methodStubs[i];
    		
    		String name = method.getElementName();
    		name = "test" + Character.toUpperCase(name.charAt(0)) +
    			name.substring(1);
    		
        	text.append("\tvoid " + name + "()\n");
        	text.append("\t{\n");
        	text.append("\t\t// TODO: Implement " + name + "() function.\n");
        	text.append("\t}\n\n");    		
    	}
    }

    private void appendClassEpilogue(StringBuffer text)
    {
    	text.append("};\n\n");
    }

    private void appendFileEpilogue(StringBuffer text)
    {
    	String guardSymbol = suiteName.toUpperCase() + "_H";
    	text.append("#endif // " + guardSymbol + "\n");
    }
}
