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
package net.sf.webcat.eclipse.cxxtest;

/**
 * Various constants used throughout the CxxTest plug-in.
 * 
 * @author Tony Allowatt (Virginia Tech Computer Science)
 */
public interface ICxxTestConstants
{
	/**
	 * Marker ID that represents an invocation problem or other severe error
	 * during the build process.
	 */
	static final String MARKER_INVOCATION_PROBLEM =
		CxxTestPlugin.PLUGIN_ID + ".invocationProblem";

	/**
	 * Marker ID that represents a failed test or other notification from the
	 * CxxTest runner (other notification could be a warning or trace message).
	 */
	static final String MARKER_FAILED_TEST =
		CxxTestPlugin.PLUGIN_ID + ".failedTest";

	/**
	 * Marker attribute associated with the "failedTest" marker.  This
	 * attribute holds the status value of the assertion (see the status codes
	 * in ICxxTestBase).
	 */
	static final String ATTR_ASSERTIONTYPE = "assertionType";

	/**
	 * The hard-coded filename of the results file that the CxxTest runner will
	 * generate. By default it is a dot-file, to prevent it from cluttering the
	 * student's project workspace in Eclipse.
	 */
	static final String TEST_RESULTS_FILE = ".cxxtestResults";

	/**
	 * The hard-coded filename of the results file that the Memwatch heap
	 * tracker will generate. By default it is a dot-file, to prevent it from
	 * cluttering the student's project workspace in Eclipse.
	 */
	static final String MEMWATCH_RESULTS_FILE = ".memwatchResults";
}
