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
package net.sf.webcat.eclipse.cxxtest.ui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

/**
 * A panel that shows the number of test methods executed, failed, and which
 * have errors.
 * 
 * Influenced greatly by the same JUnit class.
 */
public class CounterPanel extends Composite
{
	protected Text numberOfErrors;
	protected Text numberOfFailures;
	protected Text numberOfRuns;
	
	private final Image errorIcon = TestRunnerViewPart.createImage("ovr16/error_ovr.gif");
	private final Image failureIcon = TestRunnerViewPart.createImage("ovr16/failed_ovr.gif");

	public CounterPanel(Composite parent)
	{
		super(parent, SWT.WRAP);

		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 9;
		gridLayout.makeColumnsEqualWidth = false;
		gridLayout.marginWidth = 0;
		setLayout(gridLayout);
		
		numberOfRuns = createLabel("Runs:", null, "0");
		numberOfErrors = createLabel("Errors:", errorIcon, "0");
		numberOfFailures = createLabel("Failures:", failureIcon, "0");

		addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent e)
			{
				disposeIcons();
			}
		});
	}
 
	private void disposeIcons()
	{
		errorIcon.dispose();
		failureIcon.dispose();
	}

	private Text createLabel(String name, Image image, String init)
	{
		Label label = new Label(this, SWT.NONE);
		if(image != null)
		{
			image.setBackground(label.getBackground());
			label.setImage(image);
		}

		label.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING));
		
		label = new Label(this, SWT.NONE);
		label.setText(name);
		label.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING));
		
		Text value = new Text(this, SWT.READ_ONLY);
		value.setText(init);

		value.setBackground(
				getDisplay().getSystemColor(SWT.COLOR_WIDGET_BACKGROUND));
		value.setLayoutData(new GridData(
				GridData.FILL_HORIZONTAL | GridData.HORIZONTAL_ALIGN_BEGINNING));

		return value;
	}

	public void reset()
	{
		setErrorValue(0);
		setFailureValue(0);
		setRunValue(0);
	}
	
	public void setRunValue(int value)
	{
		numberOfRuns.setText(Integer.toString(value));
		redraw();
	}
	
	public void setErrorValue(int value)
	{
		numberOfErrors.setText(Integer.toString(value));
		redraw();
	}
	
	public void setFailureValue(int value)
	{
		numberOfFailures.setText(Integer.toString(value));
		redraw();
	}
}
