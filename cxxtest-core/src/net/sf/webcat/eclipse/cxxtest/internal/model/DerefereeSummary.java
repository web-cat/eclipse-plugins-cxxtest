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

import net.sf.webcat.eclipse.cxxtest.model.IDerefereeSummary;
import net.sf.webcat.eclipse.cxxtest.model.IDerefereeLeak;

public class DerefereeSummary implements IDerefereeSummary
{
	private int totalBytesAllocated;
	private int maxBytesInUse;
	private int actualLeakCount;
	private int callsNew;
	private int callsDelete;
	private int callsArrayNew;
	private int callsArrayDelete;
	private int callsDeleteNull;
	private IDerefereeLeak[] leaks;

	public DerefereeSummary(int totalBytes, int maxBytes,
			int callsNew, int callsDelete, int callsArrayNew,
			int callsArrayDelete, int callsDeleteNull)
	{
		totalBytesAllocated = totalBytes;
		maxBytesInUse = maxBytes;
		this.callsNew = callsNew;
		this.callsDelete = callsDelete;
		this.callsArrayNew = callsArrayNew;
		this.callsArrayDelete = callsArrayDelete;
		this.callsDeleteNull = callsDeleteNull;
	}

	public int getTotalBytesAllocated() {
		return totalBytesAllocated;
	}

	public int getMaxBytesInUse() {
		return maxBytesInUse;
	}

	public IDerefereeLeak[] getLeaks() {
		return leaks;
	}

	public void setLeaks(IDerefereeLeak[] leaks) {
		this.leaks = leaks;
	}
	
	public int getActualLeakCount()
	{
		return actualLeakCount;
	}

	public void setActualLeakCount(int value)
	{
		actualLeakCount = value;
	}

	public int getCallsToNew()
	{
		return callsNew;
	}
	
	public int getCallsToDelete()
	{
		return callsDelete;
	}

	public int getCallsToArrayNew()
	{
		return callsArrayNew;
	}
	
	public int getCallsToArrayDelete()
	{
		return callsArrayDelete;
	}
	
	public int getCallsToDeleteNull()
	{
		return callsDeleteNull;
	}
}
