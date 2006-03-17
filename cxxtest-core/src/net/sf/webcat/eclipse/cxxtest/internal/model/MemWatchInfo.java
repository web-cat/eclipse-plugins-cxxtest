package net.sf.webcat.eclipse.cxxtest.internal.model;

import net.sf.webcat.eclipse.cxxtest.model.IMemWatchInfo;
import net.sf.webcat.eclipse.cxxtest.model.IMemWatchLeak;

public class MemWatchInfo implements IMemWatchInfo
{
	private int numMaxBlocks;
	private int numCallsNew;
	private int numCallsDelete;
	private int numCallsArrayNew;
	private int numCallsArrayDelete;
	private IMemWatchLeak[] leaks;

	public MemWatchInfo(int maxBlocks, int callsNew, int callsDelete,
			int callsArrayNew, int callsArrayDelete) {
		numMaxBlocks = maxBlocks;
		numCallsNew = callsNew;
		numCallsDelete = callsDelete;
		numCallsArrayNew = callsArrayNew;
		numCallsArrayDelete = callsArrayDelete;
	}

	public int getMaxBlocksInUse() {
		return numMaxBlocks;
	}

	public int getCallsToNew() {
		return numCallsNew;
	}

	public int getCallsToArrayNew() {
		return numCallsArrayNew;
	}

	public int getCallsToDelete() {
		return numCallsDelete;
	}

	public int getCallsToArrayDelete() {
		return numCallsArrayDelete;
	}

	public IMemWatchLeak[] getLeaks() {
		return leaks;
	}

	public void setLeaks(IMemWatchLeak[] leaks) {
		this.leaks = leaks;
	}
}
