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

import net.sf.webcat.eclipse.cxxtest.model.IMemWatchInfo;
import net.sf.webcat.eclipse.cxxtest.model.IMemWatchLeak;

public class MemWatchInfo implements IMemWatchInfo
{
	private int totalBytesAllocated;
	private int maxBytesInUse;
	private int actualLeakCount;
	private IMemWatchLeak[] leaks;

	public MemWatchInfo(int totalBytes, int maxBytes) {
		totalBytesAllocated = totalBytes;
		maxBytesInUse = maxBytes;
	}

	public int getTotalBytesAllocated() {
		return totalBytesAllocated;
	}

	public int getMaxBytesInUse() {
		return maxBytesInUse;
	}

	public IMemWatchLeak[] getLeaks() {
		return leaks;
	}

	public void setLeaks(IMemWatchLeak[] leaks) {
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
}
