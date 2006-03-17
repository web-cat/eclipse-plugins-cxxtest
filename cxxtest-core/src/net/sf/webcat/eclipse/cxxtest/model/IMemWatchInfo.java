package net.sf.webcat.eclipse.cxxtest.model;

public interface IMemWatchInfo
{
	int getMaxBlocksInUse();

	int getCallsToNew();
	
	int getCallsToArrayNew();
	
	int getCallsToDelete();
	
	int getCallsToArrayDelete();

	IMemWatchLeak[] getLeaks();
}
