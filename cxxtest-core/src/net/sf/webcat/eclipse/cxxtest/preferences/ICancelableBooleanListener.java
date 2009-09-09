package net.sf.webcat.eclipse.cxxtest.preferences;

public interface ICancelableBooleanListener
{
	boolean shouldDenyChange(boolean newValue);
}
