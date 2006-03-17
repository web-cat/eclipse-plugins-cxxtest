package net.sf.webcat.eclipse.cxxtest;

import java.util.Vector;

import net.sf.webcat.eclipse.cxxtest.internal.model.CxxTestAssertionFactory;
import net.sf.webcat.eclipse.cxxtest.internal.model.CxxTestMethod;
import net.sf.webcat.eclipse.cxxtest.internal.model.CxxTestStackFrame;
import net.sf.webcat.eclipse.cxxtest.internal.model.CxxTestSuite;
import net.sf.webcat.eclipse.cxxtest.internal.model.StackTraceAssertion;
import net.sf.webcat.eclipse.cxxtest.model.ICxxTestAssertion;
import net.sf.webcat.eclipse.cxxtest.model.ICxxTestSuite;

import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

/**
 * This class is a very simplistic SAX content handler that parses the XML
 * results file generated by our CxxTest XML printer and builds a tree of
 * test suite and test case objects from it.
 */
public class CxxTestResultsHandler extends DefaultHandler
{
	private Vector suites = new Vector();

	private boolean insideTest = false;

	private boolean insideTestChild = false; 

	private StringBuffer textContents;

	private CxxTestMethod lastTest = null;
	
	private ICxxTestAssertion lastTestChild = null;

	public void startElement(String uri, String localName, String qName,
			Attributes attributes)
	{
		if(localName.equals("suite"))
			startSuite(attributes);
		else if(localName.equals("test"))
			startTest(attributes);
		else if(insideTest && !insideTestChild)
			startTestChild(localName, attributes);
		else if(insideTest && insideTestChild)
			startStackFrame(attributes);
	}

	public void endElement(String uri, String localName, String qName)
	{
		if(localName.equals("test"))
			endTest();
		else if(!localName.equals("stack-frame") && insideTestChild)
			endTestChild();
	}

	private void startSuite(Attributes attributes)
	{
		CxxTestSuite suite = new CxxTestSuite(attributes);
		suites.add(suite);
	}
	
	private void startTest(Attributes attributes)
	{
		CxxTestSuite suite = (CxxTestSuite)suites.lastElement();
		lastTest = new CxxTestMethod(suite, attributes);
		
		insideTest = true;
	}
	
	private void endTest()
	{
		insideTest = false;
		lastTest = null;
	}
	
	private void startTestChild(String name, Attributes attributes)
	{
		textContents = new StringBuffer();

		lastTestChild = CxxTestAssertionFactory.create(lastTest, name, attributes);
		insideTestChild = true;
	}

	public void characters(char[] chars, int start, int length)
	{
		if(insideTestChild)
			textContents.append(chars, start, length);
	}

	private void startStackFrame(Attributes attributes)
	{
		if(lastTestChild instanceof StackTraceAssertion)
		{
			StackTraceAssertion sta =
				(StackTraceAssertion)lastTestChild;
			
			String function = attributes.getValue("function");
			String file = null;
			int lineNumber = 0;
			
			String fileLine = attributes.getValue("location");
			if(fileLine != null)
			{
				String[] parts = fileLine.split(":");
				file = parts[0];
				
				if(parts.length > 1)
					lineNumber = Integer.parseInt(parts[1]);
			}
			
			sta.addStackFrame(new CxxTestStackFrame(function, file, lineNumber));
		}
	}

	private void endTestChild()
	{
		if(lastTestChild instanceof StackTraceAssertion)
		{
			StackTraceAssertion sta =
				(StackTraceAssertion)lastTestChild;
			
			sta.setMessage(textContents.toString());
		}

		insideTestChild = false;
		lastTestChild = null;
	}

	public ICxxTestSuite[] getSuites()
	{
		return (ICxxTestSuite[])suites.toArray(
				new ICxxTestSuite[suites.size()]);
	}
}