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
package net.sf.webcat.eclipse.cxxtest.internal.generator;

import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Map;

import net.sf.webcat.eclipse.cxxtest.CxxTestPlugin;
import net.sf.webcat.eclipse.cxxtest.ICxxTestConstants;

import org.antlr.stringtemplate.AutoIndentWriter;
import org.antlr.stringtemplate.StringTemplate;
import org.antlr.stringtemplate.StringTemplateGroup;
import org.antlr.stringtemplate.language.AngleBracketTemplateLexer;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;

public class TestRunnerGenerator
{
    /**
     * Instantiates an instance of the CxxTestDriverGenerator for the specified
     * project and test suite collection.
     * 
     * @param project
     *            the ICProject associated with this generator
     * @param path
     *            the path of the source file to be generated
     * @param suites
     *            the collection of test suites to be generated
     * 
     * @throws IOException
     *             if an I/O error occurs during generation
     */
    public TestRunnerGenerator(ICProject project, String path,
    		TestSuiteCollection suites, Map<String, Boolean> testsToRun)
    throws IOException
    {
        this.suites = suites;

        // Create a proxy object to manage the tests to run. Any tests
        // not in this map are assumed to be true (so that if tests
        // have been added, but not refreshed in the tool window, they
        // will be run until they are explicitly disabled).

        this.testsToRunProxy = new TestsToRunProxy();

        // Load the template from the embedded assembly resources.

        InputStream stream = FileLocator.openStream(
        		CxxTestPlugin.getDefault().getBundle(),
        		new Path(RunnerTemplateResourcePath), true);

        StringTemplateGroup templateGroup = new StringTemplateGroup(
                new InputStreamReader(stream), AngleBracketTemplateLexer.class);

        templateGroup.registerRenderer(String.class,
                new TestRunnerStringRenderer(path));

        template = templateGroup.getInstanceOf("runAllTestsFile");

        // Initialize the options that will be passed into the template.

        options = new Hashtable<String, Object>();
        options.put("platformIsMSVC", false);
        options.put("trapSignals", true);
        options.put("traceStack", true);
        options.put("noStaticInit", true);
        options.put("root", true);
        options.put("part", false);
        options.put("abortOnFail", true);
        options.put("mainProvided", suites.doesMainFunctionExist());
        options.put("testResultsFilename", ICxxTestConstants.TEST_RESULTS_FILE);
        options.put("testsToRun", testsToRunProxy);

        ArrayList<String> listeners = new ArrayList<String>();
        listeners.add("XmlStdioPrinter");
        options.put("listeners", listeners);
        
        try
        {
            writer = new FileWriter(path);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }


	public boolean isTrackingHeap()
	{
		return trackHeap;
	}

	public void setTrackHeap(boolean value)
	{
		trackHeap = value;
	}

	public boolean isTrappingSignals()
	{
		return trapSignals;
	}

	public void setTrapSignals(boolean value)
	{
		trapSignals = value;
	}

	public boolean isTracingStack()
	{
		return traceStack;
	}
	
	public void setTraceStack(boolean value)
	{
		traceStack = value;
	}

	public String[] getExtraIncludes()
	{
		return extraIncludes;
	}

	public void setExtraIncludes(String[] files)
	{
		extraIncludes = files;
	}
	
	public String[] getPossibleTestFiles()
	{
		return possibleTestFiles;
	}

	public void setPossibleTestFiles(String[] files)
	{
		possibleTestFiles = files;
	}
	
	public void generate()
    {
        template.setAttribute("options", options);
        template.setAttribute("suites", suites.getSuites());

        if (possibleTestFiles != null && possibleTestFiles.length > 0)
        {
        	template.setAttribute("possibleTestFiles", possibleTestFiles);
        }

        if (extraIncludes != null && extraIncludes.length > 0)
        {
        	options.put("extraIncludes", extraIncludes);
        }

        try
        {
            template.write(new AutoIndentWriter(writer));
            writer.close();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }


    private class TestsToRunProxy extends Hashtable<String, Boolean>
    {
        private static final long serialVersionUID = 1L;

        public boolean containsKey(Object key)
        {
            return true;
        }
        
        public Boolean get(Object key)
        {
            return true;
        }
    }


    private static final String RunnerTemplateResourcePath = "/generator-templates/runner.stg";

    private TestSuiteCollection suites;
    private TestsToRunProxy testsToRunProxy;
    private Hashtable<String, Object> options;
    private StringTemplate template;
    private Writer writer;
    
	private boolean trackHeap;
	private boolean trapSignals;
	private boolean traceStack;

	private String[] possibleTestFiles;
	private String[] extraIncludes;
}
