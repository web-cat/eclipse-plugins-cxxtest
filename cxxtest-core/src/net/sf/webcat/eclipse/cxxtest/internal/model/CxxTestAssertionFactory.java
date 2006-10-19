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
package net.sf.webcat.eclipse.cxxtest.internal.model;

import java.text.MessageFormat;

import net.sf.webcat.eclipse.cxxtest.model.ICxxTestAssertion;
import net.sf.webcat.eclipse.cxxtest.model.ICxxTestBase;
import net.sf.webcat.eclipse.cxxtest.model.ICxxTestStackFrame;

import org.xml.sax.Attributes;

/**
 * Creates an appropriate object of type ICxxTestAssertion based on the tag
 * type and attributes from the XML results file.
 * 
 * @author Tony Allowatt (Virginia Tech Computer Science)
 */
public class CxxTestAssertionFactory
{
	private static class Assertion implements ICxxTestAssertion
	{
		private CxxTestMethod parent;
		
		private int status;
		
		private String message;
		
		private String[] args;
		
		private int lineNumber;

		public Assertion(CxxTestMethod parent, int lineNumber, int status, String message, String[] args)
		{
			this.parent = parent;
			this.status = status;
			this.message = message;
			this.args = args;
			this.lineNumber = lineNumber;

			parent.addAssertion(this);
		}

		public String getMessage(boolean includeLine)
		{
			String[] realArgs = (String[])args.clone();

			if(includeLine)
				realArgs[0] = " (line " + realArgs[0] + ")";
			else
				realArgs[0] = "";

			return MessageFormat.format(message, realArgs);
		}

		public ICxxTestBase getParent()
		{
			return parent;
		}

		public int getLineNumber()
		{
			return lineNumber;
		}

		public int getStatus()
		{
			return status;
		}
		
		public ICxxTestStackFrame[] getStackTrace()
		{
			return null;
		}
	}

	private static final String MSG_TRACE = "Trace{0}: {1}";

	private static final String MSG_FAILED_ASSERT =
		"Failed assertion{0}: expected {1} == true, but found false";

	private static final String MSG_FAILED_ASSERT_EQ =
		"Failed assertion{0}: expected {1} == {2}, but found {3} != {4}";

	private static final String MSG_FAILED_ASSERT_SAME_DATA =
		"Failed assertion{0}: expected {5} ({6}) bytes equal at {1} and {2}, but found {3} differs from {4}";

	private static final String MSG_FAILED_ASSERT_DELTA =
		"Failed assertion{0}: expected {1} == {2} to within {5} ({6}), but found {3} != {4}";

	private static final String MSG_FAILED_ASSERT_NE =
		"Failed assertion{0}: expected {1} != {2}, but found both equal {3}";

	private static final String MSG_FAILED_ASSERT_LT =
		"Failed assertion{0}: expected {1} < {2}, but found {3} >= {4}";

	private static final String MSG_FAILED_ASSERT_LE = 
		"Failed assertion{0}: expected {1} <= {2}, but found {3} > {4}";

	private static final String MSG_FAILED_ASSERT_RELATION =
		"Failed assertion{0}: expected {5}({1}, {2}) == true, but found {5}({3}, {4}) == false";

	private static final String MSG_FAILED_ASSERT_PREDICATE =
		"Failed assertion{0}: expected {3}({1}) == true, but found {3}({2}) == false";

	private static final String MSG_FAILED_ASSERT_THROWS = 
		"Failed assertion{0}: expected \"{1}\" to throw \"{2}\", but {3}";

	private static final String MSG_FAILED_ASSERT_NOTHROW =
		"Failed assertion{0}: expected \"{1}\" not to throw an exception, but it did";

	static public ICxxTestAssertion create(CxxTestMethod parent, String type, Attributes attributes)
	{
		if("trace".equals(type))
			return createTrace(parent, attributes);
		else if("warning".equals(type))
			return createWarning(parent, attributes);
		else if("failed-test".equals(type))
			return createFailedTest(parent, attributes);
		else if("failed-assert".equals(type))
			return createFailedAssert(parent, attributes);
		else if("failed-assert-eq".equals(type))
			return createFailedAssertEq(parent, attributes);
		else if("failed-assert-same-data".equals(type))
			return createFailedAssertSameData(parent, attributes);
		else if("failed-assert-delta".equals(type))
			return createFailedAssertDelta(parent, attributes);
		else if("failed-assert-ne".equals(type))
			return createFailedAssertNe(parent, attributes);
		else if("failed-assert-lt".equals(type))
			return createFailedAssertLt(parent, attributes);
		else if("failed-assert-le".equals(type))
			return createFailedAssertLe(parent, attributes);
		else if("failed-assert-relation".equals(type))
			return createFailedAssertRelation(parent, attributes);
		else if("failed-assert-predicate".equals(type))
			return createFailedAssertPredicate(parent, attributes);
		else if("failed-assert-throws".equals(type))
			return createFailedAssertThrows(parent, attributes);
		else if("failed-assert-nothrow".equals(type))
			return createFailedAssertNoThrow(parent, attributes);
		else
			return null;
	}

	private static String[] getAttributeValues(Attributes attributes, String[] attrNames)
	{
		String[] values = new String[attrNames.length];
		
		for(int i = 0; i < attrNames.length; i++)
			values[i] = attributes.getValue(attrNames[i]);
		
		return values;
	}

	private static int getLineNumber(Attributes attributes)
	{
		String value = attributes.getValue("line");
		return Integer.parseInt(value);
	}

	private static ICxxTestAssertion createTrace(CxxTestMethod parent, Attributes node)
	{
		String[] values = getAttributeValues(node, new String[] { "line", "message" });
		int line = getLineNumber(node);
		return new Assertion(parent, line, ICxxTestBase.STATUS_OK, MSG_TRACE, values);
	}

	private static ICxxTestAssertion createWarning(CxxTestMethod parent, Attributes node)
	{
		int line = getLineNumber(node);
		return new StackTraceAssertion(parent, line, ICxxTestBase.STATUS_WARNING);
	}

	private static ICxxTestAssertion createFailedTest(CxxTestMethod parent, Attributes node)
	{
		int line = getLineNumber(node);
		return new StackTraceAssertion(parent, line, ICxxTestBase.STATUS_ERROR);
	}

	private static ICxxTestAssertion createFailedAssert(CxxTestMethod parent, Attributes node)
	{
		String[] values = getAttributeValues(node, new String[] { "line", "expression" });
		int line = getLineNumber(node);
		return new Assertion(parent, line, ICxxTestBase.STATUS_FAILED, MSG_FAILED_ASSERT, values);
	}

	private static ICxxTestAssertion createFailedAssertEq(CxxTestMethod parent, Attributes node)
	{
		String[] values = getAttributeValues(node, new String[] {
				"line", "lhs-desc", "rhs-desc", "lhs-value", "rhs-value" });
		int line = getLineNumber(node);
		return new Assertion(parent, line, ICxxTestBase.STATUS_FAILED, MSG_FAILED_ASSERT_EQ, values);
	}

	private static ICxxTestAssertion createFailedAssertSameData(CxxTestMethod parent, Attributes node)
	{
		String[] values = getAttributeValues(node, new String[] {
				"line", "lhs-desc", "rhs-desc", "lhs-value", "rhs-value", "size-desc", "size-value" });
		int line = getLineNumber(node);
		return new Assertion(parent, line, ICxxTestBase.STATUS_FAILED, MSG_FAILED_ASSERT_SAME_DATA, values);
	}

	private static ICxxTestAssertion createFailedAssertDelta(CxxTestMethod parent, Attributes node)
	{
		String[] values = getAttributeValues(node, new String[] {
				"line", "lhs-desc", "rhs-desc", "lhs-value", "rhs-value", "delta-desc", "delta-value" });
		int line = getLineNumber(node);
		return new Assertion(parent, line, ICxxTestBase.STATUS_FAILED, MSG_FAILED_ASSERT_DELTA, values);
	}

	private static ICxxTestAssertion createFailedAssertNe(CxxTestMethod parent, Attributes node)
	{
		String[] values = getAttributeValues(node, new String[] {
				"line", "lhs-desc", "rhs-desc", "value" });
		int line = getLineNumber(node);
		return new Assertion(parent, line, ICxxTestBase.STATUS_FAILED, MSG_FAILED_ASSERT_NE, values);
	}

	private static ICxxTestAssertion createFailedAssertLt(CxxTestMethod parent, Attributes node)
	{
		String[] values = getAttributeValues(node, new String[] {
				"line", "lhs-desc", "rhs-desc", "lhs-value", "rhs-value" });
		int line = getLineNumber(node);
		return new Assertion(parent, line, ICxxTestBase.STATUS_FAILED, MSG_FAILED_ASSERT_LT, values);
	}

	private static ICxxTestAssertion createFailedAssertLe(CxxTestMethod parent, Attributes node)
	{
		String[] values = getAttributeValues(node, new String[] {
				"line", "lhs-desc", "rhs-desc", "lhs-value", "rhs-value" });
		int line = getLineNumber(node);
		return new Assertion(parent, line, ICxxTestBase.STATUS_FAILED, MSG_FAILED_ASSERT_LE, values);
	}

	private static ICxxTestAssertion createFailedAssertRelation(CxxTestMethod parent, Attributes node)
	{
		String[] values = getAttributeValues(node, new String[] {
				"line", "lhs-desc", "rhs-desc", "lhs-value", "rhs-value", "relation" });
		int line = getLineNumber(node);
		return new Assertion(parent, line, ICxxTestBase.STATUS_FAILED, MSG_FAILED_ASSERT_RELATION, values);
	}

	private static ICxxTestAssertion createFailedAssertPredicate(CxxTestMethod parent, Attributes node)
	{
		String[] values = getAttributeValues(node, new String[] {
				"line", "arg-desc", "arg-desc", "predicate" });
		int line = getLineNumber(node);
		return new Assertion(parent, line, ICxxTestBase.STATUS_FAILED, MSG_FAILED_ASSERT_PREDICATE, values);
	}

	private static ICxxTestAssertion createFailedAssertThrows(CxxTestMethod parent, Attributes node)
	{
		String[] values = getAttributeValues(node, new String[] {
				"line", "expression", "type", "threw" });
		
		int line = getLineNumber(node);

		if(values[3].equals("other"))
			values[3] = "it threw a different type";
		else
			values[3] = "it threw nothing";

		return new Assertion(parent, line, ICxxTestBase.STATUS_FAILED, MSG_FAILED_ASSERT_THROWS, values);
	}

	private static ICxxTestAssertion createFailedAssertNoThrow(CxxTestMethod parent, Attributes node)
	{
		String[] values = getAttributeValues(node, new String[] {
				"line", "expression" });
		int line = getLineNumber(node);
		return new Assertion(parent, line, ICxxTestBase.STATUS_FAILED, MSG_FAILED_ASSERT_NOTHROW, values);
	}
}
