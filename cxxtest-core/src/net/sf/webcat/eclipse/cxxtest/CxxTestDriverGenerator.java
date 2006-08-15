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

/*
 * Change log:
 * 
 * 1.1.2:  If a user-written main() was provided and signal handling was
 *         turned off, signal registration code in the initialization code
 *         would still be generated. 
 * 1.1.0:  Initial public release.
 */
package net.sf.webcat.eclipse.cxxtest;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.URL;

import net.sf.webcat.eclipse.cxxtest.framework.FrameworkPlugin;

import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.IMethodDeclaration;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;

/**
 * A helper class that encapsulates the generation of the CxxTest test runner
 * source file. This class mimics the functionality of the Perl and Python
 * scripts that are included with the standard CxxTest distribution.
 * 
 * This class currently supports all of the features of the CxxTest script
 * at the time of release when this was written, with the exception of:
 * 
 *  - user-specified header files to be included by the generated source
 *  - generating the source from a template file
 *
 * @author Tony Allowatt (Virginia Tech Computer Science)
 */
public class CxxTestDriverGenerator
{
	private ICProject project;
	
	private CxxTestSuiteInfo[] suites;
	
	private PrintWriter writer;
	
	private boolean trackHeap;
	
	private boolean trapSignals;
	
	private boolean traceStack;
	
	private String compiledExePath;

	private boolean noStaticInit;

	private boolean root;
	
	private boolean part;
	
	private boolean abortOnFail;

	private String longLongType;
	
	private boolean usesStandardLibrary;
	
	private boolean mainProvided;

	private String runner;
	
	private String memWatchFile;
	
	private boolean createBinaryLog;

	/**
	 * Instantiates an instance of the CxxTestDriverGenerator for the
	 * specified project and test suite collection.
	 * 
	 * @param project the ICProject associated with this generator
	 * @param path the path of the source file to be generated
	 * @param suites the collection of test suites to be generated
	 * 
	 * @throws IOException if an I/O error occurs during generation
	 */
	public CxxTestDriverGenerator(ICProject project, String path, CxxTestSuiteInfo[] suites) throws IOException
	{
		this.project = project;
		this.suites = suites;
		
		writer = new PrintWriter(new FileOutputStream(path));
		
		trackHeap = false;
		trapSignals = false;
		traceStack = false;
		compiledExePath = null;
		noStaticInit = true;
		
		root = true;
		part = false;

		abortOnFail = true;
		longLongType = null;
		
		usesStandardLibrary = true;
		mainProvided = false;
		
		runner = "XmlStdioPrinter";
		memWatchFile = ICxxTestConstants.MEMWATCH_RESULTS_FILE;

		createBinaryLog = CxxTestPlugin.getDefault().getConfigurationBoolean(
				CxxTestPlugin.CXXTEST_CPREF_LOG_EXECUTION);
	}
	
	public ICProject getProject()
	{
		return project;
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

	public String getCompiledExePath()
	{
		return compiledExePath;
	}
	
	public void setCompiledExePath(String value)
	{
		compiledExePath = value;
	}

	public boolean isNoStaticInit()
	{
		return noStaticInit;
	}

	public void setNoStaticInit(boolean value)
	{
		noStaticInit = value;
	}

	public boolean isAbortOnFail()
	{
		return abortOnFail;
	}

	public void setAbortOnFail(boolean value)
	{
		abortOnFail = value;
	}

	public boolean isUsingStandardLibrary()
	{
		return usesStandardLibrary;
	}

	public void setUsingStandardLibrary(boolean value)
	{
		usesStandardLibrary = value;
	}

	public boolean isMainProvided()
	{
		return mainProvided;
	}

	public void setMainProvided(boolean value)
	{
		mainProvided = value;
	}

	public String getLongLongType()
	{
		return longLongType;
	}

	public void setLongLongType(String value)
	{
		longLongType = value;
	}

	public String getMemWatchFile()
	{
		return memWatchFile;
	}
	
	public void setMemWatchFile(String value)
	{
		memWatchFile = value;
	}

	public void buildDriver() throws IOException
	{
		writer.println("/* Generated file, do not edit */");
		writer.println();

		writePreamble();
		writeWorld();
		writeMain();

		writer.close();
	}
	
	private void writePreamble() throws IOException
	{
		writer.println("#ifndef CXXTEST_RUNNING");
		writer.println("#define CXXTEST_RUNNING");
		writer.println("#endif");
		writer.println();

		if(usesStandardLibrary)
			writer.println("#define _CXXTEST_HAVE_STD");

//	    if haveExceptionHandling:
			writer.println("#define _CXXTEST_HAVE_EH");
			
		if(trapSignals)
			writer.println("#define CXXTEST_TRAP_SIGNALS");
		if(abortOnFail)
			writer.println("#define _CXXTEST_ABORT_TEST_ON_FAIL");
		if(longLongType != null)
			writer.println("#define _CXXTEST_LONGLONG " + longLongType);

		if(traceStack && compiledExePath != null)
			writer.println("#define CXXTEST_STACK_TRACE_EXE \"" +
					compiledExePath + "\"");

		writer.println();

		//for header in headers:
        //output.write( "#include %s\n" % header )

		writer.println("#include <cxxtest/TestListener.h>");
		writer.println("#include <cxxtest/TestTracker.h>");
		writer.println("#include <cxxtest/TestRunner.h>");
		writer.println("#include <cxxtest/RealDescriptions.h>");

		if(createBinaryLog)
		{
			writer.println("#define CXXTEST_CREATE_BINARY_LOG");
			writer.println("#include <cxxtest/ExecutionLog.h>");
			writer.println("ExecutionLog executionLog;");
			writer.println();
		}

		writer.println("#include <cxxtest/" + runner + ".h>");
	    writer.println();

		writer.println("#ifdef CXXTEST_TRACE_STACK");
		writer.println("#include <symreader.h>");
		writer.println("#endif");
	    
	    writer.println("#include <chkptr.h>");

	    writer.println("typedef const CxxTest::SuiteDescription *SuiteDescriptionPtr;");
	    writer.println("typedef const CxxTest::TestDescription *TestDescriptionPtr;");
	    writer.println();
	}

	private void writeMain() throws IOException
	{
		if(root || !part)
			writeRoot();

		if(trapSignals)
			writeSignalHandler();

		writeCheckedPointerHandler();
		writeCheckedPointerReporter();

		if(!mainProvided)
			writeMainRunner();
		else
			writeStaticObjectRunner();
	}

	private void writeTestRunStatement(boolean inCtor) throws IOException
	{
		writer.println("#ifdef CXXTEST_TRACE_STACK");
		writer.println(" symreader_initialize(CXXTEST_STACK_TRACE_EXE, SYMFLAGS_DEMANGLE);");
		writer.println("#endif");
		
		writer.println(" ChkPtr::__manager.setReportAtEnd(true);");
		writer.println(" ChkPtr::__manager.setErrorHandler(" +
				"&CxxTest::__cxxtest_chkptr_error_handler);");
		writer.println(" ChkPtr::__manager.setReporter(" +
				"new CxxTest::xml_chkptr_reporter(\"" +
				memWatchFile + "\"), true);");

		if("XmlStdioPrinter".equals(runner))
		{
			writer.println(" FILE* resultsFile = fopen(\"" +
					ICxxTestConstants.TEST_RESULTS_FILE + "\", \"w\");");
			
			if(!inCtor)
				writer.print(" int exitCode =");
			
			writer.println(" CxxTest::" + runner + "(resultsFile).run();");
			writer.println(" fclose(resultsFile);");

			if(createBinaryLog)
			{
				writer.println();
				writer.println("#ifdef CXXTEST_CREATE_BINARY_LOG");
				writer.println(" executionLog.appendToFile(\"" +
						ICxxTestConstants.BINARY_LOG_FILE + "\");");
				writer.println("#endif");
			}
			
			if(!inCtor)
				writer.println(" return exitCode;");
		}
		else
		{
			if(!inCtor)
				writer.println(" return CxxTest::" + runner + "().run();");
			else
				writer.println(" CxxTest::" + runner + "().run();");				
		}
	}

	private void writeMainRunner() throws IOException
	{
		writer.println("int main() {");
		
		if(trapSignals)
			writeSignalRegistration();
		
		if(noStaticInit)
			writer.println(" CxxTest::initialize();");

		writeTestRunStatement(false);

		writer.println("}");		
	}

	private void writeStaticObjectRunner() throws IOException
	{
		writer.println("class CxxTestMain {");
		writer.println("public:");
		writer.println("    CxxTestMain() {");

		if(trapSignals)
			writeSignalRegistration();

		if(noStaticInit)
			writer.println("        CxxTest::initialize();");
		
		writeTestRunStatement(true);

		writer.println("    }");
		writer.println("};");
		writer.println("CxxTestMain cxxTestMain __attribute__((init_priority(65535)));;");
		writer.println();
	}

	private void writeFromFrameworkStream(String path) throws IOException
	{
		URL entry = FileLocator.find(
				FrameworkPlugin.getDefault().getBundle(),
				new Path(path), null);
		URL url = FileLocator.resolve(entry);

		InputStream stream = url.openStream();
		writeFromStream(stream);
		stream.close();
	}

	private void writeSignalHandler() throws IOException
	{
		writeFromFrameworkStream("/fragments/signalHandler.c");
	}

	private void writeSignalRegistration() throws IOException
	{
		writeFromFrameworkStream("/fragments/signalRegistration.c");
	}

	private void writeCheckedPointerHandler() throws IOException
	{
		writeFromFrameworkStream("/fragments/chkptrErrorHandler.c");
	}

	private void writeCheckedPointerReporter() throws IOException
	{
		writeFromFrameworkStream("/fragments/chkptrReporter.cpp");
	}

	private void writeFromStream(InputStream stream) throws IOException
	{
		int ch = stream.read();
		while(ch != -1)
		{
			writer.print((char)ch);
			ch = stream.read();
		}
	}

	private void writeWorld() throws IOException
	{
		writeSuites();
		
		if(noStaticInit)
			writeInitialize();
	}

	private void writeRoot() throws IOException
	{
		if(trackHeap)
		{
			writer.println("#define CHKPTR_BASIC_HEAP_CHECK");
			writer.println();
		}

		writer.println("#include <cxxtest/Root.cpp>");
		
		if(trackHeap)
		{
			if(traceStack)
			{
				writer.println("#define MW_STACK_TRACE_INITIAL_PREFIX CXXTEST_STACK_TRACE_INITIAL_PREFIX");
				writer.println("#define MW_STACK_TRACE_OTHER_PREFIX CXXTEST_STACK_TRACE_INITIAL_PREFIX");
			}
//			writer.println("#define MW_XML_OUTPUT_FILE \"" + memWatchFile + "\"\n");
//			writer.println("#include <cxxtest/Memwatch.cpp>");
		}
	}

	private void writeInitialize() throws IOException
	{
		writer.println("namespace CxxTest {");
		writer.println(" void initialize()");
		writer.println(" {");

		for(int i = 0; i < suites.length; i++)
		{
			CxxTestSuiteInfo suite = suites[i];
			
			writer.println("  " + suite.getTestListName() + ".initialize();");
			
			if(suite.isDynamic())
			{
				writer.println("  " + suite.getObjectName() + " = 0;");
				
				writer.print("  " + suite.getDescriptionObjectName() + ".initialize( ");
				writer.print(makeCString(suite.getPath()) + ", ");
				writer.print(suite.getLineNumber());
				writer.print(", \"" + suite.getName() + "\", ");
				writer.print(suite.getTestListName() + ", ");
				writer.print(suite.getObjectName() + ", ");
				writer.print(suite.getCreateLineNumber());
				writer.print(", " + suite.getDestroyLineNumber());
				writer.println(" );");
			}
			else
			{
				writer.print("  " + suite.getDescriptionObjectName() + ".initialize( ");
				writer.print(makeCString(suite.getPath()) + ", ");
				writer.print(suite.getLineNumber());
				writer.print(", \"" + suite.getName() + "\", ");
				writer.print(suite.getObjectName() + ", ");
				writer.print(suite.getTestListName());
				writer.println(" );");
			}
			
			IMethodDeclaration[] tests = suite.getTestMethods();
			for(int j = 0; j < tests.length; j++)
			{
				IMethodDeclaration test = tests[j];
				
				writer.print("  testDescription_" + suite.getName() + "_" + test.getElementName());
				writer.print(".initialize( ");
				writer.print(suite.getTestListName() + ", ");
				writer.print(suite.getDescriptionObjectName() + ", ");
				writer.print(getLineNumber(test));
				writer.println(", \"" + test.getElementName() + "\" );");
			}
		}
		
		writer.println(" }");
		writer.println("}");
	}

	private int getLineNumber(IMethodDeclaration method)
	{
		try
		{
			return method.getSourceRange().getStartLine();
		}
		catch (CModelException e)
		{
			return 1;
		}
	}

	private void writeSuites() throws IOException
	{
		for(int i = 0; i < suites.length; i++)
		{
			CxxTestSuiteInfo suite = suites[i];
			
			writeInclude(suite);
			
			if(suite.isDynamic())
				writeSuitePointer(suite);
			else
				writeSuiteObject(suite);
			
			writeTestList(suite);
			writeSuiteDescription(suite);
			writeTestDescriptions(suite);
		}
	}
	
	private void writeInclude(CxxTestSuiteInfo suite) throws IOException
	{
		writer.println("#include \"" + suite.getPath() + "\"");
	}
	
	private void writeSuitePointer(CxxTestSuiteInfo suite) throws IOException
	{
		if(noStaticInit)
		{
			writer.print("static " + suite.getName() + " *");
			writer.println(suite.getObjectName() + ";");
		}
		else
		{
			writer.print("static " + suite.getName() + " *");
			writer.println(suite.getObjectName() + " = 0;");
		}

		writer.println();
	}

	private void writeSuiteObject(CxxTestSuiteInfo suite) throws IOException
	{
		writer.print("static " + suite.getName() + " ");
		writer.println(suite.getObjectName() + ";");
		writer.println();
	}

	private void writeTestList(CxxTestSuiteInfo suite) throws IOException
	{
		if(noStaticInit)
		{
			writer.println("static CxxTest::List " + suite.getTestListName() + ";");
		}
		else
		{
			writer.println("static CxxTest::List " + suite.getTestListName() + " = { 0, 0 };");
		}
	}
	
	private void writeSuiteDescription(CxxTestSuiteInfo suite) throws IOException
	{
		if(suite.isDynamic())
		{
			writeDynamicDescription(suite);
		}
		else
		{
			writeStaticDescription(suite);
		}
	}
	
	private String makeCString(String str)
	{
		String res = "";
		for(int i = 0; i < str.length(); i++)
		{
			char ch = str.charAt(i);
			
			if(ch == '\\')
				res += "\\\\";
			else
				res += ch;
		}
		
		return "\"" + res + "\"";
	}

	private void writeDynamicDescription(CxxTestSuiteInfo suite) throws IOException
	{
		writer.print("CxxTest::DynamicSuiteDescription<" + suite.getName() + "> ");
		writer.print(suite.getDescriptionObjectName());
		
		if(!noStaticInit)
		{
			writer.print("( " + makeCString(suite.getPath()) + ", ");
			writer.print(suite.getLineNumber());
			writer.print(", \"" + suite.getName() + "\", ");
			writer.print(suite.getTestListName() + ", ");
			writer.print(suite.getObjectName() + ", ");
			writer.print(suite.getCreateLineNumber());
			writer.print(", ");
			writer.print(suite.getDestroyLineNumber());
			writer.print(" )");
		}
		
		writer.println(";");
		writer.println();
	}
	
	private void writeStaticDescription(CxxTestSuiteInfo suite) throws IOException
	{
		writer.print("CxxTest::StaticSuiteDescription ");
		writer.print(suite.getDescriptionObjectName());
		
		if(!noStaticInit)
		{
			writer.print("( " + makeCString(suite.getPath()) + ", ");
			writer.print(suite.getLineNumber());
			writer.print(", \"" + suite.getName() + "\", ");
			writer.print(suite.getObjectName() + ", ");
			writer.print(suite.getTestListName());
			writer.print(" )");
		}
		
		writer.println(";");
		writer.println();
	}
	
	private void writeTestDescriptions(CxxTestSuiteInfo suite) throws IOException
	{
		IMethodDeclaration[] tests = suite.getTestMethods();

		for(int i = 0; i < tests.length; i++)
		{
			IMethodDeclaration test = tests[i];
			writeTestDescription(suite, test);
		}
	}
	
	private void writeTestDescription(CxxTestSuiteInfo suite, IMethodDeclaration test) throws IOException
	{
		String testClass = "TestDescription_" + suite.getName() + "_" + test.getElementName();
		
		writer.println("static class " + testClass + " : public CxxTest::RealTestDescription {");
		writer.println("public:");

		if(!noStaticInit)
		{
			writer.print(" " + testClass + "() : CxxTest::RealTestDescription( ");
			writer.print(suite.getTestListName() + ", ");
			writer.print(suite.getDescriptionObjectName() + ", ");
			writer.print(getLineNumber(test));
			writer.println(", \"" + test.getElementName() + "\" ) {}");
		}
		
		writer.print(" void runTest() { ");

		if(suite.isDynamic())
			writeDynamicRun(suite, test);
		else
			writeStaticRun(suite, test);
		
		writer.println(" }");
		
		writer.println("} testDescription_" + suite.getName() + "_" + test.getElementName() + ";");
		writer.println();
	}
	
	private void writeDynamicRun(CxxTestSuiteInfo suite, IMethodDeclaration test) throws IOException
	{
		writer.print("if (" + suite.getObjectName() + ") ");
		writer.print(suite.getObjectName() + "->" + test.getElementName() + "();");
	}

	private void writeStaticRun(CxxTestSuiteInfo suite, IMethodDeclaration test) throws IOException
	{
		writer.print(suite.getObjectName() + "." + test.getElementName() + "();");
	}
}
